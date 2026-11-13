package pe.edu.upeu.AlquileresBackend.service.service;

import java.math.BigDecimal;
import java.util.List;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.service.generic.CrudService;

public interface EquipoService extends CrudService<EquipoRequestDTO, EquipoResponseDTO, Long> {
  List<EquipoResponseDTO> porCategoria(Long categoriaId);

  List<EquipoResponseDTO> buscar(
      String nombre,
      Long categoriaId,
      BigDecimal tarifaMax,
      Boolean disponible,
      String orden,
      String dir);
}
