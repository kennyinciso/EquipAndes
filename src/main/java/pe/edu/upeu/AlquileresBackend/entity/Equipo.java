package pe.edu.upeu.AlquileresBackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.AlquileresBackend.enums.*;

@Entity
@Table(name = "equipos")
@Getter
@Setter
public class Equipo extends Auditable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 7)
  private String codigo;

  @Column(nullable = false, length = 150)
  private String nombre;

  @Column(name = "tarifa_diaria", nullable = false, precision = 10, scale = 2)
  private BigDecimal tarifaDiaria;

  @Column(name = "unidades_disponibles", nullable = false)
  private Integer unidadesDisponibles;

  @Column(nullable = false)
  private Boolean estado = true;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "categoria_id", nullable = false)
  private Categoria categoria;
}
