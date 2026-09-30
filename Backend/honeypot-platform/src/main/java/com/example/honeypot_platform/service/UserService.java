package com.example.honeypot_platform.service;

import com.example.honeypot_platform.dto.UserCreateRequest;
import com.example.honeypot_platform.dto.UserResponse;
import com.example.honeypot_platform.dto.UserUpdateRequest;
import com.example.honeypot_platform.entity.User;
import com.example.honeypot_platform.entity.UserRole;
import com.example.honeypot_platform.exception.DuplicateResourceException;
import com.example.honeypot_platform.exception.InvalidRequestException;
import com.example.honeypot_platform.exception.ResourceNotFoundException;
import com.example.honeypot_platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(parseRole(request.role()))
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUserOrThrow(id));
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUserOrThrow(id);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.email() != null && !request.email().isBlank()) {
            userRepository.findByEmail(request.email())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Email already registered: " + request.email());
                    });
            user.setEmail(request.email());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null && !request.role().isBlank()) {
            user.setRole(parseRole(request.role()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.fromString(role);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException(ex.getMessage());
        }
    }
}
