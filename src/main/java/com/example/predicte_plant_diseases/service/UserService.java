package com.example.predicte_plant_diseases.service;

import com.example.predicte_plant_diseases.dto.AuthResponse;
import com.example.predicte_plant_diseases.dto.LoginRequest;
import com.example.predicte_plant_diseases.dto.RegisterRequest;
import java.util.List;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    List<AuthResponse.UserDetails> getAllUsers();
    void deleteUser(Long id);
}

