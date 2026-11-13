package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record AlquilerRequestDTO(
    @NotNull @Positive Long clienteId,
    @NotNull LocalDate fechaInicio,
    @NotNull @Min(1) @Max(30) Integer dias,
    @Size(max = 150) String lugarEvento,
    @NotEmpty @Size(max = 100) List<@NotNull @Valid DetalleAlquilerRequestDTO> detalles) {}
