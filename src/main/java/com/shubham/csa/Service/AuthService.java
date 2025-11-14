package com.shubham.csa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.shubham.csa.dto.*;
import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.Security.JwtTokenUtil;
import com.shubham.csa.dto.AuthResponse;
import com.shubham.csa.dto.LoginRequest;
import com.shubham.csa.entity.User;

/**
 * Authentication Service
 * Handles user login, token generation, and refresh
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    // Simplified login (for demo - no password check)
    // In production, you'd verify password with BCrypt
    public AuthResponse login(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        // Generate tokens
        String accessToken = jwtTokenUtil.generateToken(
            user.getId(), 
            user.getEmail(), 
            user.getRole().toString()
        );

        String refreshToken = jwtTokenUtil.generateRefreshToken(
            user.getId(), 
            user.getEmail()
        );

        // Convert user to DTO
        UserDto userDto = userService.getUserById(user.getId());

        // Return auth response
        return new AuthResponse(accessToken, refreshToken, jwtExpiration / 1000, userDto);
    }

    // Refresh access token using refresh token
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Extract user info from refresh token
        String userId = jwtTokenUtil.extractUserId(refreshToken);
        String email = jwtTokenUtil.extractUsername(refreshToken);

        // Get user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        // Generate new access token
        String newAccessToken = jwtTokenUtil.generateToken(
            user.getId(), 
            user.getEmail(), 
            user.getRole().toString()
        );

        // Convert user to DTO
        UserDto userDto = userService.getUserById(user.getId());

        // Return new tokens
        return new AuthResponse(newAccessToken, refreshToken, jwtExpiration / 1000, userDto);
    }

    // Get current authenticated user
    public User getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
