package ru.yandex.practicum.smart.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class DynamicSqlServiceTest {
    @Test
    void shouldRejectNonSelectQueryFromDynamicApi() {
        DataSource dataSource = Mockito.mock(DataSource.class);
        DynamicSqlService service = new DynamicSqlService(dataSource);

        assertThrows(IllegalArgumentException.class,
                () -> service.query("DELETE FROM users", java.util.Map.of()));
    }
}
