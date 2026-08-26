package com.example.predicte_plant_diseases.controller;

import com.example.predicte_plant_diseases.dto.AuthResponse;
import com.example.predicte_plant_diseases.dto.LoginRequest;
import com.example.predicte_plant_diseases.dto.RegisterRequest;
import com.example.predicte_plant_diseases.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final com.example.predicte_plant_diseases.util.JwtUtil jwtUtil;

    public AuthController(UserService userService, com.example.predicte_plant_diseases.util.JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody java.util.Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder().success(false).message("Refresh token is required").build());
        }

        try {
            com.auth0.jwt.interfaces.DecodedJWT jwt = jwtUtil.verifyToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(jwt);
            Long userId = jwtUtil.getUserIdFromToken(jwt);

            String newAccessToken = jwtUtil.generateAccessToken(username, userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);

            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .message("Token refreshed")
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().success(false).message("Invalid or expired refresh token").build());
        }
    }
}
