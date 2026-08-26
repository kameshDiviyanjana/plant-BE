import os
import json
import torch
import torch.nn as nn
import torchvision.transforms as transforms
from fastapi import FastAPI, UploadFile, File, HTTPException
from PIL import Image
import io

app = FastAPI(title="Plant Disease Prediction API")

# Reconstruct the ResNet9 architecture
def conv_block(in_channels, out_channels, pool=False):
    layers = [
        nn.Conv2d(in_channels, out_channels, kernel_size=3, padding=1),
        nn.BatchNorm2d(out_channels),
        nn.ReLU(inplace=True)
    ]
    if pool:
        layers.append(nn.MaxPool2d(2))
    return nn.Sequential(*layers)

class ResNet9(nn.Module):
    def __init__(self, in_channels, num_classes):
        super().__init__()
        self.conv1 = conv_block(in_channels, 64)
        self.conv2 = conv_block(64, 128, pool=True)
        self.res1 = nn.Sequential(conv_block(128, 128), conv_block(128, 128))
        
        self.conv3 = conv_block(128, 256, pool=True)
        self.conv4 = conv_block(256, 512, pool=True)
        self.res2 = nn.Sequential(conv_block(512, 512), conv_block(512, 512))
        
        self.classifier = nn.Sequential(
            nn.MaxPool2d(4),
            nn.Flatten(),
            nn.Linear(512, num_classes)
        )

    def forward(self, xb):
        out = self.conv1(xb)
        out = self.conv2(out)
        out = out + self.res1(out)
        out = self.conv3(out)
        out = self.conv4(out)
        out = out + self.res2(out)
        out = self.classifier(out)
        return out

# Global model and indices loaders
model = None
classes = []

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "plant_disease_prediction_model.pth")
CLASSES_PATH = os.path.join(BASE_DIR, "class_indices.json")

# Define Image Transformations matching ResNet9 training parameters
# Input size is 32x32 based on model architecture inspection
transform = transforms.Compose([
    transforms.Resize((32, 32)),
    transforms.ToTensor(),
    # Standard normalization for ImageNet/PlantVillage datasets
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

@app.on_event("startup")
def load_resources():
    global model, classes
    
    # Load class indices
    if not os.path.exists(CLASSES_PATH):
        raise RuntimeError(f"Class indices file not found at: {CLASSES_PATH}")
    with open(CLASSES_PATH, "r") as f:
        classes = json.load(f)
        
    # Load model
    if not os.path.exists(MODEL_PATH):
        raise RuntimeError(f"Model file not found at: {MODEL_PATH}")
        
    model = ResNet9(3, len(classes))
    state_dict = torch.load(MODEL_PATH, map_location=torch.device('cpu'), weights_only=False)
    model.load_state_dict(state_dict)
    model.eval()
    print("PyTorch model loaded successfully with", len(classes), "classes.")

@app.post("/predict")
async def predict(image: UploadFile = File(...)):
    if model is None:
        raise HTTPException(status_code=500, detail="Model is not loaded.")
        
    try:
        # Read uploaded image bytes
        contents = await image.read()
        pil_image = Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid image file: {str(e)}")
        
    try:
        # Preprocess the image
        tensor = transform(pil_image).unsqueeze(0)  # Add batch dimension
        
        # Run inference without tracking gradients
        with torch.no_grad():
            outputs = model(tensor)
            # Apply softmax to get probabilities
            probabilities = torch.softmax(outputs, dim=1)[0]
            confidence, predicted_idx = torch.max(probabilities, dim=0)
            
        predicted_class = classes[predicted_idx.item()]
        
        # Parse the class label (e.g. "Apple___Apple_scab" -> "Apple", "Apple scab")
        parts = predicted_class.split("___")
        plant_name = parts[0].replace("_", " ")
        disease_name = parts[1].replace("_", " ") if len(parts) > 1 else "healthy"
        
        return {
            "plant_name": plant_name,
            "disease_name": disease_name,
            "confidence": round(confidence.item(), 4),
            "raw_class": predicted_class
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")

@app.get("/health")
def health_check():
    return {"status": "healthy", "model_loaded": model is not None, "classes_count": len(classes)}
