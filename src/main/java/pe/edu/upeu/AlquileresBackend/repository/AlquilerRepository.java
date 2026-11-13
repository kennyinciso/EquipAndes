package pe.edu.upeu.AlquileresBackend.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.AlquileresBackend.dto.EquipoAlquiladoDTO;
import pe.edu.upeu.AlquileresBackend.entity.Alquiler;
import pe.edu.upeu.AlquileresBackend.enums.EstadoAlquiler;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {
  long countByClienteIdAndEstado(Long clienteId, EstadoAlquiler estado);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Alquiler a where a.id=:id")
  Optional<Alquiler> bloquear(@Param("id") Long id);

  @Query(
      """
      select d.equipo.codigo as codigo, d.equipo.nombre as equipo,
             sum(d.cantidad) as unidadesAlquiladas, sum(d.importe) as importeBruto
      from DetalleAlquiler d
      where d.alquiler.fechaRegistro >= :inicio and d.alquiler.fechaRegistro < :fin
        and (:categoriaId is null or d.equipo.categoria.id=:categoriaId)
      group by d.equipo.id, d.equipo.codigo, d.equipo.nombre
      order by sum(d.cantidad) desc, d.equipo.codigo asc
      """)
  List<EquipoAlquiladoDTO> reportar(
      @Param("inicio") LocalDateTime inicio,
      @Param("fin") LocalDateTime fin,
      @Param("categoriaId") Long categoriaId);
}
