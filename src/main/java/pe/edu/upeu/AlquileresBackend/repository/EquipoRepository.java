package pe.edu.upeu.AlquileresBackend.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.AlquileresBackend.entity.Equipo;

public interface EquipoRepository
    extends JpaRepository<Equipo, Long>, JpaSpecificationExecutor<Equipo> {
  boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

  boolean existsByCategoriaId(Long categoriaId);

  List<Equipo> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Equipo e where e.id=:id")
  Optional<Equipo> bloquear(@Param("id") Long id);
}
