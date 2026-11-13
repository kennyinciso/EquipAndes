package pe.edu.upeu.AlquileresBackend.mapper;

import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.entity.*;

/** DTO planos: el servicio mapea dentro de su transacción, sin exponer entidades LAZY. */
public final class ApiMapper {
  private ApiMapper() {}

  public static CategoriaResponseDTO categoria(Categoria c) {
    return new CategoriaResponseDTO(
        c.getId(),
        c.getNombre(),
        c.getDescripcion(),
        c.getEstado(),
        c.getFechaCreacion(),
        c.getFechaModificacion());
  }

  public static EquipoResponseDTO equipo(Equipo e) {
    return new EquipoResponseDTO(
        e.getId(),
        e.getCodigo(),
        e.getNombre(),
        e.getTarifaDiaria(),
        e.getUnidadesDisponibles(),
        e.getEstado(),
        e.getCategoria().getId(),
        e.getCategoria().getNombre(),
        e.getFechaCreacion(),
        e.getFechaModificacion());
  }

  public static ClienteResponseDTO cliente(Cliente c) {
    return new ClienteResponseDTO(
        c.getId(),
        c.getCodigo(),
        c.getDocumento(),
        c.getNombre(),
        c.getEmail(),
        c.getTelefono(),
        c.getTipo(),
        c.getEstado(),
        c.getFechaCreacion(),
        c.getFechaModificacion());
  }

  public static AlquilerResponseDTO alquiler(Alquiler a) {
    return new AlquilerResponseDTO(
        a.getId(),
        a.getFechaRegistro(),
        a.getFechaInicio(),
        a.getDias(),
        a.getCliente().getId(),
        a.getCliente().getNombre(),
        a.getLugarEvento(),
        a.getEstado(),
        a.getFechaDevolucion(),
        a.getTotalUnidades(),
        a.getSubtotal(),
        a.getDescuento(),
        a.getMontoTotal(),
        a.getDetalles().stream()
            .map(
                d ->
                    new DetalleAlquilerResponseDTO(
                        d.getId(),
                        d.getEquipo().getId(),
                        d.getEquipo().getCodigo(),
                        d.getEquipo().getNombre(),
                        d.getCantidad(),
                        d.getTarifaDiaria(),
                        d.getImporte()))
            .toList());
  }
}
