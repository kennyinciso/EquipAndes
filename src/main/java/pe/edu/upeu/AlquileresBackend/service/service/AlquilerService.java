package pe.edu.upeu.AlquileresBackend.service.service;

import java.util.List;
import pe.edu.upeu.AlquileresBackend.dto.*;

public interface AlquilerService {
  AlquilerResponseDTO crear(AlquilerRequestDTO request);

  AlquilerResponseDTO buscar(Long id);

  List<AlquilerResponseDTO> listar();

  AlquilerResponseDTO devolver(Long id);
}
