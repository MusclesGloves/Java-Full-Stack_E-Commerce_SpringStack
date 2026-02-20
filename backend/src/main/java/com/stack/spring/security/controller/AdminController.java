package com.stack.spring.security.controller;

import com.stack.spring.security.dto.AdminCreateUserRequest;
import com.stack.spring.security.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createUser(@Valid @RequestBody AdminCreateUserRequest req) {
        adminService.createUser(req);
        return ResponseEntity.noContent().build();
    }
}
