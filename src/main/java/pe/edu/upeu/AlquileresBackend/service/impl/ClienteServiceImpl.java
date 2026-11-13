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
public class ClienteServiceImpl implements ClienteService {
  private static final Logger log = LoggerFactory.getLogger(ClienteServiceImpl.class);
  private final ClienteRepository clientes;

  public ClienteServiceImpl(ClienteRepository clientes) {
    this.clientes = clientes;
  }

  private Cliente entidad(Long id) {
    return clientes
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
  }

  private void aplicar(Cliente c, ClienteRequestDTO r) {
    Long id = c.getId() == null ? 0L : c.getId();
    if (clientes.existsByCodigoIgnoreCaseAndIdNot(r.codigo(), id))
      throw new ReglaNegocioException("Código de cliente duplicado");
    if (clientes.existsByDocumentoAndIdNot(r.documento(), id))
      throw new ReglaNegocioException("Documento de cliente duplicado");
    c.setCodigo(r.codigo());
    c.setDocumento(r.documento());
    c.setNombre(r.nombre().strip());
    c.setEmail(r.email());
    c.setTelefono(r.telefono());
    c.setTipo(r.tipo());
    c.setEstado(r.estado());
  }

  @Override
  @Transactional
  public ClienteResponseDTO create(ClienteRequestDTO r) {
    Cliente c = new Cliente();
    aplicar(c, r);
    c = clientes.saveAndFlush(c);
    log.info("Cliente creado: {}", c.getCodigo());
    return ApiMapper.cliente(c);
  }

  @Override
  @Transactional
  public ClienteResponseDTO update(Long id, ClienteRequestDTO r) {
    Cliente c =
        clientes
            .bloquear(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
    aplicar(c, r);
    clientes.flush();
    log.info("Cliente actualizado: {}", id);
    return ApiMapper.cliente(c);
  }

  @Override
  public ClienteResponseDTO read(Long id) {
    return ApiMapper.cliente(entidad(id));
  }

  @Override
  public List<ClienteResponseDTO> readAll() {
    return clientes.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(ApiMapper::cliente)
        .toList();
  }

  @Override
  @Transactional
  public void delete(Long id) {
    clientes.delete(entidad(id));
    clientes.flush();
    log.info("Cliente eliminado: {}", id);
  }
}
