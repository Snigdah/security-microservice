package com.example.securityservice.service;

import com.example.securityservice.DTO.AuthenticationResponseDTO;
import com.example.securityservice.DTO.RegisterRequestDTO;
import com.example.securityservice.entity.Role;
import com.example.securityservice.entity.User;
import com.example.securityservice.exception.AuthenticationException;
import com.example.securityservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService implements IAuthenticationService{

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        // Check if the email already exists in the database
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthenticationException(HttpStatus.BAD_REQUEST, "Email address is already taken.");
        }

        Role userRole;
        if ("USER".equalsIgnoreCase(request.getRole())) {
            userRole = Role.USER;
        } else if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            userRole = Role.ADMIN;
        } else {
            throw new AuthenticationException(HttpStatus.BAD_REQUEST, "Invalid role. Supported roles are USER and ADMIN.");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        // Save the user to the database
        User newUser = userRepository.save(user);

        // Generate JWT token and return the response
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponseDTO.builder()
                .userId(newUser.getId())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .token(jwtToken)
                .build();
    }

}
