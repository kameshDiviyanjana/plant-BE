package com.example.predicte_plant_diseases.service;

import com.example.predicte_plant_diseases.dto.PredictionRequest;
import com.example.predicte_plant_diseases.dto.PredictionResponse;
import com.example.predicte_plant_diseases.entity.Prediction;
import com.example.predicte_plant_diseases.entity.User;
import com.example.predicte_plant_diseases.repository.PredictionRepository;
import com.example.predicte_plant_diseases.repository.UserRepository;
import com.example.predicte_plant_diseases.util.DiseaseTreatmentUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;

    public PredictionServiceImpl(PredictionRepository predictionRepository, UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PredictionResponse savePrediction(PredictionRequest request) {
        if (request.getPlantName() == null || request.getPlantName().trim().isEmpty()) {
            throw new IllegalArgumentException("Plant name is required");
        }
        if (request.getDiseaseName() == null || request.getDiseaseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Disease name is required");
        }
        if (request.getConfidence() == null) {
            throw new IllegalArgumentException("Confidence score is required");
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));
        }

        String treatment = request.getTreatment();
        if (treatment == null || treatment.trim().isEmpty()) {
            treatment = DiseaseTreatmentUtil.getTreatment(request.getPlantName(), request.getDiseaseName());
        }

        Prediction prediction = Prediction.builder()
                .user(user)
                .plantName(request.getPlantName())
                .diseaseName(request.getDiseaseName())
                .confidence(request.getConfidence())
                .imageUrl(request.getImageUrl())
                .treatment(treatment)
                .build();

        Prediction saved = predictionRepository.save(prediction);
        return mapToPredictionResponse(saved);
    }

    @Override
    public List<PredictionResponse> getPredictionsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        return predictionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToPredictionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PredictionResponse getPredictionById(Long id) {
        Prediction prediction = predictionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prediction not found with id: " + id));
        return mapToPredictionResponse(prediction);
    }

    @Override
    public PredictionResponse predictAndSave(MultipartFile imageFile, Long userId) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty or missing");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Wrap file bytes in ByteArrayResource to prevent disk IO
            ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename() : "image.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String url = "http://localhost:8000/predict";

            ResponseEntity<FastApiResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, FastApiResponse.class);
            FastApiResponse apiResponse = responseEntity.getBody();

            if (apiResponse == null) {
                throw new RuntimeException("ML prediction service returned an empty response");
            }

            PredictionRequest request = PredictionRequest.builder()
                    .userId(userId)
                    .plantName(apiResponse.getPlant_name())
                    .diseaseName(apiResponse.getDisease_name())
                    .confidence(apiResponse.getConfidence())
                    .imageUrl(null) // Can be extended with S3/local file upload URL if needed
                    .build();

            return savePrediction(request);

        } catch (Exception e) {
            throw new RuntimeException("Prediction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PredictionResponse> getAllPredictions() {
        return predictionRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToPredictionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePrediction(Long id) {
        if (!predictionRepository.existsById(id)) {
            throw new IllegalArgumentException("Prediction not found with id: " + id);
        }
        predictionRepository.deleteById(id);
    }

    private PredictionResponse mapToPredictionResponse(Prediction prediction) {

        return PredictionResponse.builder()
                .id(prediction.getId())
                .userId(prediction.getUser() != null ? prediction.getUser().getId() : null)
                .plantName(prediction.getPlantName())
                .diseaseName(prediction.getDiseaseName())
                .confidence(prediction.getConfidence())
                .imageUrl(prediction.getImageUrl())
                .treatment(prediction.getTreatment())
                .createdAt(prediction.getCreatedAt())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class FastApiResponse {
        private String plant_name;
        private String disease_name;
        private Double confidence;
        private String raw_class;
    }
}
