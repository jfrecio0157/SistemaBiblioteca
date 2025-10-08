package com.example.biblioteca.expection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RevistaNoEncontradoException extends RuntimeException {
    public RevistaNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
