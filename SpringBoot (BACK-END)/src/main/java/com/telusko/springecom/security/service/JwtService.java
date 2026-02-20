package com.telusko.springecom.security.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {
    private final Key key = Keys.hmacShaKeyFor("replace-with-32byte-minimum-secret-key-123456".getBytes());

    public String generateToken(String username, Set<String> roles, long ttlMillis){
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String jwt){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
    }
}
