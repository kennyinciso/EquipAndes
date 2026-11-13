package pe.edu.upeu.AlquileresBackend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.AlquileresBackend.controller.HealthController;
import pe.edu.upeu.AlquileresBackend.service.impl.HealthServiceImpl;

class HealthServiceTest {
  private final Clock reloj =
      Clock.fixed(Instant.parse("2026-11-13T15:00:00Z"), ZoneId.of("America/Lima"));

  @Test
  void conexionCaidaDevuelve503SinExponerElErrorDelDriver() throws Exception {
    DataSource datasource = mock(DataSource.class);
    when(datasource.getConnection()).thenThrow(new SQLException("Error interno de conexión"));
    var response = new HealthController(new HealthServiceImpl(datasource, reloj)).health();
    assertEquals(503, response.getStatusCode().value());
    assertEquals("DOWN", response.getBody().database());
    assertEquals(LocalDateTime.of(2026, 11, 13, 10, 0), response.getBody().timestamp());
  }

  @Test
  void conexionEjecutaConsultaRealYLiberaRecursos() throws Exception {
    DataSource datasource = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    Statement statement = mock(Statement.class);
    ResultSet result = mock(ResultSet.class);
    when(datasource.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.executeQuery("SELECT 1 FROM DUAL")).thenReturn(result);
    when(result.next()).thenReturn(true);
    when(result.getInt(1)).thenReturn(1);
    var response = new HealthController(new HealthServiceImpl(datasource, reloj)).health();
    assertEquals(200, response.getStatusCode().value());
    assertEquals("UP", response.getBody().database());
    verify(statement).setQueryTimeout(2);
    verify(result).close();
    verify(statement).close();
    verify(connection).close();
  }
}
