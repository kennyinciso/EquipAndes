package pe.edu.upeu.AlquileresBackend.dto;

import java.math.BigDecimal;

/** Proyección de la consulta JPQL agregada; importe bruto anterior al descuento. */
public interface EquipoAlquiladoDTO {
  String getCodigo();

  String getEquipo();

  Long getUnidadesAlquiladas();

  BigDecimal getImporteBruto();
}
