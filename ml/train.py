import os
import json
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
import torchvision.transforms as transforms
import torchvision.datasets as datasets

# Reconstruct the ResNet9 architecture matching app.py
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

def train_model(data_dir, epochs=10, batch_size=64, lr=0.001, save_dir="."):
    """
    Train ResNet9 model on the plant disease dataset.
    
    Expected folder structure for data_dir:
    data_dir/
        train/
            class1/
                img1.jpg
                ...
            class2/
                img2.jpg
                ...
        val/
            class1/
                ...
    """
    train_dir = os.path.join(data_dir, "train")
    val_dir = os.path.join(data_dir, "val")
    
    if not os.path.exists(train_dir):
        print(f"Error: Train directory {train_dir} does not exist.")
        print("Please structure your dataset folder as:")
        print("dataset/")
        print("  ├── train/ (subfolders for each class, e.g. Apple___healthy, etc.)")
        print("  └── val/ (subfolders for each class)")
        return

    # Image transformations: Resize to 32x32 matching the model input shape
    train_transform = transforms.Compose([
        transforms.Resize((32, 32)),
        transforms.RandomCrop(32, padding=4, padding_mode='reflect'),
        transforms.RandomHorizontalFlip(),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    
    val_transform = transforms.Compose([
        transforms.Resize((32, 32)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    
    # Load dataset
    print("Loading datasets...")
    train_dataset = datasets.ImageFolder(train_dir, transform=train_transform)
    print(f"Found {len(train_dataset)} training images across {len(train_dataset.classes)} classes.")
    
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True, num_workers=2, pin_memory=True)
    
    # Optional Validation loader
    val_loader = None
    if os.path.exists(val_dir):
        val_dataset = datasets.ImageFolder(val_dir, transform=val_transform)
        print(f"Found {len(val_dataset)} validation images.")
        val_loader = DataLoader(val_dataset, batch_size=batch_size, shuffle=False, num_workers=2, pin_memory=True)
    
    # Device configuration
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Training on device: {device}")
    
    # Instantiate Model
    num_classes = len(train_dataset.classes)
    model = ResNet9(3, num_classes).to(device)
    
    # Loss and optimizer
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=lr, weight_decay=1e-4)
    # OneCycleLR scheduler for faster, more stable convergence
    scheduler = optim.lr_scheduler.OneCycleLR(
        optimizer, lr, epochs=epochs, steps_per_epoch=len(train_loader)
    )
    
    # Save class indices
    class_indices_path = os.path.join(save_dir, "class_indices.json")
    with open(class_indices_path, "w") as f:
        json.dump(train_dataset.classes, f)
    print(f"Saved class indices configuration to: {class_indices_path}")
    
    # Training Loop
    best_acc = 0.0
    for epoch in range(epochs):
        model.train()
        running_loss = 0.0
        correct = 0
        total = 0
        
        for images, labels in train_loader:
            images, labels = images.to(device), labels.to(device)
            
            optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            scheduler.step()
            
            running_loss += loss.item() * images.size(0)
            _, predicted = outputs.max(1)
            total += labels.size(0)
            correct += predicted.eq(labels).sum().item()
            
        epoch_loss = running_loss / total
        epoch_acc = correct / total
        
        print(f"Epoch [{epoch+1}/{epochs}] - Loss: {epoch_loss:.4f} - Acc: {epoch_acc:.4f}")
        
        # Validation Phase
        if val_loader:
            model.eval()
            val_loss = 0.0
            val_correct = 0
            val_total = 0
            
            with torch.no_grad():
                for images, labels in val_loader:
                    images, labels = images.to(device), labels.to(device)
                    outputs = model(images)
                    loss = criterion(outputs, labels)
                    
                    val_loss += loss.item() * images.size(0)
                    _, predicted = outputs.max(1)
                    val_total += labels.size(0)
                    val_correct += predicted.eq(labels).sum().item()
            
            val_epoch_loss = val_loss / val_total
            val_epoch_acc = val_correct / val_total
            print(f"  Validation - Loss: {val_epoch_loss:.4f} - Acc: {val_epoch_acc:.4f}")
            
            # Save the best model
            if val_epoch_acc > best_acc:
                best_acc = val_epoch_acc
                torch.save(model.state_dict(), os.path.join(save_dir, "plant_disease_prediction_model.pth"))
                print(f"  Saved best model with validation accuracy: {best_acc:.4f}")
        else:
            # If no validation set, save current model at last epoch
            if epoch == epochs - 1:
                torch.save(model.state_dict(), os.path.join(save_dir, "plant_disease_prediction_model.pth"))
                print("  Saved final model weights.")

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Train Plant Disease Prediction ResNet9 Model")
    parser.add_argument("--data_dir", type=str, default="./dataset", help="Path to dataset root folder containing 'train' and 'val' subfolders")
    parser.add_argument("--epochs", type=int, default=10, help="Number of training epochs")
    parser.add_argument("--batch_size", type=int, default=64, help="Batch size for training")
    parser.add_argument("--lr", type=float, default=0.001, help="Max learning rate for OneCycleLR scheduler")
    parser.add_argument("--save_dir", type=str, default=".", help="Directory to save the trained model (.pth) and class_indices.json")
    
    args = parser.parse_args()
    train_model(args.data_dir, args.epochs, args.batch_size, args.lr, args.save_dir)
