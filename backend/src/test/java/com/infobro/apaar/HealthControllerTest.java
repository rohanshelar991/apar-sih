package com.infobro.apaar;

import com.infobro.apaar.controller.HealthController;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {
    @Test void healthyDatabaseReturnsUpAndClosesConnection() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        Connection connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isValid" -> true;
                    case "close" -> { closed.set(true); yield null; }
                    case "isClosed" -> closed.get();
                    case "unwrap" -> throw new SQLException("Not a wrapper");
                    case "isWrapperFor" -> false;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        DataSource source = new AbstractDataSource() {
            @Override public Connection getConnection() { return connection; }
            @Override public Connection getConnection(String username, String password) { return connection; }
        };
        var result = new HealthController(source).health();
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsEntry("status", "UP");
        assertThat(closed.get()).isTrue();
    }
    @Test void unavailableDatabaseReturns503WithoutLeakingDetails() throws Exception {
        DataSource source = new AbstractDataSource() {
            @Override public Connection getConnection() throws SQLException {
                throw new SQLException("private connection detail");
            }
            @Override public Connection getConnection(String username, String password) throws SQLException {
                throw new SQLException("private connection detail");
            }
        };
        var result = new HealthController(source).health();
        assertThat(result.getStatusCode().value()).isEqualTo(503);
        assertThat(result.getBody()).hasSize(1).containsEntry("status", "DOWN");
    }
}
