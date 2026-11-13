package pe.edu.upeu.AlquileresBackend.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import pe.edu.upeu.AlquileresBackend.enums.*;

public record ClienteRequestDTO(
    @NotBlank @Pattern(regexp = "^CLI-\\d{3}$") String codigo,
    @NotBlank @Pattern(regexp = "^(\\d{8}|\\d{11})$") String documento,
    @NotBlank @Size(min = 3, max = 150) String nombre,
    @NotBlank @Email @Size(max = 150) String email,
    @Size(max = 20) String telefono,
    @NotNull TipoCliente tipo,
    @NotNull Boolean estado) {}
