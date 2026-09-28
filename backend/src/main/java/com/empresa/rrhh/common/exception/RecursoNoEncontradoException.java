package com.empresa.rrhh.common.exception;

// excepción propia (en vez de una genérica de Java) para que GlobalExceptionHandler
// pueda distinguirla de cualquier otro error y traducirla siempre a 404
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
