package com.example.biblioteca.service;

import com.example.biblioteca.model.Libro;
import com.example.biblioteca.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;


    @Autowired
    private Validator validator;

    public LibroService(LibroRepository libroRepository, Validator validator) {
        this.libroRepository = libroRepository;
        this.validator = validator;
    }


    public void insertarLibro (Libro libro){
        Set<ConstraintViolation<Libro>> violations = validator.validate(libro);

        //Si el conjunto violations está vacío, no se lanza excepción y el libro se guarda
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        libroRepository.save(libro);
    }

    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    public Libro buscarLibroByIsbn (String isbn){
        return libroRepository.findLibroConAutores(isbn).orElse(null);
    }

    public Libro buscarLibroByTitulo (String titulo){
       Optional<Libro> libro = libroRepository.findByTitulo(titulo);
       return libro.orElse(null);
    }

    public void eliminarLibroById (int id) {
        libroRepository.deleteById(id);
    }

    public void eliminarLibroByIsbn (String isbn) {
        libroRepository.deleteByIsbn(isbn);
    }

}