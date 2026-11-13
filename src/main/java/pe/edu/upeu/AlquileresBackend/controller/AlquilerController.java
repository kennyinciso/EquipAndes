package pe.edu.upeu.AlquileresBackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.service.service.AlquilerService;

@RestController
@RequestMapping("/api/v1/alquileres")
public class AlquilerController {
  private final AlquilerService servicio;

  public AlquilerController(AlquilerService servicio) {
    this.servicio = servicio;
  }

  @PostMapping
  @Operation(summary = "Registrar alquiler atómico con sus detalles")
  public ResponseEntity<AlquilerResponseDTO> crear(@Valid @RequestBody AlquilerRequestDTO r) {
    var a = servicio.crear(r);
    return ResponseEntity.created(URI.create("/api/v1/alquileres/" + a.id())).body(a);
  }

  @GetMapping
  @Operation(summary = "Listar alquileres con detalles")
  public ResponseEntity<List<AlquilerResponseDTO>> listar() {
    return ResponseEntity.ok(servicio.listar());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Consultar alquiler con detalles")
  public ResponseEntity<AlquilerResponseDTO> buscar(@PathVariable Long id) {
    return ResponseEntity.ok(servicio.buscar(id));
  }

  @PatchMapping("/{id}/devolver")
  @Operation(summary = "Devolver alquiler y reponer unidades una sola vez")
  public ResponseEntity<AlquilerResponseDTO> devolver(@PathVariable Long id) {
    return ResponseEntity.ok(servicio.devolver(id));
  }
}
