package pe.edu.upeu.AlquileresBackend.service.service;

import java.util.List;
import pe.edu.upeu.AlquileresBackend.dto.EquipoAlquiladoDTO;

public interface ReporteService {
  List<EquipoAlquiladoDTO> equiposAlquilados(String periodo, Long categoriaId);
}
