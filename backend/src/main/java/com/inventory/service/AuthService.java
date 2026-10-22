package com.inventory.service;

import com.inventory.dto.CreateUserRequestDTO;
import com.inventory.dto.LoginRequestDTO;
import com.inventory.dto.LoginResponseDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.RegisterRequestDTO;
import com.inventory.dto.UserResponseDTO;
import org.springframework.data.domain.Pageable;

public interface AuthService {

    UserResponseDTO register(RegisterRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    UserResponseDTO createUser(CreateUserRequestDTO request);

    PageResponseDTO<UserResponseDTO> listUsers(Pageable pageable);
}
