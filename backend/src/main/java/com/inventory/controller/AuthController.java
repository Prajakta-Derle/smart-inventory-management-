package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.LoginRequestDTO;
import com.inventory.dto.LoginResponseDTO;
import com.inventory.dto.RegisterRequestDTO;
import com.inventory.dto.UserResponseDTO;
import com.inventory.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Public endpoint — registers a new STAFF user.
     * No role field accepted; any submitted role is silently ignored.
     * Returns HTTP 201 on success.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        UserResponseDTO created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "User registered successfully", created));
    }

    /**
     * Public endpoint — authenticates and returns a JWT.
     * Returns HTTP 200 with token on success; 401 on bad credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Login successful", response));
    }
}
