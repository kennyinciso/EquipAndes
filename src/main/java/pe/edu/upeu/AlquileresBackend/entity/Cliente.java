package pe.edu.upeu.AlquileresBackend.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.AlquileresBackend.enums.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente extends Auditable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 7)
  private String codigo;

  @Column(nullable = false, unique = true, length = 11)
  private String documento;

  @Column(nullable = false, length = 150)
  private String nombre;

  @Column(nullable = false, length = 150)
  private String email;

  @Column(length = 20)
  private String telefono;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TipoCliente tipo;

  @Column(nullable = false)
  private Boolean estado = true;
}
