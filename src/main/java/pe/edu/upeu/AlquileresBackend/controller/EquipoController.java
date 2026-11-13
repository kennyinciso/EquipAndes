package pe.edu.upeu.AlquileresBackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.service.service.*;

@RestController
@RequestMapping("/api/v1/equipos")
@Tag(name = "equipos")
public class EquipoController {
  private final EquipoService servicio;

  public EquipoController(EquipoService servicio) {
    this.servicio = servicio;
  }

  @GetMapping
  @Operation(summary = "Listar recursos")
  public ResponseEntity<List<EquipoResponseDTO>> listar() {
    return ResponseEntity.ok(servicio.readAll());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Consultar recurso por ID")
  public ResponseEntity<EquipoResponseDTO> leer(@PathVariable Long id) {
    return ResponseEntity.ok(servicio.read(id));
  }

  @PostMapping
  @Operation(summary = "Registrar recurso")
  public ResponseEntity<EquipoResponseDTO> crear(@Valid @RequestBody EquipoRequestDTO r) {
    var creado = servicio.create(r);
    return ResponseEntity.created(URI.create("/api/v1/equipos/" + creado.id())).body(creado);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar recurso")
  public ResponseEntity<EquipoResponseDTO> actualizar(
      @PathVariable Long id, @Valid @RequestBody EquipoRequestDTO r) {
    return ResponseEntity.ok(servicio.update(id, r));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Eliminar recurso")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    servicio.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/buscar")
  @Operation(summary = "Buscar equipos con filtros combinables, sin paginación")
  public ResponseEntity<List<EquipoResponseDTO>> buscar(
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) Long categoriaId,
      @RequestParam(required = false) BigDecimal tarifaMax,
      @RequestParam(required = false) Boolean disponible,
      @RequestParam(defaultValue = "nombre") String orden,
      @RequestParam(defaultValue = "asc") String dir) {
    return ResponseEntity.ok(
        servicio.buscar(nombre, categoriaId, tarifaMax, disponible, orden, dir));
  }
}
