package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record EquipoResponseDTO(
    Long id,
    String codigo,
    String nombre,
    BigDecimal tarifaDiaria,
    Integer unidadesDisponibles,
    Boolean estado,
    Long categoriaId,
    String categoriaNombre,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion) {}
