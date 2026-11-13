package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record CategoriaRequestDTO(
    @NotBlank @Size(min = 3, max = 60) String nombre,
    @Size(max = 200) String descripcion,
    @NotNull Boolean estado) {}
