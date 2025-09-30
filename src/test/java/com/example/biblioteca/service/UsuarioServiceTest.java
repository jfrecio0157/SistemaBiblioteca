package com.example.biblioteca.service;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {
    private UsuarioRepository usuarioRepositoryMock;
    private UsuarioService usuarioServiceMock;

    @BeforeEach
    void setUp() {
        usuarioRepositoryMock = mock(UsuarioRepository.class);
        usuarioServiceMock = new UsuarioService(usuarioRepositoryMock);
    }

    @Test
    void insertarUsuario () {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1);

        //Act
        usuarioServiceMock.insertarUsuario(usuarioMock);

        //Assert
        verify(usuarioRepositoryMock).save(any(Usuario.class));

    }

    @Test
    void consultarUsuarioPorNombre_cuandoExisteUsuario_deberiaDevolverUsuario () {
        String nombre = "FRANCISCO GARCIA";
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1);

        when(usuarioRepositoryMock.findByNombre(nombre)).thenReturn(Optional.of(usuarioMock));

        //Act
        Usuario resultado = usuarioServiceMock.consultarUsuarioByNombre(nombre);

        //Assert
        assertNotNull(resultado);
        assertEquals(usuarioMock,resultado);
    }

    @Test
    void consultarUsuarioPorNombre_cuandoNoExisteUsuario_deberiaDevolverNull () {
        String nombre = "FRANCISCO GARCIA";

        when(usuarioRepositoryMock.findByNombre(nombre)).thenReturn(Optional.empty());

        //Act
        Usuario resultado = usuarioServiceMock.consultarUsuarioByNombre(nombre);

        //Assert
        assertNull(resultado);
    }

    @Test
    void eliminarUsuarioById () {
        //Act
        usuarioServiceMock.eliminarUsuarioById(1);
        //Assert
        verify(usuarioRepositoryMock).deleteById(anyInt());
    }
}
