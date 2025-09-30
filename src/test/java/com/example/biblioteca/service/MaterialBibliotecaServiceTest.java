package com.example.biblioteca.service;

import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.repository.MaterialBibliotecaRepository;
import com.example.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MaterialBibliotecaServiceTest {
    private MaterialBibliotecaRepository materialBibliotecaRepositoryMock;
    private PrestamoRepository prestamoRepositoryMock;
    private MaterialBibliotecaService materialBibliotecaServiceMock;

    @BeforeEach
    void setUp () {
        materialBibliotecaRepositoryMock = mock(MaterialBibliotecaRepository.class);
        prestamoRepositoryMock = mock(PrestamoRepository.class);
        materialBibliotecaServiceMock = new MaterialBibliotecaService(materialBibliotecaRepositoryMock, prestamoRepositoryMock);
    }

    @Test
    void obtenerMaterialDelPrestamoById_cuandoPrestamoExisteYCoinciden_deberiaDevolverListaMaterialesDelPrestamo () {
        //Datos de entrada
        int prestamoId = 1;

        //Lista de materiales
        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);

        MaterialBiblioteca material2 = mock(MaterialBiblioteca.class);
        when(material2.getId()).thenReturn(102);

        Prestamo prestamo = new Prestamo();
        prestamo.setId(1);
        prestamo.setMateriales(List.of(material1, material2));

        when(prestamoRepositoryMock.findById(prestamoId)).thenReturn(Optional.of(prestamo));

        //Ejecutamos el proceso
        List<MaterialBiblioteca> resultado = materialBibliotecaServiceMock.obtenerMaterialesDelPrestamoById(prestamoId);

        //Asset
        assertEquals(prestamo.getMateriales(), resultado);
    }

    @Test
    void obtenerMaterialDelPrestamoById_cuandoPrestamoExisteYNoCoinciden_deberiaDevolverListaVacia () {
        //Datos de entrada
        int prestamoId = 2;

        //Lista de materiales
        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);

        MaterialBiblioteca material2 = mock(MaterialBiblioteca.class);
        when(material2.getId()).thenReturn(102);

        Prestamo prestamo = new Prestamo();
        prestamo.setId(1);
        prestamo.setMateriales(List.of(material1, material2));

        when(prestamoRepositoryMock.findById(prestamoId)).thenReturn(Optional.empty());

        //Ejecutamos el proceso
        List<MaterialBiblioteca> resultado = materialBibliotecaServiceMock.obtenerMaterialesDelPrestamoById(prestamoId);

        //Asset
        assertTrue(resultado.isEmpty());
        assertNotEquals(prestamo.getMateriales(), resultado);
    }

    @Test
    void obtenerMaterialDelPrestamoByTitulo () {
        //Datos de entrada
        String titulo = "TITULO PRESTAMO";

        //Lista de materiales
        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);

        when(materialBibliotecaRepositoryMock.findByTitulo(titulo)).thenReturn(material1);

        //Ejecutamos el proceso
        MaterialBiblioteca resultado = materialBibliotecaServiceMock.obtenerMaterialDelPrestamoByTitulo(titulo);

        //Asset
        assertEquals(material1, resultado);

    }

    @Test
    void actualizarDisponible () {
        //Datos de entrada
        int disponible = 2;

        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);
        when(material1.getDisponibles()).thenReturn(3);

        //Ejecutamos el proceso
        materialBibliotecaServiceMock.actualizarDisponible(material1, disponible);

        //Assert
        // Verifica que se llamó a setDisponibles con el valor esperado
        verify(material1).setDisponibles(disponible);

        verify(materialBibliotecaRepositoryMock).save(any(MaterialBiblioteca.class));

    }
}
