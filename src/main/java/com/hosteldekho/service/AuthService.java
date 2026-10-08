package com.hosteldekho.service;

import com.hosteldekho.config.JwtService;
import com.hosteldekho.dto.ApiDtos.AuthRequest;
import com.hosteldekho.dto.ApiDtos.AuthResponse;
import com.hosteldekho.dto.ApiDtos.ManagerSummary;
import com.hosteldekho.dto.ApiDtos.RegisterRequest;
import com.hosteldekho.entity.Manager;
import com.hosteldekho.exception.ApiException;
import com.hosteldekho.repository.ManagerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthService {
    private final ManagerRepository managers;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginAttemptService loginAttempts;

    public AuthService(ManagerRepository managers, PasswordEncoder encoder, JwtService jwt,
                       LoginAttemptService loginAttempts) {
        this.managers = managers;
        this.encoder = encoder;
        this.jwt = jwt;
        this.loginAttempts = loginAttempts;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (managers.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        Manager manager = managers.save(Manager.builder().name(request.name().trim()).email(email)
            .passwordHash(encoder.encode(request.password())).phone(request.phone().trim()).build());
        return response(manager);
    }

    public AuthResponse login(AuthRequest request, String remoteAddress) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String key = remoteAddress + ":" + email;
        if (loginAttempts.isBlocked(key)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed login attempts; try again later");
        }
        Manager manager = managers.findByEmail(email).orElse(null);
        if (manager == null || !encoder.matches(request.password(), manager.getPasswordHash())) {
            loginAttempts.failed(key);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        loginAttempts.succeeded(key);
        return response(manager);
    }

    private AuthResponse response(Manager manager) {
        return new AuthResponse(jwt.create(manager.getEmail()),
            new ManagerSummary(manager.getId(), manager.getName(), manager.getEmail()));
    }
}
