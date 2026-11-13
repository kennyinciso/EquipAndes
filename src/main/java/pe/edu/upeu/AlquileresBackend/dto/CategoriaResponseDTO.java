package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record CategoriaResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    Boolean estado,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion) {}
