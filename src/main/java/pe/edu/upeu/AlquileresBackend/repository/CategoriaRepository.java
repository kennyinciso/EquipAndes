package pe.edu.upeu.AlquileresBackend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.AlquileresBackend.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
  @Query(
      "select count(c) from Categoria c where lower(replace(c.nombre, ' ', '')) = :nombre and (:id"
          + " is null or c.id <> :id)")
  long contarNombre(@Param("nombre") String nombre, @Param("id") Long id);
}
