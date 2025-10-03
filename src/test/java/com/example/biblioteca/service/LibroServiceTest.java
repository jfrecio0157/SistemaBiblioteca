package com.example.biblioteca.service;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.repository.LibroRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LibroServiceTest {
    private LibroRepository libroRepositoryMock;
    private LibroService libroServiceMock;
    private Validator validatorMock;

    @BeforeEach
    public void setUp (){
        libroRepositoryMock = mock(LibroRepository.class);
        validatorMock = mock(Validator.class);
        libroServiceMock = new LibroService(libroRepositoryMock,validatorMock);
    }

    @Test
    public void insertarLibro_cuandoLibroEsValido_deberiaGuardarLibro() {
        // Arrange
        Autor autorMock = new Autor();
        autorMock.setNombre("Gabriel García Márquez");
        Libro libroValido = new Libro("1234", 2020, List.of(autorMock));

        //Con Collections.emptySet() simula que no hay errores de validación
        when(validatorMock.validate(libroValido)).thenReturn(Collections.emptySet());

        // Act
        libroServiceMock.insertarLibro(libroValido);

        // Assert
        verify(libroRepositoryMock).save(libroValido);
    }

    @Test
    public void insertarLibro_cuandoLibroNoEsValido_deberiaLanzarConstraintViolationException() {
        // Arrange
        Libro libroInvalido = new Libro(); // Supongamos que le faltan campos obligatorios

        ConstraintViolation<Libro> violationMock = mock(ConstraintViolation.class);
        Set<ConstraintViolation<Libro>> violations = Set.of(violationMock);

        when(validatorMock.validate(libroInvalido)).thenReturn(violations);

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> {
            libroServiceMock.insertarLibro(libroInvalido);
        });

        verify(libroRepositoryMock, never()).save(any());
    }


    @Test
    public void listarLibros () {
        Libro libroMock = new Libro();
        libroMock.setId(1);
        List<Libro> libroListMock = List.of(libroMock);

        when(libroRepositoryMock.findAll()).thenReturn(libroListMock);

        //Act
        List<Libro> resultado = libroServiceMock.listarLibros();

        //Assert
        assertEquals(libroListMock, resultado);

    }

    @Test
    public void buscarLibroByIsbn_cuandoEncuentraLibro_deberiaDevolverLibro () {
        String isbn = "1234";

        Libro libroMock = new Libro();
        libroMock.setIsbn("1234");

        when(libroRepositoryMock.findLibroConAutores(isbn)).thenReturn(Optional.of(libroMock));

        //Act
        Libro resultado = libroServiceMock.buscarLibroByIsbn(isbn);

        //Assert
        assertNotNull(resultado);
        assertEquals(libroMock, resultado);
    }

    @Test
    public void buscarLibroByIsbn_cuandoNoEncuentraLibro_deberiaDevolverNull () {
        String isbn = "1234";

        when(libroRepositoryMock.findLibroConAutores(isbn)).thenReturn(Optional.empty());

        //Act
        Libro resultado = libroServiceMock.buscarLibroByIsbn(isbn);

        //Assert
        assertNull(resultado);
    }

    @Test
    public void buscarLibroByTitulo_cuandoEncuentraLibro_deberiaDevolverLibro () {
        String titulo = "TITULO PRUEBA";

        Libro libroMock = new Libro();
        libroMock.setId(1);

        when(libroRepositoryMock.findByTitulo(titulo)).thenReturn(Optional.of(libroMock));

        //Act
        Libro resultado = libroServiceMock.buscarLibroByTitulo(titulo);

        //Asset
        assertNotNull(resultado);
        assertEquals(libroMock, resultado);

    }

    @Test
    public void buscarLibroByTitulo_cuandoNoEncuentraLibro_deberiaDevolverNull () {
        String titulo = "TITULO PRUEBA";

        when(libroRepositoryMock.findByTitulo(titulo)).thenReturn(Optional.empty());

        //Act
        Libro resultado = libroServiceMock.buscarLibroByTitulo(titulo);

        //Asset
        assertNull(resultado);

    }

    @Test
    public void eliminarLibroById () {
        String isbn = "1234";

        //Act
        libroServiceMock.eliminarLibroByIsbn(isbn);

        //Assert
        verify(libroRepositoryMock).deleteByIsbn(anyString());
    }

}
