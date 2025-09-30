package com.example.biblioteca.service;

import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PrestamoServiceTest {
    private PrestamoRepository prestamoRepositoryMock;
    private PrestamoService prestamoServiceMock;

    @BeforeEach
    void setUp () {
        prestamoRepositoryMock = mock(PrestamoRepository.class);
        prestamoServiceMock = new PrestamoService(prestamoRepositoryMock);
    }


    @Test
    void insertarPrestamo () {
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(1);

        //Act
        prestamoServiceMock.insertarPrestamo(prestamoMock);

        //Assert
        verify(prestamoRepositoryMock).save(any(Prestamo.class));

    }

    @Test
    void buscarPrestamoByMaterialId () {
        int id = 1;

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(1);
        List<Prestamo> prestamoListMock = List.of(prestamoMock);
        when(prestamoRepositoryMock.findMaterialById(id)).thenReturn(prestamoListMock);

        //Act
        List<Prestamo> resultado = prestamoServiceMock.buscarPrestamoByMaterialId(id);

        //Assert
        verify(prestamoRepositoryMock).findMaterialById(id);
        assertEquals(prestamoListMock, resultado);

    }

    @Test
    void buscarPrestamoByUsuarioIdAndActivo () {
        int usuario_id = 1;

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(1);
        List<Prestamo> prestamoListMock = List.of(prestamoMock);
        when(prestamoRepositoryMock.findByUsuario_idAndActivo(usuario_id)).thenReturn(prestamoListMock);

        //Act
        List<Prestamo> resultado = prestamoServiceMock.buscarPrestamoByUsuarioIdAndActivo(usuario_id);

        //Assert
        verify(prestamoRepositoryMock).findByUsuario_idAndActivo(usuario_id);
        assertEquals(prestamoListMock, resultado);
    }

    @Test
    void testObtenerMaterialIdsPorPrestamos() {
        // Datos de entrada
        List<Integer> prestamoIds = List.of(1, 2);

        // Simulación de materiales
        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);

        MaterialBiblioteca material2 = mock(MaterialBiblioteca.class);
        when(material2.getId()).thenReturn(102);

        MaterialBiblioteca material3 = mock(MaterialBiblioteca.class);
        when(material3.getId()).thenReturn(101); // Repetido para probar el distinct

        // Simulación de préstamos
        Prestamo prestamo1 = new Prestamo();
        prestamo1.setMateriales(List.of(material1, material2));

        Prestamo prestamo2 = new Prestamo();
        prestamo2.setMateriales(List.of(material3));

        // Configurar el mock
        when(prestamoRepositoryMock.findAllById(prestamoIds)).thenReturn(List.of(prestamo1, prestamo2));

        // Ejecutar el método
        List<Integer> resultado = prestamoServiceMock.obtenerMaterialIdsPorPrestamos(prestamoIds);

        // Verificar el resultado
        assertEquals(List.of(101, 102), resultado);
    }

    @Test
    void desactivarPrestamoConMaterial () {
        // Datos de entrada
        List<Integer> prestamoIds = List.of(1, 2);
        int materialId = 101;

        // Simulación de materiales
        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);

        MaterialBiblioteca material2 = mock(MaterialBiblioteca.class);
        when(material2.getId()).thenReturn(102);

        MaterialBiblioteca material3 = mock(MaterialBiblioteca.class);
        when(material3.getId()).thenReturn(101); // Repetido para probar el distinct

        // Simulación de préstamos
        Prestamo prestamo1 = new Prestamo();
        prestamo1.setMateriales(List.of(material1, material2));

        Prestamo prestamo2 = new Prestamo();
        prestamo2.setMateriales(List.of(material3));

        // Configurar el mock
        when(prestamoRepositoryMock.findAllWithMaterialesById(prestamoIds)).thenReturn(List.of(prestamo1, prestamo2));

        // Ejecutar el método
        prestamoServiceMock.desactivarPrestamoConMaterial(prestamoIds, materialId);

        // Asset
        verify(prestamoRepositoryMock).save(any(Prestamo.class));

    }

    @Test
    void NoDesactivarPrestamoConMaterial () {
        // Datos de entrada
        List<Integer> prestamoIds = List.of(1, 2);
        int materialId = 999; //No coincide con niguno

        // Simulación de materiales
        MaterialBiblioteca material1 = mock(MaterialBiblioteca.class);
        when(material1.getId()).thenReturn(101);

        // Simulación de préstamos
        Prestamo prestamo1 = new Prestamo();
        prestamo1.setMateriales(List.of(material1));

        // Configurar el mock
        when(prestamoRepositoryMock.findAllWithMaterialesById(prestamoIds)).thenReturn(List.of(prestamo1));

        // Ejecutar el método
        prestamoServiceMock.desactivarPrestamoConMaterial(prestamoIds, materialId);

        // Asset
        // Verificamos que no se ejecuta porque no coincide
        verify(prestamoRepositoryMock,never()).save(any(Prestamo.class));

    }

}

