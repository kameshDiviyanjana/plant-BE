package com.example.predicte_plant_diseases.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mySecretKeyForPlantDiseasePredictionAppPleaseChangeMeInProduction";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    public String generateAccessToken(String username, Long userId, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    public String generateRefreshToken(String username, Long userId, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("role",role)
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public String getUsernameFromToken(DecodedJWT jwt) {
        return jwt.getSubject();
    }

    public Long getUserIdFromToken(DecodedJWT jwt) {
        return jwt.getClaim("userId").asLong();
    }

    public String getRoleFromToken(DecodedJWT jwt) {
        return jwt.getClaim("role").asString();
    }
}
