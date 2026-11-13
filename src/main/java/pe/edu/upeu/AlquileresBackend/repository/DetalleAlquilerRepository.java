package pe.edu.upeu.AlquileresBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.AlquileresBackend.entity.DetalleAlquiler;

public interface DetalleAlquilerRepository extends JpaRepository<DetalleAlquiler, Long> {
  boolean existsByEquipoId(Long equipoId);
}
