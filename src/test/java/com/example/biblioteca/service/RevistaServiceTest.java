package com.example.biblioteca.service;

import com.example.biblioteca.model.Revista;
import com.example.biblioteca.repository.RevistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RevistaServiceTest {
    private RevistaRepository revistaRepositoryMock;
    private RevistaService revistaServiceMock;

    @BeforeEach
    public void setUp () {
        revistaRepositoryMock = mock(RevistaRepository.class);
        revistaServiceMock = new RevistaService(revistaRepositoryMock);
    }

    @Test
    public void insertRevista () {
        Revista revistaMock = new Revista();
        revistaMock.setId(1);

        //Act
        revistaServiceMock.insertarRevista(revistaMock);

        //Assert
        verify(revistaRepositoryMock).save(any(Revista.class));

    }

    @Test
    public void buscarRevistaByTitulo_cuandoExisteRevista_deberiaDevolverRevista () {
        String titulo = "TITULO REVISTA";
        Revista revistaMock = new Revista();
        revistaMock.setId(1);

        when(revistaRepositoryMock.findByTitulo(titulo)).thenReturn(Optional.of(revistaMock));

        //Act
        Revista resultado = revistaServiceMock.buscarRevistaByTitulo(titulo);

        //Assert
        assertNotNull(resultado);
        assertEquals(revistaMock, resultado);
    }

    @Test
    public void buscarRevistaByTitulo_cuandoNoExisteRevista_deberiaDevolverNull () {
        String titulo = "TITULO REVISTA";

        when(revistaRepositoryMock.findByTitulo(titulo)).thenReturn(Optional.empty());

        //Act
        Revista resultado = revistaServiceMock.buscarRevistaByTitulo(titulo);

        //Assert
        assertNull(resultado);
    }

    @Test
    public void eliminarRevistaById () {
        //Act
        revistaServiceMock.eliminarRevistaById(1);

        //Assert
        verify(revistaRepositoryMock).deleteById(anyInt());

    }

    @Test
    public void listaRevistas () {
        Revista revistaMock = new Revista();
        revistaMock.setId(1);
        List<Revista> revistaListMock = List.of(revistaMock);

        when(revistaRepositoryMock.findAll()).thenReturn(revistaListMock);

        //Act
        List<Revista> resultado = revistaServiceMock.listarRevistas();

        //Assert
        verify(revistaRepositoryMock).findAll();
        assertEquals(revistaListMock, resultado);
    }

}
