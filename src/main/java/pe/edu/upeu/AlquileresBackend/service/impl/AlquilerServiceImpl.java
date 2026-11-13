package pe.edu.upeu.AlquileresBackend.service.impl;

import java.math.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.entity.*;
import pe.edu.upeu.AlquileresBackend.enums.EstadoAlquiler;
import pe.edu.upeu.AlquileresBackend.exception.*;
import pe.edu.upeu.AlquileresBackend.mapper.ApiMapper;
import pe.edu.upeu.AlquileresBackend.repository.*;
import pe.edu.upeu.AlquileresBackend.service.service.*;

@Service
@Transactional(readOnly = true)
public class AlquilerServiceImpl implements AlquilerService {
  private static final Logger log = LoggerFactory.getLogger(AlquilerServiceImpl.class);
  private final AlquilerRepository alquileres;
  private final ClienteRepository clientes;
  private final EquipoRepository equipos;
  private final Clock reloj;

  public AlquilerServiceImpl(
      AlquilerRepository alquileres,
      ClienteRepository clientes,
      EquipoRepository equipos,
      Clock reloj) {
    this.alquileres = alquileres;
    this.clientes = clientes;
    this.equipos = equipos;
    this.reloj = reloj;
  }

  private ReglaNegocioException rechazar(String mensaje) {
    log.warn("{}", mensaje);
    return new ReglaNegocioException(mensaje);
  }

  private Alquiler entidad(Long id) {
    return alquileres
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Alquiler no encontrado: " + id));
  }

  @Override
  @Transactional
  public AlquilerResponseDTO crear(AlquilerRequestDTO r) {
    // El bloqueo del cliente serializa altas concurrentes y protege el límite RN-03.
    Cliente cliente =
        clientes
            .bloquear(r.clienteId())
            .orElseThrow(
                () -> new RecursoNoEncontradoException("Cliente no encontrado: " + r.clienteId()));
    LocalDate hoy = LocalDate.now(reloj);
    if (!Boolean.TRUE.equals(cliente.getEstado()))
      throw rechazar("RN-01: el cliente está inactivo");
    if (r.fechaInicio().isBefore(hoy) || r.fechaInicio().isAfter(hoy.plusDays(60)))
      throw rechazar("RN-01: el inicio debe estar entre hoy y los próximos 60 días");
    if (alquileres.countByClienteIdAndEstado(cliente.getId(), EstadoAlquiler.VIGENTE) >= 2)
      throw rechazar("RN-03: el cliente ya tiene 2 alquileres vigentes");
    Set<Long> ids = new TreeSet<>();
    for (var linea : r.detalles())
      if (!ids.add(linea.equipoId())) throw rechazar("RN-04: el alquiler no puede repetir equipos");
    // Orden fijo de bloqueos para evitar vender la misma disponibilidad en paralelo.
    Map<Long, Equipo> bloqueados = new HashMap<>();
    for (Long id : ids)
      bloqueados.put(
          id,
          equipos
              .bloquear(id)
              .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado: " + id)));
    Alquiler a = new Alquiler();
    a.setCliente(cliente);
    a.setFechaInicio(r.fechaInicio());
    a.setDias(r.dias());
    a.setLugarEvento(r.lugarEvento());
    BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
    int unidades = 0;
    for (var linea : r.detalles()) {
      Equipo e = bloqueados.get(linea.equipoId());
      if (!Boolean.TRUE.equals(e.getEstado()))
        throw rechazar("RN-01: equipo inactivo: " + e.getCodigo());
      if (linea.cantidad() > e.getUnidadesDisponibles())
        throw rechazar("RN-02: unidades insuficientes de " + e.getCodigo());
      DetalleAlquiler d = new DetalleAlquiler();
      d.setEquipo(e);
      d.setCantidad(linea.cantidad());
      d.setTarifaDiaria(e.getTarifaDiaria().setScale(2, RoundingMode.HALF_UP));
      d.setImporte(
          d.getTarifaDiaria()
              .multiply(BigDecimal.valueOf(linea.cantidad()))
              .multiply(BigDecimal.valueOf(r.dias()))
              .setScale(2, RoundingMode.HALF_UP));
      subtotal = subtotal.add(d.getImporte());
      unidades += linea.cantidad();
      e.setUnidadesDisponibles(e.getUnidadesDisponibles() - linea.cantidad());
      a.agregarDetalle(d);
    }
    BigDecimal descuento =
        (r.dias() >= 7 ? subtotal.multiply(new BigDecimal("0.15")) : BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);
    if (subtotal.compareTo(new BigDecimal("9999999999.99")) > 0)
      throw rechazar("El subtotal supera la capacidad monetaria del alquiler");
    a.setTotalUnidades(unidades);
    a.setSubtotal(subtotal);
    a.setDescuento(descuento);
    a.setMontoTotal(subtotal.subtract(descuento));
    a = alquileres.saveAndFlush(a);
    log.info(
        "Alquiler registrado: id={}, cliente={}, total={}",
        a.getId(),
        cliente.getCodigo(),
        a.getMontoTotal());
    return ApiMapper.alquiler(a);
  }

  @Override
  public AlquilerResponseDTO buscar(Long id) {
    return ApiMapper.alquiler(entidad(id));
  }

  @Override
  public List<AlquilerResponseDTO> listar() {
    return alquileres.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(ApiMapper::alquiler)
        .toList();
  }

  @Override
  @Transactional
  public AlquilerResponseDTO devolver(Long id) {
    Alquiler a =
        alquileres
            .bloquear(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Alquiler no encontrado: " + id));
    if (a.getEstado() != EstadoAlquiler.VIGENTE)
      throw rechazar("RN-02: solo se puede devolver un alquiler VIGENTE");
    List<DetalleAlquiler> ordenados =
        a.getDetalles().stream().sorted(Comparator.comparing(d -> d.getEquipo().getId())).toList();
    for (DetalleAlquiler d : ordenados) {
      Equipo e =
          equipos
              .bloquear(d.getEquipo().getId())
              .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado"));
      if ((long) e.getUnidadesDisponibles() + d.getCantidad() > Integer.MAX_VALUE)
        throw rechazar("La devolución supera el límite de unidades del equipo");
      e.setUnidadesDisponibles(e.getUnidadesDisponibles() + d.getCantidad());
    }
    a.setEstado(EstadoAlquiler.DEVUELTO);
    a.setFechaDevolucion(LocalDateTime.now(reloj));
    alquileres.flush();
    log.info("Alquiler devuelto: {}", id);
    return ApiMapper.alquiler(a);
  }
}
