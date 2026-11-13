package pe.edu.upeu.AlquileresBackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class Auditable {
  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_modificacion")
  private LocalDateTime fechaModificacion;

  @PrePersist
  protected void crearFecha() {
    fechaCreacion = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
  }

  @PreUpdate
  protected void modificarFecha() {
    fechaModificacion = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
  }
}
