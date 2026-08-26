package com.example.predicte_plant_diseases.controller;

import com.example.predicte_plant_diseases.dto.PredictionResponse;
import com.example.predicte_plant_diseases.service.PredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PredictionControllerPredictionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PredictionService predictionService;

    @MockitoBean
    private com.example.predicte_plant_diseases.config.JwtInterceptor jwtInterceptor;

    @BeforeEach
    public void setup() {
        try {
            when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testPredictSuccess() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test_tomato.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        PredictionResponse mockResponse = PredictionResponse.builder()
                .id(1L)
                .userId(10L)
                .plantName("Tomato")
                .diseaseName("Early Blight")
                .confidence(0.92)
                .createdAt(LocalDateTime.now())
                .build();

        when(predictionService.predictAndSave(any(), eq(10L))).thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/predictions/predict")
                        .file(imageFile)
                        .param("userId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.plantName", is("Tomato")))
                .andExpect(jsonPath("$.diseaseName", is("Early Blight")))
                .andExpect(jsonPath("$.confidence", is(0.92)))
                .andExpect(jsonPath("$.userId", is(10)));
    }
}
