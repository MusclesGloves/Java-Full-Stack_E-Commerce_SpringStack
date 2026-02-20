package com.stack.spring.security.service;

import com.stack.spring.security.dto.AdminCreateUserRequest;
import com.stack.spring.security.model.AppUser;
import com.stack.spring.security.repo.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class AdminService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AdminService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public void createUser(AdminCreateUserRequest req) {
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
    }
}
