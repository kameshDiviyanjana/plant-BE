package com.example.predicte_plant_diseases.repository;

import com.example.predicte_plant_diseases.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
