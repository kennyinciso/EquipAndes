package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record EquipoRequestDTO(
    @NotBlank @Pattern(regexp = "^EQP-\\d{3}$") String codigo,
    @NotBlank @Size(min = 3, max = 150) String nombre,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 8, fraction = 2)
        BigDecimal tarifaDiaria,
    @NotNull @Min(0) Integer unidadesDisponibles,
    @NotNull Boolean estado,
    @NotNull @Positive Long categoriaId) {}
