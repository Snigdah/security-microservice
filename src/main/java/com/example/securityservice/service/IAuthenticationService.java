package com.example.securityservice.service;

import com.example.securityservice.DTO.AuthenticationResponseDTO;
import com.example.securityservice.DTO.RegisterRequestDTO;

public interface IAuthenticationService {
    public AuthenticationResponseDTO register(RegisterRequestDTO request);
}
