package com.stack.spring.security.service;

import com.stack.spring.security.dto.AuthResponse;
import com.stack.spring.security.dto.LoginRequest;
import com.stack.spring.security.dto.RegisterRequest;
import com.stack.spring.security.model.AppUser;
import com.stack.spring.security.repo.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final long TOKEN_TTL_MS = 1000L * 60 * 60 * 24; // 24 hours

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(AppUserRepository users,
                       PasswordEncoder encoder,
                       AuthenticationManager authManager,
                       JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (users.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");

        if (req.isAdmin()) {
            roles.add("ROLE_ADMIN");
        }

        AppUser u = new AppUser(req.getUsername(), encoder.encode(req.getPassword()), roles);
        users.save(u);

        String token = jwt.generateToken(u.getUsername(), u.getRoles(), TOKEN_TTL_MS);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        // Will throw if invalid credentials
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        AppUser u = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = jwt.generateToken(u.getUsername(), u.getRoles(), TOKEN_TTL_MS);
        return new AuthResponse(token);
    }
}
