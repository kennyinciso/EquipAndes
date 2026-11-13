package pe.edu.upeu.AlquileresBackend.service.impl;

import java.math.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
public class EquipoServiceImpl implements EquipoService {
  private static final Logger log = LoggerFactory.getLogger(EquipoServiceImpl.class);
  private final EquipoRepository equipos;
  private final CategoriaRepository categorias;
  private final DetalleAlquilerRepository detalles;

  public EquipoServiceImpl(
      EquipoRepository equipos,
      CategoriaRepository categorias,
      DetalleAlquilerRepository detalles) {
    this.equipos = equipos;
    this.categorias = categorias;
    this.detalles = detalles;
  }

  private Equipo entidad(Long id) {
    return equipos
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado: " + id));
  }

  private void aplicar(Equipo e, EquipoRequestDTO r) {
    if (equipos.existsByCodigoIgnoreCaseAndIdNot(r.codigo(), e.getId() == null ? 0L : e.getId()))
      throw new ReglaNegocioException("Código de equipo duplicado");
    Categoria c =
        categorias
            .findById(r.categoriaId())
            .orElseThrow(
                () ->
                    new RecursoNoEncontradoException(
                        "Categoría no encontrada: " + r.categoriaId()));
    e.setCodigo(r.codigo());
    e.setNombre(r.nombre().strip());
    e.setTarifaDiaria(r.tarifaDiaria().setScale(2, RoundingMode.HALF_UP));
    e.setUnidadesDisponibles(r.unidadesDisponibles());
    e.setEstado(r.estado());
    e.setCategoria(c);
  }

  @Override
  @Transactional
  public EquipoResponseDTO create(EquipoRequestDTO r) {
    Equipo e = new Equipo();
    aplicar(e, r);
    e = equipos.saveAndFlush(e);
    log.info("Equipo creado: {}", e.getCodigo());
    return ApiMapper.equipo(e);
  }

  @Override
  @Transactional
  public EquipoResponseDTO update(Long id, EquipoRequestDTO r) {
    Equipo e =
        equipos
            .bloquear(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado: " + id));
    aplicar(e, r);
    equipos.flush();
    log.info("Equipo actualizado: {}", id);
    return ApiMapper.equipo(e);
  }

  @Override
  public EquipoResponseDTO read(Long id) {
    return ApiMapper.equipo(entidad(id));
  }

  @Override
  public List<EquipoResponseDTO> readAll() {
    return equipos.findAll(Sort.by("id")).stream().map(ApiMapper::equipo).toList();
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Equipo e =
        equipos
            .bloquear(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado: " + id));
    if (detalles.existsByEquipoId(id))
      throw new ReglaNegocioException("No se puede eliminar un equipo con alquileres");
    equipos.delete(e);
    equipos.flush();
    log.info("Equipo eliminado: {}", id);
  }

  @Override
  public List<EquipoResponseDTO> porCategoria(Long id) {
    if (!categorias.existsById(id))
      throw new RecursoNoEncontradoException("Categoría no encontrada: " + id);
    return equipos.findByCategoriaIdOrderByNombreAsc(id).stream().map(ApiMapper::equipo).toList();
  }

  @Override
  public List<EquipoResponseDTO> buscar(
      String nombre,
      Long categoriaId,
      BigDecimal tarifaMax,
      Boolean disponible,
      String orden,
      String dir) {
    String campo =
        switch (orden) {
          case "nombre" -> "nombre";
          case "tarifa" -> "tarifaDiaria";
          case "unidades" -> "unidadesDisponibles";
          default ->
              throw new PeticionInvalidaException("orden debe ser nombre, tarifa o unidades");
        };
    if (!"asc".equalsIgnoreCase(dir) && !"desc".equalsIgnoreCase(dir))
      throw new PeticionInvalidaException("dir debe ser asc o desc");
    if (tarifaMax != null && tarifaMax.signum() < 0)
      throw new PeticionInvalidaException("tarifaMax no puede ser negativa");
    if (categoriaId != null && categoriaId <= 0)
      throw new PeticionInvalidaException("categoriaId debe ser positivo");
    Specification<Equipo> filtro =
        (root, q, cb) -> {
          var condiciones = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (nombre != null && !nombre.isBlank()) {
            String literal =
                nombre
                    .strip()
                    .toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            condiciones.add(cb.like(cb.lower(root.get("nombre")), "%" + literal + "%", '\\'));
          }
          if (categoriaId != null)
            condiciones.add(cb.equal(root.get("categoria").get("id"), categoriaId));
          if (tarifaMax != null) condiciones.add(cb.le(root.get("tarifaDiaria"), tarifaMax));
          if (disponible != null)
            condiciones.add(
                disponible
                    ? cb.gt(root.get("unidadesDisponibles"), 0)
                    : cb.equal(root.get("unidadesDisponibles"), 0));
          return cb.and(condiciones.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return equipos
        .findAll(filtro, Sort.by(Sort.Direction.fromString(dir), campo).and(Sort.by("codigo")))
        .stream()
        .map(ApiMapper::equipo)
        .toList();
  }
}
