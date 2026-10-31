package com.infobro.apaar;

import com.infobro.apaar.controller.HealthController;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {
    @Test void healthyDatabaseReturnsUpAndClosesConnection() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        var result = new HealthController(source).health();
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsEntry("status", "UP");
        verify(connection).close();
    }
    @Test void unavailableDatabaseReturns503WithoutLeakingDetails() throws Exception {
        DataSource source = mock(DataSource.class);
        when(source.getConnection()).thenThrow(new SQLException("private connection detail"));
        var result = new HealthController(source).health();
        assertThat(result.getStatusCode().value()).isEqualTo(503);
        assertThat(result.getBody()).hasSize(1).containsEntry("status", "DOWN");
    }
}
