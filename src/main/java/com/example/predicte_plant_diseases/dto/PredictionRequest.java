package com.example.predicte_plant_diseases.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    private Long userId;
    private String plantName;
    private String diseaseName;
    private Double confidence;
    private String imageUrl;
    private String treatment;
}
