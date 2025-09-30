package com.example.biblioteca.service;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.repository.AutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class AutorServiceTest {

    private AutorRepository autorRepositoryMock;
    private AutorService autorService;

    @BeforeEach
    void setUp () {
        autorRepositoryMock = mock(AutorRepository.class);
        autorService = new AutorService(autorRepositoryMock);
    }

    @Test
    public void insertarAutor_deberiaGuardarAutorEnRepositorio() {
        String nombre = "Gabriel García Márquez";

        //Act
        autorService.insertarAutor(nombre);

        //Assert
        verify(autorRepositoryMock).save(any(Autor.class));
    }

    @Test
    public void buscarAutorByNombre_cuandoEncuentraElAutor_deberiaDevolverElAutor () {
        Autor autorMock = new Autor();
        autorMock.setNombre("Gabriel García Márquez");

        String nombre = "Gabriel García Márquez";

        when(autorRepositoryMock.findByNombre(nombre)).thenReturn(Optional.of(autorMock));

        //Act
        Autor resultado = autorService.buscarAutorByNombre(nombre);

        //Assert
        assertNotNull(resultado);
        assertEquals(autorMock, resultado);

    }

    @Test
    public void buscarAutorByNombre_cuandoNoEncuentraElAutor_deberiaDevolverNull () {

        String nombre = "Gabriel García Márquez";

        when(autorRepositoryMock.findByNombre(nombre)).thenReturn(Optional.empty());

        //Act
        Autor resultado = autorService.buscarAutorByNombre(nombre);

        //Assert
        assertNull(resultado);


    }

    @Test
    public void eliminarAutorById () {
        int id = 1;
        //Act
        autorService.eliminarAutorById(id);
        //Assert
        assertFalse(autorRepositoryMock.existsById(id));
    }


}
