package pe.edu.upeu.AlquileresBackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.service.service.*;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "categorias")
public class CategoriaController {
  private final CategoriaService servicio;
  private final EquipoService equipoService;

  public CategoriaController(CategoriaService servicio, EquipoService equipoService) {
    this.servicio = servicio;
    this.equipoService = equipoService;
  }

  @GetMapping
  @Operation(summary = "Listar recursos")
  public ResponseEntity<List<CategoriaResponseDTO>> listar() {
    return ResponseEntity.ok(servicio.readAll());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Consultar recurso por ID")
  public ResponseEntity<CategoriaResponseDTO> leer(@PathVariable Long id) {
    return ResponseEntity.ok(servicio.read(id));
  }

  @PostMapping
  @Operation(summary = "Registrar recurso")
  public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO r) {
    var creado = servicio.create(r);
    return ResponseEntity.created(URI.create("/api/v1/categorias/" + creado.id())).body(creado);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar recurso")
  public ResponseEntity<CategoriaResponseDTO> actualizar(
      @PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO r) {
    return ResponseEntity.ok(servicio.update(id, r));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Eliminar recurso")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    servicio.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/equipos")
  @Operation(summary = "Listar equipos de una categoría")
  public ResponseEntity<List<EquipoResponseDTO>> equipos(@PathVariable Long id) {
    return ResponseEntity.ok(equipoService.porCategoria(id));
  }
}
