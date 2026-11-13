package pe.edu.upeu.AlquileresBackend.service.impl;

import java.math.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.entity.*;
import pe.edu.upeu.AlquileresBackend.exception.*;
import pe.edu.upeu.AlquileresBackend.mapper.ApiMapper;
import pe.edu.upeu.AlquileresBackend.repository.*;
import pe.edu.upeu.AlquileresBackend.service.service.*;

@Service
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {
  private static final Logger log = LoggerFactory.getLogger(CategoriaServiceImpl.class);
  private final CategoriaRepository categorias;
  private final EquipoRepository equipos;

  public CategoriaServiceImpl(CategoriaRepository categorias, EquipoRepository equipos) {
    this.categorias = categorias;
    this.equipos = equipos;
  }

  private Categoria entidad(Long id) {
    return categorias
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + id));
  }

  private void aplicar(Categoria c, CategoriaRequestDTO r) {
    String nombre = r.nombre().strip().replaceAll("\\s+", " ");
    if (nombre.length() < 3)
      throw new PeticionInvalidaException(
          "nombre debe tener al menos 3 caracteres sin espacios externos");
    String clave = nombre.replace(" ", "").toLowerCase(Locale.ROOT);
    if (categorias.contarNombre(clave, c.getId()) > 0)
      throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
    c.setNombre(nombre);
    c.setDescripcion(r.descripcion());
    c.setEstado(r.estado());
  }

  @Override
  @Transactional
  public CategoriaResponseDTO create(CategoriaRequestDTO r) {
    Categoria c = new Categoria();
    aplicar(c, r);
    c = categorias.saveAndFlush(c);
    log.info("Categoría creada: {}", c.getId());
    return ApiMapper.categoria(c);
  }

  @Override
  @Transactional
  public CategoriaResponseDTO update(Long id, CategoriaRequestDTO r) {
    Categoria c = entidad(id);
    aplicar(c, r);
    categorias.flush();
    log.info("Categoría actualizada: {}", id);
    return ApiMapper.categoria(c);
  }

  @Override
  public CategoriaResponseDTO read(Long id) {
    return ApiMapper.categoria(entidad(id));
  }

  @Override
  public List<CategoriaResponseDTO> readAll() {
    return categorias.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(ApiMapper::categoria)
        .toList();
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Categoria c = entidad(id);
    if (equipos.existsByCategoriaId(id))
      throw new ReglaNegocioException("No se puede eliminar una categoría con equipos");
    categorias.delete(c);
    categorias.flush();
    log.info("Categoría eliminada: {}", id);
  }
}
