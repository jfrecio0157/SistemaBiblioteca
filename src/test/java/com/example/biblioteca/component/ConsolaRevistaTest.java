package com.example.biblioteca.component;

import com.example.biblioteca.model.*;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.service.MaterialBibliotecaService;
import com.example.biblioteca.service.RevistaService;
import com.example.biblioteca.util.CommonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class ConsolaRevistaTest {

    @Mock
    RevistaService revistaService;
    @Mock
    MaterialBibliotecaService materialBibliotecaService;
    @Mock
    CommonService commonService;
    @Mock
    CommonUtil commonUtil;

    @InjectMocks
    private ConsolaRevista consolaRevista;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void menuLibro_deberiaEjecutarOpcionesYSalirCorrectamente() {
        // Simular entrada: A (alta), B (baja), C (consulta), S (salir)
        String input = "A\nB\nC\nD\nS\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        ConsolaRevista consolaSpy = spy(new ConsolaRevista(revistaService,materialBibliotecaService,commonService,commonUtil));


        doNothing().when(consolaSpy).procesarAltaRevista();
        doNothing().when(consolaSpy).procesarBajaRevista();
        doNothing().when(consolaSpy).procesarConsultaByTitulo();
        doNothing().when(consolaSpy).procesarConsultaTotal();
        doNothing().when(commonService).volverMenuPrincipal();
        doNothing().when(commonService).mostrarError();

        // Act
        consolaSpy.menuRevista();

        // Assert
        verify(consolaSpy).procesarAltaRevista();
        verify(consolaSpy).procesarBajaRevista();
        verify(consolaSpy).procesarConsultaByTitulo();
        verify(consolaSpy).procesarConsultaTotal();
        verify(commonService).volverMenuPrincipal();
    }

    @Test
    public void  procesarAltaRevista_cuandoDatosValidos_deberiaInsertarRevistaYMostrarMensaje(){
        //Arranque
        Revista revistaMock = new Revista();
        revistaMock.setTitulo("TITULO PRUEBA");
        revistaMock.setPeriodicidad("MENSUAL");
        revistaMock.setNumeroEdicion(1);
        revistaMock.setTotales(5);
        revistaMock.setDisponibles(5);

        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);
        doReturn(revistaMock).when(consolaSyp).solicitarDatosRevista();

        //Act
        consolaSyp.procesarAltaRevista();

        //Assert
        verify(revistaService).insertarRevista(revistaMock);
        verify(commonUtil).mostrarMensaje("Revista insertada correctamente.");

    }

    @Test
    public void  procesarAltaRevista_cuandoExcepcion_deberiaMostrarMensajeError(){
        //Arranque
        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);

        Revista revistaMock = new Revista();

        // Simulamos una excepción general al insertar
        Exception exception = new RuntimeException("Error inesperado");
        doReturn(revistaMock).when(consolaSyp).solicitarDatosRevista();
        doThrow(exception).when(revistaService).insertarRevista(revistaMock);

        //Act
        consolaSyp.procesarAltaRevista();

        //Assert
        verify(commonUtil).mostrarMensajeError("alta de la revista: ", exception);

    }

    @Test
    public void  procesarBajaRevista_cuandoDatosValidos_deberiaBajaRevistaYMostrarMensaje() {
        //Arranque
        String titulo = "TITULO PRUEBA";

        Revista revistaMock = new Revista();
        revistaMock.setId(1);

        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);
        doReturn(titulo).when(consolaSyp).solicitarRevistaByTitulo();
        doReturn(revistaMock).when(revistaService).buscarRevistaByTitulo(titulo);

        //Se mockea MaterialBiblioteca porque es una clase abstracta
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        doReturn(true).when(commonService).preguntarSiBorrar();

        //Act
        consolaSyp.procesarBajaRevista();

        //Assert
        verify(revistaService).eliminarRevistaById(1);
        verify(commonUtil).mostrarMensaje("La revista ha sido eliminada");
    }

    @Test
    public void  procesarBajaRevista_cuandoRevistaInexistente_deberiaMostrarMensaje() {
        //Arranque
        String titulo = "TITULO PRUEBA";

        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);
        doReturn(titulo).when(consolaSyp).solicitarRevistaByTitulo();
        doReturn(null).when(revistaService).buscarRevistaByTitulo(titulo);

        //Act
        consolaSyp.procesarBajaRevista();

        //Assert
        verify(commonUtil,times(1)).mostrarMensaje("Revista \"" + titulo + "\" inexistente en la biblioteca");
        //Verificamos que no se ejecutan el resto de procesos posteriores
        verify(materialBibliotecaService,never()).obtenerMaterialDelPrestamoByTitulo(anyString());
        verify(commonService,never()).preguntarSiBorrar();
        verify(revistaService,never()).eliminarRevistaById(anyInt());
    }

    @Test
    public void  procesarBajaRevista_cuandoRevistaPrestada_deberiaMostrarMensaje() {
        //Arranque
        String titulo = "TITULO PRUEBA";

        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);
        doReturn(titulo).when(consolaSyp).solicitarRevistaByTitulo();

        Revista revistaMock = new Revista();
        revistaMock.setTitulo(titulo);
        doReturn(revistaMock).when(revistaService).buscarRevistaByTitulo(titulo);

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(3);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //Act
        consolaSyp.procesarBajaRevista();

        //Assert
        verify(commonUtil,times(1)).mostrarMensaje("La revista \"" + titulo + "\" no se puede eliminar. Esta prestada.");
        //Verificamos que no se ejecutan el resto de procesos posteriores
        verify(commonService,never()).preguntarSiBorrar();
        verify(revistaService,never()).eliminarRevistaById(anyInt());
    }

    @Test
    public void procesarBajaRevista_cuandoNoSeConfirmaBorrar_deberiaMostrarMensaje() {
        //Arranque
        String titulo = "TITULO PRUEBA";

        Revista revistaMock = new Revista();
        revistaMock.setId(1);

        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);
        doReturn(titulo).when(consolaSyp).solicitarRevistaByTitulo();
        doReturn(revistaMock).when(revistaService).buscarRevistaByTitulo(titulo);

        //Se mockea MaterialBiblioteca porque es una clase abstracta
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //No se confirma borrar
        doReturn(false).when(commonService).preguntarSiBorrar();

        //Act
        consolaSyp.procesarBajaRevista();

        //Assert
        verify(commonUtil,times(1)).mostrarMensaje("La revista \"" + titulo + "\" no ha sido eliminada.");
        //Verificamos que el resto de procesos posteriores no se ejecutan
        verify(revistaService,never()).eliminarRevistaById(anyInt());
    }

    @Test
    public void procesarBajaRevista_cuandoExcepcion_deberiaMostrarMensajeError() {
        //Arranque
        String titulo = "TITULO PRUEBA";

        Revista revistaMock = new Revista();
        revistaMock.setId(1);

        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);
        doReturn(titulo).when(consolaSyp).solicitarRevistaByTitulo();
        doReturn(revistaMock).when(revistaService).buscarRevistaByTitulo(titulo);

        //Se mockea MaterialBiblioteca porque es una clase abstracta
        //El material no está prestado
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //Se confirma borrar
        doReturn(true).when(commonService).preguntarSiBorrar();
        Exception exception = new RuntimeException("Error inesperado");

        //Se lanza excepcion en el metodo eliminarRevistaById
        doThrow(exception).when(revistaService).eliminarRevistaById(1);

        //Act
        consolaSyp.procesarBajaRevista();

        //Assert
        verify(commonUtil).mostrarMensajeError("baja revista: ", exception);
    }

    @Test
    public void procesarConsultaByTitulo_cuandoDatosValidos_deberiaMostrarDatosPorConsola(){
        // Redirigir salida estándar
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Arranque
        String titulo = "TITULO PRUEBA";
        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);

        doReturn(titulo).when(commonUtil).leerEntrada();
        doReturn(titulo).when(consolaSyp).solicitarRevistaByTitulo();


        Revista revistaMock = new Revista();
        revistaMock.setId(1);
        revistaMock.setTitulo("TITULO PRUEBA");
        revistaMock.setPeriodicidad("MENSUAL");
        revistaMock.setNumeroEdicion(1);
        revistaMock.setTotales(5);
        revistaMock.setDisponibles(5);

        doReturn(revistaMock).when(revistaService).buscarRevistaByTitulo(titulo);

        // Act
        consolaSyp.procesarConsultaByTitulo();

        // Assert
        String salida = outContent.toString();
        assertTrue(salida.contains("Datos de la revista"));
        assertTrue(salida.contains("id: 1"));
        assertTrue(salida.contains("Título: TITULO PRUEBA"));
        assertTrue(salida.contains("Periodicidad: MENSUAL"));
        assertTrue(salida.contains("Número edición: 1"));
        assertTrue(salida.contains("Ejemplares totales: 5"));
        assertTrue(salida.contains("Ejemplares disponibles: 5"));

        // Restaurar salida estándar
        System.setOut(System.out);
    }

    @Test
    public void procesarConsultaTotal_cuandoDatosValidos_deberiaMostrarDatosPorConsola(){
        // Redirigir salida estándar
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Arranque
        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);

        Revista revista1Mock = new Revista();
        revista1Mock.setId(1);
        revista1Mock.setTitulo("TITULO 1 PRUEBA");
        revista1Mock.setPeriodicidad("MENSUAL");
        revista1Mock.setNumeroEdicion(1);
        revista1Mock.setTotales(5);
        revista1Mock.setDisponibles(5);

        Revista revista2Mock = new Revista();
        revista2Mock.setId(2);
        revista2Mock.setTitulo("TITULO 2 PRUEBA");
        revista2Mock.setPeriodicidad("DIARIA");
        revista2Mock.setNumeroEdicion(1);
        revista2Mock.setTotales(3);
        revista2Mock.setDisponibles(3);

        List<Revista> revistaListMock = List.of(revista1Mock, revista2Mock);

        doReturn(revistaListMock).when(revistaService).listarRevistas();

        //Act
        consolaSyp.procesarConsultaTotal();

        // Assert
        String salida = outContent.toString();
        assertTrue(salida.contains("Datos de la revista"));
        assertTrue(salida.contains("id: 1"));
        assertTrue(salida.contains("Título: TITULO 1 PRUEBA"));
        assertTrue(salida.contains("Periodicidad: MENSUAL"));
        assertTrue(salida.contains("Número edición: 1"));
        assertTrue(salida.contains("Ejemplares totales: 5"));
        assertTrue(salida.contains("Ejemplares disponibles: 5"));

        assertTrue(salida.contains("Datos de la revista"));
        assertTrue(salida.contains("id: 2"));
        assertTrue(salida.contains("Título: TITULO 2 PRUEBA"));
        assertTrue(salida.contains("Periodicidad: DIARIA"));
        assertTrue(salida.contains("Número edición: 1"));
        assertTrue(salida.contains("Ejemplares totales: 3"));
        assertTrue(salida.contains("Ejemplares disponibles: 3"));

        // Restaurar salida estándar
        System.setOut(System.out);

    }

    @Test
    public void procesarConsultaTotal_cuandoNoExitenRevistas_deberiaMostrarMensDatosPorConsola(){
        // Arranque
        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);

        Revista revista1Mock = new Revista();

        //Act
        consolaSyp.procesarConsultaTotal();

        //Assert
        verify(commonUtil).mostrarMensaje("No hay revistas en la biblioteca");

    }

    @Test
    void solicitarDatosRevista_cuandoDatosValidos_deberiaDevolverRevistaCompleta() {
        // Arranque
        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);

        when(commonUtil.leerEntrada())
                .thenReturn("TITULO PRUEBA") //Titulo de la revista
                .thenReturn("MENSUAL"); //Periodicidad de la revista

        when(commonUtil.leerEntero())
                .thenReturn(1) //Número edición
                .thenReturn(5); //Número de ejemplares de la biblioteca

        //Act
        Revista revista = consolaSyp.solicitarDatosRevista();

        //Asserts
        assertEquals("TITULO PRUEBA", revista.getTitulo());
        assertEquals("MENSUAL", revista.getPeriodicidad());
        assertEquals(1, revista.getNumeroEdicion());
        assertEquals(5, revista.getTotales());
        assertEquals(5, revista.getDisponibles());

    }

    @Test
    public void solicitarRevistaByTitulo_cuandoDatosValidos_deberiaDevolverTitulo(){
        // Arranque
        ConsolaRevista consolaSyp = Mockito.spy(consolaRevista);

        String titulo = "TITULO PRUEBA";
        when(commonUtil.leerEntrada()).thenReturn("TITULO PRUEBA");

        //Act
        titulo = consolaSyp.solicitarRevistaByTitulo();

        //Assert
        assertEquals("TITULO PRUEBA", titulo);

    }

}
