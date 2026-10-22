package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.CreateUserRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.UserResponseDTO;
import com.inventory.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /**
     * ADMIN only — creates a user with an explicit role (ADMIN or STAFF).
     * Returns HTTP 201 on success.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody CreateUserRequestDTO request) {

        UserResponseDTO created = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "User created successfully", created));
    }

    /**
     * ADMIN only — paginated list of all users.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponseDTO<UserResponseDTO>>> listUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        PageResponseDTO<UserResponseDTO> result = authService.listUsers(pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Users retrieved", result));
    }
}
