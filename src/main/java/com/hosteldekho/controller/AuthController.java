package com.hosteldekho.controller;

import com.hosteldekho.dto.ApiDtos.AuthRequest;
import com.hosteldekho.dto.ApiDtos.AuthResponse;
import com.hosteldekho.dto.ApiDtos.RegisterRequest;
import com.hosteldekho.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest) {
        return service.login(request, servletRequest.getRemoteAddr());
    }
}
