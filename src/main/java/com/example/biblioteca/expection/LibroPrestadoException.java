package com.example.biblioteca.expection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LibroPrestadoException extends RuntimeException {
    public LibroPrestadoException(String mensaje) {
        super(mensaje);
    }
}
