package pe.edu.upeu.AlquileresBackend.service.impl;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.AlquileresBackend.dto.*;
import pe.edu.upeu.AlquileresBackend.entity.*;
import pe.edu.upeu.AlquileresBackend.exception.*;
import pe.edu.upeu.AlquileresBackend.repository.*;
import pe.edu.upeu.AlquileresBackend.service.service.*;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
  private static final Logger log = LoggerFactory.getLogger(ReporteServiceImpl.class);
  private final AlquilerRepository alquileres;

  public ReporteServiceImpl(AlquilerRepository alquileres) {
    this.alquileres = alquileres;
  }

  @Override
  public List<EquipoAlquiladoDTO> equiposAlquilados(String periodo, Long categoriaId) {
    if (periodo == null || !periodo.matches("\\d{4}-\\d{2}"))
      throw new PeticionInvalidaException("periodo debe tener formato AAAA-MM");
    if (categoriaId != null && categoriaId <= 0)
      throw new PeticionInvalidaException("categoriaId debe ser positivo");
    YearMonth mes;
    try {
      mes = YearMonth.parse(periodo);
    } catch (DateTimeParseException e) {
      throw new PeticionInvalidaException("Periodo inválido");
    }
    log.info("Reporte equipos alquilados: periodo={}, categoriaId={}", periodo, categoriaId);
    return alquileres.reportar(
        mes.atDay(1).atStartOfDay(), mes.plusMonths(1).atDay(1).atStartOfDay(), categoriaId);
  }
}
