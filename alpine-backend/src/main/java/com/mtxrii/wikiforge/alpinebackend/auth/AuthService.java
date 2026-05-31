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
import org.springframework.stereotype.Service;

@Service
public class AuthService extends JdbcSupport {
    private final JwtTokenService jwtTokenService;

    private final RowMapper<UserResponse> userMapper = (rs, rowNum) -> new UserResponse(
            rs.getObject("id", UUID.class),
            rs.getString("email"),
            rs.getString("name"),
            rs.getString("avatar_url")
    );

    public AuthService(NamedParameterJdbcTemplate jdbc, JwtTokenService jwtTokenService) {
        super(jdbc);
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO users (email, name, avatar_url)
                VALUES (:email, :name, :avatarUrl)
                RETURNING id
                """, params("email", request.email(), "name", request.name(), "avatarUrl", request.avatarUrl()));
        return jwtTokenService.issueAuthResponse(getUser(id));
    }

    public AuthResponse login(LoginRequest request) {
        UserResponse user = optional("SELECT * FROM users WHERE email = :email", Map.of("email", request.email()), userMapper)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email"));
        return jwtTokenService.issueAuthResponse(user);
    }

    public UserResponse getUser(UUID id) {
        return required("User", id, "SELECT * FROM users WHERE id = :id", Map.of("id", id), userMapper);
    }
}
