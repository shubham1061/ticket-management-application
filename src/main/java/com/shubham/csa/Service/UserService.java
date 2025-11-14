package com.shubham.csa.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.dto.CreateUserDto;
import com.shubham.csa.dto.UserDto;
import com.shubham.csa.entity.User;


@Service
public class UserService {
  @Autowired
    private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
    // Create a new user
    public UserDto createUser(CreateUserDto createDto) {
        // Check if user already exists
        String hashedPassword = passwordEncoder.encode(createDto.getPassword());

        Optional<User> existingUser = userRepository.findByEmail(createDto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        User user = new User(createDto.getName(), createDto.getEmail(), createDto.getRole());
        user.setPassword(hashedPassword);
        user = userRepository.save(user);

        return mapToDto(user);
    }

    

    // Get user by ID
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToDto(user);
    }

    // Get user by email
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToDto(user);
    }

    // Get all users
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Get users by role
    public List<UserDto> getUsersByRole(User.Role role) {
        return userRepository.findByRoleAndActiveTrue(role).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Update user
    public UserDto updateUser(String id, CreateUserDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setName(updateDto.getName());
        user.setEmail(updateDto.getEmail());
        user.setRole(updateDto.getRole());

        user = userRepository.save(user);
        return mapToDto(user);
    }

    // Deactivate user (soft delete)
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setActive(false);
        userRepository.save(user);
    }

    // Helper method to convert Entity to DTO
    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
