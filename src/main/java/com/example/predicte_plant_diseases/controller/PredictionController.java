package com.example.predicte_plant_diseases.controller;

import com.example.predicte_plant_diseases.dto.PredictionRequest;
import com.example.predicte_plant_diseases.dto.PredictionResponse;
import com.example.predicte_plant_diseases.service.PredictionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<?> savePrediction(@RequestBody PredictionRequest request) {
        try {
            PredictionResponse saved = predictionService.savePrediction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestParam("image") org.springframework.web.multipart.MultipartFile image,
                                     @RequestAttribute(value = "userId", required = false) Long attributeUserId,
                                     @RequestParam(value = "userId", required = false) Long userId) {
        try {
            Long finalUserId = attributeUserId != null ? attributeUserId : userId;
            PredictionResponse result = predictionService.predictAndSave(image, finalUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPredictionsByUserId(@PathVariable Long userId) {
        try {
            List<PredictionResponse> predictions = predictionService.getPredictionsByUserId(userId);
            return ResponseEntity.ok(predictions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPredictionById(@PathVariable Long id) {
        try {
            PredictionResponse prediction = predictionService.getPredictionById(id);
            return ResponseEntity.ok(prediction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
