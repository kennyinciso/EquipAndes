package pe.edu.upeu.AlquileresBackend.service.impl;

import java.sql.*;
import java.time.*;
import javax.sql.DataSource;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import pe.edu.upeu.AlquileresBackend.dto.HealthResponseDTO;
import pe.edu.upeu.AlquileresBackend.service.service.HealthService;

@Service
public class HealthServiceImpl implements HealthService {
  private static final Logger log = LoggerFactory.getLogger(HealthServiceImpl.class);
  private final DataSource datasource;
  private final Clock reloj;

  public HealthServiceImpl(DataSource datasource, Clock reloj) {
    this.datasource = datasource;
    this.reloj = reloj;
  }

  public HealthResponseDTO comprobar() {
    try (Connection c = datasource.getConnection();
        Statement s = c.createStatement()) {
      s.setQueryTimeout(2);
      try (ResultSet rs = s.executeQuery("SELECT 1 FROM DUAL")) {
        if (rs.next() && rs.getInt(1) == 1)
          return new HealthResponseDTO("UP", "UP", LocalDateTime.now(reloj));
      }
    } catch (SQLException e) {
      log.warn("La comprobación de conexión a la base de datos falló");
    }
    return new HealthResponseDTO("DOWN", "DOWN", LocalDateTime.now(reloj));
  }
}
