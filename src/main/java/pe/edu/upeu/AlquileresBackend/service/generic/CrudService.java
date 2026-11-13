package pe.edu.upeu.AlquileresBackend.service.generic;

import java.util.List;

/** Contrato de la base PharmaBackend, conservando la separación por capas. */
public interface CrudService<REQ, RES, ID> {
  RES create(REQ request);

  RES update(ID id, REQ request);

  RES read(ID id);

  List<RES> readAll();

  void delete(ID id);
}
