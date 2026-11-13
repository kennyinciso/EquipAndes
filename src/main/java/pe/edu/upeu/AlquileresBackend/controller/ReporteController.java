package pe.edu.upeu.AlquileresBackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.AlquileresBackend.dto.EquipoAlquiladoDTO;
import pe.edu.upeu.AlquileresBackend.service.service.ReporteService;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {
  private final ReporteService servicio;

  public ReporteController(ReporteService servicio) {
    this.servicio = servicio;
  }

  @GetMapping("/equipos-alquilados")
  @Operation(summary = "Unidades e importe bruto por equipo y mes de registro")
  public ResponseEntity<List<EquipoAlquiladoDTO>> equipos(
      @RequestParam String periodo, @RequestParam(required = false) Long categoriaId) {
    return ResponseEntity.ok(servicio.equiposAlquilados(periodo, categoriaId));
  }
}
