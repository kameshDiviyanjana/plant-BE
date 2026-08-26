package com.example.predicte_plant_diseases.controller;

import com.example.predicte_plant_diseases.dto.PredictionRequest;
import com.example.predicte_plant_diseases.entity.User;
import com.example.predicte_plant_diseases.repository.PredictionRepository;
import com.example.predicte_plant_diseases.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.example.predicte_plant_diseases.config.JwtInterceptor jwtInterceptor;

    private User savedUser;

    @BeforeEach
    public void setup() {
        try {
            org.mockito.Mockito.when(jwtInterceptor.preHandle(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(true);
        } catch (Exception e) {
            // ignore
        }

        predictionRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("predicter")
                .email("predicter@example.com")
                .password("hashed_password_123")
                .createdAt(LocalDateTime.now())
                .build();
        savedUser = userRepository.save(user);
    }

    @Test
    public void testSavePredictionWithUser() throws Exception {
        PredictionRequest request = PredictionRequest.builder()
                .userId(savedUser.getId())
                .plantName("Tomato")
                .diseaseName("Early Blight")
                .confidence(0.92)
                .imageUrl("http://example.com/images/tomato_blight.jpg")
                .build();

        mockMvc.perform(post("/api/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.plantName", is("Tomato")))
                .andExpect(jsonPath("$.diseaseName", is("Early Blight")))
                .andExpect(jsonPath("$.confidence", is(0.92)))
                .andExpect(jsonPath("$.imageUrl", is("http://example.com/images/tomato_blight.jpg")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    public void testSavePredictionAnonymous() throws Exception {
        PredictionRequest request = PredictionRequest.builder()
                .plantName("Apple")
                .diseaseName("Apple Scab")
                .confidence(0.85)
                .imageUrl("http://example.com/images/apple_scab.jpg")
                .build();

        mockMvc.perform(post("/api/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.plantName", is("Apple")))
                .andExpect(jsonPath("$.diseaseName", is("Apple Scab")))
                .andExpect(jsonPath("$.confidence", is(0.85)));
    }

    @Test
    public void testSavePredictionValidationFail() throws Exception {
        PredictionRequest request = PredictionRequest.builder()
                .plantName("")
                .diseaseName("Apple Scab")
                .confidence(0.85)
                .build();

        mockMvc.perform(post("/api/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetPredictionsByUserId() throws Exception {
        PredictionRequest request1 = PredictionRequest.builder()
                .userId(savedUser.getId())
                .plantName("Tomato")
                .diseaseName("Early Blight")
                .confidence(0.92)
                .build();

        PredictionRequest request2 = PredictionRequest.builder()
                .userId(savedUser.getId())
                .plantName("Potato")
                .diseaseName("Late Blight")
                .confidence(0.95)
                .build();

        mockMvc.perform(post("/api/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/predictions/user/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].plantName", is("Potato")))
                .andExpect(jsonPath("$[1].plantName", is("Tomato")));
    }
}
