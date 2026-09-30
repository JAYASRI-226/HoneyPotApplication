package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.entity.User;
import com.example.honeypot_platform.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** API response. Password is intentionally omitted. */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
