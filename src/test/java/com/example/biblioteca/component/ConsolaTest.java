package com.example.biblioteca.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class ConsolaTest {

    @InjectMocks
    private Consola consola;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void menuConsola_cuandoTodoCorrecto_deberiaMostrarMenu(){
        Consola consolaSpy = spy(consola);

        String expected ="""
                \nBienvenido a la Biblioteca
                1.- Usuario
                2.- Libros
                3.- Revistas
                4.- Préstamos
                5.- Autor
                S.- Salir
                Elige una opción:""";

        //Act
        consolaSpy.menuConsola();

        //Assert
        assertEquals(expected, consolaSpy.menuConsola());
    }
}
