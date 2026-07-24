package com.mtxrii.wikiforge.alpinebackend.user;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.auth.dto.UserResponse;
import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService extends JdbcSupport {
    private final RowMapper<UserResponse> userMapper = (rs, rowNum) -> new UserResponse(
            rs.getObject("id", UUID.class),
            rs.getString("email"),
            rs.getString("name"),
            rs.getString("avatar_url")
    );

    public UserService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    public List<UserResponse> list() {
        return jdbc.query("SELECT id, email, name, avatar_url FROM users ORDER BY name ASC", userMapper);
    }

    public UserResponse get(UUID id) {
        return required("User", id, "SELECT id, email, name, avatar_url FROM users WHERE id = :id", Map.of("id", id), userMapper);
    }
}
