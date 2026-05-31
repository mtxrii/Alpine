package com.mtxrii.wikiforge.alpinebackend.auth;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.auth.dto.AuthResponse;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.LoginRequest;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.RegisterRequest;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.UserResponse;
import com.mtxrii.wikiforge.alpinebackend.common.ApiException;
import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import com.mtxrii.wikiforge.alpinebackend.security.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends JdbcSupport {
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    private final RowMapper<UserResponse> userMapper = (rs, rowNum) -> new UserResponse(
            rs.getObject("id", UUID.class),
            rs.getString("email"),
            rs.getString("name"),
            rs.getString("avatar_url")
    );

    public AuthService(NamedParameterJdbcTemplate jdbc, JwtTokenService jwtTokenService, PasswordEncoder passwordEncoder) {
        super(jdbc);
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO users (email, name, password_hash, avatar_url)
                VALUES (:email, :name, :passwordHash, :avatarUrl)
                RETURNING id
                """, params(
                "email", request.email(),
                "name", request.name(),
                "passwordHash", passwordEncoder.encode(request.password()),
                "avatarUrl", request.avatarUrl()));
        return jwtTokenService.issueAuthResponse(getUser(id));
    }

    public AuthResponse login(LoginRequest request) {
        UserCredentials user = optional(
                "SELECT id, email, name, avatar_url, password_hash FROM users WHERE email = :email",
                Map.of("email", request.email()),
                (rs, rowNum) -> new UserCredentials(
                        new UserResponse(
                                rs.getObject("id", UUID.class),
                                rs.getString("email"),
                                rs.getString("name"),
                                rs.getString("avatar_url")),
                        rs.getString("password_hash")))
                .orElseThrow(() -> invalidCredentials());
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw invalidCredentials();
        }

        return jwtTokenService.issueAuthResponse(user.user());
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private record UserCredentials(UserResponse user, String passwordHash) {
    }

    public UserResponse getUser(UUID id) {
        return required("User", id, "SELECT * FROM users WHERE id = :id", Map.of("id", id), userMapper);
    }
}
