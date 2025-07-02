package com.foroescolar.services;

import com.foroescolar.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

public interface TokenService {

    boolean validateToken(String token, UserDetails userDetails);
    String generateToken(User user);
    String getUsernameFromToken(String token);
    void invalidateToken(String token);

    boolean isTokenBlacklisted(String token);
    public boolean isTokenInBlacklist(String token);

    Set<String> getAllBlacklistedTokens();
}
