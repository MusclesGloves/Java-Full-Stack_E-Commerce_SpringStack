package com.telusko.springecom.security.web;

import com.telusko.springecom.security.dto.AuthResponse;
import com.telusko.springecom.security.dto.LoginRequest;
import com.telusko.springecom.security.dto.RegisterRequest;
import com.telusko.springecom.security.model.AppUser;
import com.telusko.springecom.security.repo.AppUserRepository;
import com.telusko.springecom.security.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthController(AppUserRepository users, PasswordEncoder encoder,
                          AuthenticationManager authManager, JwtService jwt){
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req){
        if(users.existsByUsername(req.getUsername())){
            throw new RuntimeException("Username already exists");
        }
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        if(req.isAdmin()) roles.add("ROLE_ADMIN");
        AppUser u = new AppUser(req.getUsername(), encoder.encode(req.getPassword()), roles);
        users.save(u);
        String token = jwt.generateToken(u.getUsername(), u.getRoles(), 1000L * 60 * 60 * 24);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req){
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        AppUser u = users.findByUsername(req.getUsername()).orElseThrow();
        String token = jwt.generateToken(u.getUsername(), u.getRoles(), 1000L * 60 * 60 * 24);
        return new AuthResponse(token);
    }
}
