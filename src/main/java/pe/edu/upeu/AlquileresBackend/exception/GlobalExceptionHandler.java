package pe.edu.upeu.AlquileresBackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upeu.AlquileresBackend.exception.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ErrorResponseDTO> error(
      HttpStatus s, String mensaje, HttpServletRequest r, Map<String, String> campos) {
    return ResponseEntity.status(s)
        .body(
            new ErrorResponseDTO(
                LocalDateTime.now(),
                s.value(),
                s.getReasonPhrase(),
                mensaje,
                r.getRequestURI(),
                campos));
  }

  @ExceptionHandler(RecursoNoEncontradoException.class)
  ResponseEntity<ErrorResponseDTO> inexistente(
      RecursoNoEncontradoException e, HttpServletRequest r) {
    return error(HttpStatus.NOT_FOUND, e.getMessage(), r, null);
  }

  @ExceptionHandler(ReglaNegocioException.class)
  ResponseEntity<ErrorResponseDTO> negocio(ReglaNegocioException e, HttpServletRequest r) {
    log.warn("Regla rechazada: {}", e.getMessage());
    return error(HttpStatus.CONFLICT, e.getMessage(), r, null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorResponseDTO> integridad(
      DataIntegrityViolationException e, HttpServletRequest r) {
    log.warn("Conflicto de integridad en {}", r.getRequestURI());
    return error(
        HttpStatus.CONFLICT, "Registro duplicado o referenciado por otros recursos", r, null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponseDTO> validacion(
      MethodArgumentNotValidException e, HttpServletRequest r) {
    Map<String, String> campos = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return error(HttpStatus.BAD_REQUEST, "Existen errores de validación", r, campos);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ErrorResponseDTO> tipo(
      MethodArgumentTypeMismatchException e, HttpServletRequest r) {
    return error(
        HttpStatus.BAD_REQUEST, "Parámetro inválido", r, Map.of(e.getName(), "Formato inválido"));
  }

  @ExceptionHandler(PeticionInvalidaException.class)
  ResponseEntity<ErrorResponseDTO> parametro(PeticionInvalidaException e, HttpServletRequest r) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage(), r, Map.of("parametros", e.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ErrorResponseDTO> json(HttpMessageNotReadableException e, HttpServletRequest r) {
    return error(
        HttpStatus.BAD_REQUEST,
        "JSON inválido: revise tipos, fechas y valores de enum",
        r,
        Map.of("body", "Cuerpo inválido"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponseDTO> inesperado(Exception e, HttpServletRequest r) {
    if (e instanceof ErrorResponse s) {
      HttpStatus estado = HttpStatus.resolve(s.getStatusCode().value());
      return error(
          estado == null ? HttpStatus.INTERNAL_SERVER_ERROR : estado,
          "Petición no procesable",
          r,
          null);
    }
    log.error("Error inesperado en {}", r.getRequestURI(), e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", r, null);
  }
}
