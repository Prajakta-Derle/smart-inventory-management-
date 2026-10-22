package com.inventory.service;

import com.inventory.dto.CreateUserRequestDTO;
import com.inventory.dto.LoginRequestDTO;
import com.inventory.dto.LoginResponseDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.RegisterRequestDTO;
import com.inventory.dto.UserResponseDTO;
import com.inventory.entity.Role;
import com.inventory.entity.User;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // ── Public registration — always STAFF ───────────────────────────────────

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        validateUniqueConstraints(request.getUsername(), request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)          // public registration always STAFF
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return toResponseDTO(saved);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
        } catch (AuthenticationException ex) {
            // Generic message — do not distinguish unknown user vs wrong password
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUsername()));

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .role(user.getRole())
                .username(user.getUsername())
                .build();
    }

    // ── ADMIN: create user with explicit role ─────────────────────────────────

    @Override
    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        validateUniqueConstraints(request.getUsername(), request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();

        return toResponseDTO(userRepository.save(user));
    }

    // ── ADMIN: list users ─────────────────────────────────────────────────────

    @Override
    public PageResponseDTO<UserResponseDTO> listUsers(Pageable pageable) {
        Page<UserResponseDTO> page = userRepository.findAll(pageable)
                .map(this::toResponseDTO);
        return PageResponseDTO.from(page);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateUniqueConstraints(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("username", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("email", email);
        }
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
