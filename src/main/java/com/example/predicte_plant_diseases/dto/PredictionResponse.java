package com.example.predicte_plant_diseases.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private Long id;
    private Long userId;
    private String plantName;
    private String diseaseName;
    private Double confidence;
    private String imageUrl;
    private LocalDateTime createdAt;
}
