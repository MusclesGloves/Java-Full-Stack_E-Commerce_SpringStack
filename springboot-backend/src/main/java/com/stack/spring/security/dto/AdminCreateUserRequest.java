package com.stack.spring.security.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminCreateUserRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private boolean admin;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin){this.admin = admin; }

}
