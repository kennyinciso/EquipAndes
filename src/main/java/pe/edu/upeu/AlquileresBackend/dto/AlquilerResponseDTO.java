package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record AlquilerResponseDTO(
    Long id,
    LocalDateTime fechaRegistro,
    LocalDate fechaInicio,
    Integer dias,
    Long clienteId,
    String clienteNombre,
    String lugarEvento,
    EstadoAlquiler estado,
    LocalDateTime fechaDevolucion,
    Integer totalUnidades,
    BigDecimal subtotal,
    BigDecimal descuento,
    BigDecimal montoTotal,
    List<DetalleAlquilerResponseDTO> detalles) {}
