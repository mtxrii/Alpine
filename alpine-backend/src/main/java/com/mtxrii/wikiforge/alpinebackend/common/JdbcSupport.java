package com.mtxrii.wikiforge.alpinebackend.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public abstract class JdbcSupport {
    protected final NamedParameterJdbcTemplate jdbc;

    protected JdbcSupport(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    protected UUID insertReturningId(String sql, Map<String, ?> params) {
        return jdbc.queryForObject(sql, params, UUID.class);
    }

    protected UUID insertReturningId(String sql, SqlParameterSource params) {
        return jdbc.queryForObject(sql, params, UUID.class);
    }

    protected int update(String sql, Map<String, ?> params) {
        return jdbc.update(sql, new MapSqlParameterSource(params));
    }

    protected int update(String sql, SqlParameterSource params) {
        return jdbc.update(sql, params);
    }

    protected <T> T required(String name, UUID id, String sql, Map<String, ?> params, RowMapper<T> mapper) {
        return optional(sql, params, mapper).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, name + " not found: " + id));
    }

    protected <T> Optional<T> optional(String sql, Map<String, ?> params, RowMapper<T> mapper) {
        List<T> rows = jdbc.query(sql, params, mapper);
        return rows.stream().findFirst();
    }

    protected <T> Optional<T> optional(String sql, SqlParameterSource params, RowMapper<T> mapper) {
        List<T> rows = jdbc.query(sql, params, mapper);
        return rows.stream().findFirst();
    }

    protected MapSqlParameterSource params(Object... keysAndValues) {
        var source = new MapSqlParameterSource();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            source.addValue((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return source;
    }

    protected void ensureUpdated(String name, UUID id, int rows) {
        if (rows == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, name + " not found: " + id);
        }
    }

    protected static Instant instant(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }
}
