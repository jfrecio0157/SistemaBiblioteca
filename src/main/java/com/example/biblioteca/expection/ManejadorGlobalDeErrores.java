package com.example.biblioteca.expection;

import com.example.biblioteca.model.MaterialBiblioteca;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<String> manejarUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<String> manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LibroPrestadoException.class)
    public ResponseEntity<String> manejarLibroPrestado(LibroPrestadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AutorNoEncontradoException.class)
    public ResponseEntity<String> manejarAutorNoEncontrado(AutorNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<String> manejarOperacionNoPermitida(OperacionNoPermitidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RevistaNoEncontradoException.class)
    public ResponseEntity<String> manejarRevistaNoEncontrado(RevistaNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RevistaPrestadaException.class)
    public ResponseEntity<String> manejarRevistaPrestada(RevistaPrestadaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MaterialNoEncontradoException.class)
    public ResponseEntity<String> manejarMaterialNoEncontrado(MaterialNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
