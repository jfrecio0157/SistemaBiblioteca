package com.example.biblioteca.expection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RevistaPrestadaException extends RuntimeException {
    public RevistaPrestadaException(String mensaje) {
        super(mensaje);
    }
}
