package com.example.foodordering.service;

import com.example.foodordering.dto.AdminUserResponse;
import com.example.foodordering.dto.ProfileUpdateRequest;
import com.example.foodordering.dto.UserProfileResponse;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(String userId) {
        User user = findUserById(userId);
        return mapToUserProfileResponse(user);
    }

    public UserProfileResponse updateProfile(String userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);

        user.setName(request.getName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());
    }

    public AdminUserResponse getUserById(String userId) {
        User user = findUserById(userId);
        return mapToAdminUserResponse(user);
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
