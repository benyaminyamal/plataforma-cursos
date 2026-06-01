package com.plataforma.cursos.exception;

/**
 * Se lanza cuando no se encuentra un recurso solicitado (por ejemplo, un curso inexistente).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
