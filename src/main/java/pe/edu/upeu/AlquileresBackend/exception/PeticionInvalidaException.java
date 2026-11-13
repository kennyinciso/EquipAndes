package pe.edu.upeu.AlquileresBackend.exception;

public class PeticionInvalidaException extends RuntimeException {
  public PeticionInvalidaException(String mensaje) {
    super(mensaje);
  }
}
