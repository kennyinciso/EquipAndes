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
@RequestMapping("/api/v1/clientes")
@Tag(name = "clientes")
public class ClienteController {
  private final ClienteService servicio;

  public ClienteController(ClienteService servicio) {
    this.servicio = servicio;
  }

  @GetMapping
  @Operation(summary = "Listar recursos")
  public ResponseEntity<List<ClienteResponseDTO>> listar() {
    return ResponseEntity.ok(servicio.readAll());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Consultar recurso por ID")
  public ResponseEntity<ClienteResponseDTO> leer(@PathVariable Long id) {
    return ResponseEntity.ok(servicio.read(id));
  }

  @PostMapping
  @Operation(summary = "Registrar recurso")
  public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO r) {
    var creado = servicio.create(r);
    return ResponseEntity.created(URI.create("/api/v1/clientes/" + creado.id())).body(creado);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar recurso")
  public ResponseEntity<ClienteResponseDTO> actualizar(
      @PathVariable Long id, @Valid @RequestBody ClienteRequestDTO r) {
    return ResponseEntity.ok(servicio.update(id, r));
  }
}
