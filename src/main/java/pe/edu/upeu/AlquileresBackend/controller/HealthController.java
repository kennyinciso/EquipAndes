package pe.edu.upeu.AlquileresBackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.AlquileresBackend.dto.HealthResponseDTO;
import pe.edu.upeu.AlquileresBackend.service.service.HealthService;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
  private final HealthService servicio;

  public HealthController(HealthService servicio) {
    this.servicio = servicio;
  }

  @GetMapping
  @Operation(summary = "Comprobar conexión real a la base de datos; 503 si no responde")
  public ResponseEntity<HealthResponseDTO> health() {
    var h = servicio.comprobar();
    return ResponseEntity.status("UP".equals(h.database()) ? 200 : 503).body(h);
  }
}
