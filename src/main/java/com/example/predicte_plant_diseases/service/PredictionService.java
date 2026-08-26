package com.example.predicte_plant_diseases.service;

import com.example.predicte_plant_diseases.dto.PredictionRequest;
import com.example.predicte_plant_diseases.dto.PredictionResponse;
import java.util.List;

public interface PredictionService {
    PredictionResponse savePrediction(PredictionRequest request);
    List<PredictionResponse> getPredictionsByUserId(Long userId);
    PredictionResponse getPredictionById(Long id);
    PredictionResponse predictAndSave(org.springframework.web.multipart.MultipartFile imageFile, Long userId);
}
