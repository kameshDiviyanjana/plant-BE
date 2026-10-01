package com.example.predicte_plant_diseases.service;

import com.example.predicte_plant_diseases.dto.AuthResponse;
import com.example.predicte_plant_diseases.dto.LoginRequest;
import com.example.predicte_plant_diseases.dto.RegisterRequest;
import com.example.predicte_plant_diseases.entity.User;
import com.example.predicte_plant_diseases.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final com.example.predicte_plant_diseases.util.JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, com.example.predicte_plant_diseases.util.JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Username is required")
                    .build();
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email is required")
                    .build();
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Password must be at least 6 characters long")
                    .build();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Username is already taken")
                    .build();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email is already registered")
                    .build();
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashedPassword)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(savedUser.getUsername(), savedUser.getId());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUsername(), savedUser.getId());

        return AuthResponse.builder()
                .success(true)
                .message("User registered successfully")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserDetails(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().trim().isEmpty()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Username or Email is required")
                    .build();
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Password is required")
                    .build();
        }

        Optional<User> userOpt = userRepository.findByUsername(request.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(request.getUsernameOrEmail());
        }

        if (userOpt.isEmpty()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserDetails(user))
                .build();
    }

    private AuthResponse.UserDetails mapToUserDetails(User user) {
        return AuthResponse.UserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public java.util.List<AuthResponse.UserDetails> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserDetails)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}

