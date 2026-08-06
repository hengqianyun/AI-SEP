package com.shdata.datachain.common.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码哈希（BCrypt）。
 */
@Component
public class PasswordHasher {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String raw) {
        return encoder.encode(raw);
    }

    public boolean matches(String raw, String hash) {
        if (raw == null || hash == null || hash.isBlank()) {
            return false;
        }
        return encoder.matches(raw, hash);
    }
}
