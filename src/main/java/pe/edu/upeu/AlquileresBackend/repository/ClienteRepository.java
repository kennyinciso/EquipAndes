package pe.edu.upeu.AlquileresBackend.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.AlquileresBackend.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
  boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

  boolean existsByDocumentoAndIdNot(String documento, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Cliente c where c.id=:id")
  Optional<Cliente> bloquear(@Param("id") Long id);
}
