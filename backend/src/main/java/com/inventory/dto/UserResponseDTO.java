package com.inventory.dto;

import com.inventory.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    // passwordHash is deliberately excluded
}
