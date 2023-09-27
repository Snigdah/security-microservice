package com.example.securityservice.controller;


import com.example.securityservice.DTO.AuthenticationResponseDTO;
import com.example.securityservice.DTO.RegisterRequestDTO;
import com.example.securityservice.service.IAuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@AllArgsConstructor
public class AuthenticationController {

    private IAuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequestDTO request){
        AuthenticationResponseDTO responseDTO = authenticationService.register(request);
        return ResponseEntity.ok(responseDTO);
    }

}
