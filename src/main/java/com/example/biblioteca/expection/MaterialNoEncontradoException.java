package com.example.biblioteca.expection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MaterialNoEncontradoException extends RuntimeException {
    public MaterialNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
