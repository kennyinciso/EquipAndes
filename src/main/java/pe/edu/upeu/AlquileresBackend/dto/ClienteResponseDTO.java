package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record ClienteResponseDTO(
    Long id,
    String codigo,
    String documento,
    String nombre,
    String email,
    String telefono,
    TipoCliente tipo,
    Boolean estado,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion) {}
