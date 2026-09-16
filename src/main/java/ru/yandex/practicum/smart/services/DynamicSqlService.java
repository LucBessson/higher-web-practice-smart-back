package ru.yandex.practicum.smart.services;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.util.*;

@Service
public class DynamicSqlService {
    private final NamedParameterJdbcTemplate jdbc;

    public DynamicSqlService(DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> query(String sql, Map<String, ?> parameters) {
        if (!isReadOnly(sql)) {
            throw new IllegalArgumentException("Only SELECT queries are allowed through dynamic API");
        }
        return jdbc.queryForList(sql, new MapSqlParameterSource(parameters));
    }

    public int execute(String sql, Map<String, ?> parameters) {
        return jdbc.update(sql, new MapSqlParameterSource(parameters));
    }

    private boolean isReadOnly(String sql) {
        String normalized = sql.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("select") || normalized.startsWith("with");
    }
}
