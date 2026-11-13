package pe.edu.upeu.AlquileresBackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.AlquileresBackend.enums.*;

@Entity
@Table(
    name = "detalle_alquileres",
    uniqueConstraints = @UniqueConstraint(columnNames = {"alquiler_id", "equipo_id"}))
@Getter
@Setter
public class DetalleAlquiler extends Auditable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "alquiler_id", nullable = false)
  private Alquiler alquiler;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "equipo_id", nullable = false)
  private Equipo equipo;

  @Column(nullable = false)
  private Integer cantidad;

  @Column(name = "tarifa_diaria", nullable = false, precision = 10, scale = 2)
  private BigDecimal tarifaDiaria;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal importe;
}
