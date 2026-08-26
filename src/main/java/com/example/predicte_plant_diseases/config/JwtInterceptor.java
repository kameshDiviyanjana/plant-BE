package com.example.predicte_plant_diseases.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.predicte_plant_diseases.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"Missing or invalid Authorization header\"}");
            response.setContentType("application/json");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            String username = jwtUtil.getUsernameFromToken(jwt);
            Long userId = jwtUtil.getUserIdFromToken(jwt);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return false;
        }
    }
}
