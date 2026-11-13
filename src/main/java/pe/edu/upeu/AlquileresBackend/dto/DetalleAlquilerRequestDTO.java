package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record DetalleAlquilerRequestDTO(
    @NotNull @Positive Long equipoId, @NotNull @Min(1) @Max(1000000) Integer cantidad) {}
