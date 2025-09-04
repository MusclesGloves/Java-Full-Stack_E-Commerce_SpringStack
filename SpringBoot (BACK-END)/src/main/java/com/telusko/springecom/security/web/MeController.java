package com.telusko.springecom.security.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public MeResponse me(Authentication auth){
        if(auth == null) return new MeResponse("anonymous", Set.of());
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return new MeResponse(auth.getName(), roles);
    }

    public static class MeResponse {
        public String username;
        public Set<String> roles;
        public MeResponse(String username, Set<String> roles){
            this.username = username;
            this.roles = roles;
        }
    }
}
