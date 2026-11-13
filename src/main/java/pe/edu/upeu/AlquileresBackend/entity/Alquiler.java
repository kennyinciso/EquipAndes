package pe.edu.upeu.AlquileresBackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.AlquileresBackend.enums.*;

@Entity
@Table(name = "alquileres")
@Getter
@Setter
public class Alquiler extends Auditable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "fecha_registro", nullable = false, updatable = false)
  private LocalDateTime fechaRegistro;

  @Column(name = "fecha_inicio", nullable = false)
  private LocalDate fechaInicio;

  @Column(nullable = false)
  private Integer dias;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  @Column(name = "lugar_evento", length = 150)
  private String lugarEvento;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private EstadoAlquiler estado = EstadoAlquiler.VIGENTE;

  @Column(name = "fecha_devolucion")
  private LocalDateTime fechaDevolucion;

  @Column(name = "total_unidades", nullable = false)
  private Integer totalUnidades;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal descuento;

  @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal montoTotal;

  @OneToMany(mappedBy = "alquiler", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DetalleAlquiler> detalles = new ArrayList<>();

  public void agregarDetalle(DetalleAlquiler detalle) {
    detalles.add(detalle);
    detalle.setAlquiler(this);
  }

  @PrePersist
  private void registrarFecha() {
    fechaRegistro = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
  }
}
