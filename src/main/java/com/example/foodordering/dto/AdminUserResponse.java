package com.example.foodordering.dto;

import com.example.foodordering.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private Instant createdAt;
}
