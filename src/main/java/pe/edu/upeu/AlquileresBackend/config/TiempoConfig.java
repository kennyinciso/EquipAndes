package pe.edu.upeu.AlquileresBackend.config;

import java.time.*;
import org.springframework.context.annotation.*;

@Configuration
public class TiempoConfig {
  @Bean
  public Clock reloj() {
    return Clock.system(ZoneId.of("America/Lima"));
  }
}
