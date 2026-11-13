package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record DetalleAlquilerResponseDTO(
    Long id,
    Long equipoId,
    String equipoCodigo,
    String equipoNombre,
    Integer cantidad,
    BigDecimal tarifaDiaria,
    BigDecimal importe) {}
