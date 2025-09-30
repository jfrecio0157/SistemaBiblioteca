package com.example.biblioteca.component;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.service.AutorService;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.util.CommonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsolaAutorTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Mock
    private AutorService autorService;

    @Mock
    private CommonService commonService;

    @Mock
    private CommonUtil commonUtil;

    @Spy
    @InjectMocks
    private ConsolaAutor consolaSpy;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void procesarAltaAutor_cuandoDatosValidos_deberiaInsertarAutorYMostrarMensaje (){

        // Arranque
        Autor autorMock = new Autor();
        autorMock.setNombre("GABRIEL GARCIA MARQUEZ");

        // Simular que solicitarDatosAutor devuelve un autor
        doReturn(autorMock).when(consolaSpy).solicitarDatosAutor();

        // Act
        consolaSpy.procesarAltaAutor();

        // Assert
        verify(autorService).insertarAutor("GABRIEL GARCIA MARQUEZ");
        verify(commonUtil).mostrarMensaje("Autor dado de alta correctamente");

        //Verificamos que los procesos posteriores no es ejecutan
        verify(commonUtil,never()).mostrarMensajeError(anyString(),any());
    }


    @Test
    void procesarAltaAutor_cuandoError_deberiaMostrarErrorSiFalla() {
        // Arrange
        Autor autorMock = new Autor();
        autorMock.setNombre("AUTOR FALLIDO");

        doReturn(autorMock).when(consolaSpy).solicitarDatosAutor();

        doThrow(new RuntimeException("Error de prueba"))
                .when(autorService).insertarAutor("AUTOR FALLIDO");

        // Act
        consolaSpy.procesarAltaAutor();

        // Assert
        verify(commonUtil).mostrarMensajeError(eq("alta Autor"), any(RuntimeException.class));

        verify(commonUtil,never()).mostrarMensaje(anyString());
    }

    @Test
    void procesarBajaAutor_cuandoDatosValidos_deberiaEliminarAutorYMostrarMensaje (){
        //Arranque
        Autor autorMock = new Autor();
        autorMock.setId(40);

        doReturn(autorMock).when(consolaSpy).obtenerAutor();

        // Act
        consolaSpy.procesarBajaAutor();

        // Assert
        verify(autorService).eliminarAutorById(40);
        verify(commonUtil).mostrarMensaje("Autor dado de baja correctamente");

        //Verificamos que nos ejecutan procesos posteriores
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    void procesarBajaAutor_cuandoHayExcepcion_deberiaMostrarErrorSiFalla() {
        //Arranque
        Autor autorMock = new Autor();
        autorMock.setId(99);

        doReturn(autorMock).when(consolaSpy).obtenerAutor();

        doThrow(new RuntimeException("Error de prueba"))
                .when(autorService).eliminarAutorById(99);

        // Act
        consolaSpy.procesarBajaAutor();

        // Assert
        verify(commonUtil).mostrarMensajeError(eq("baja Autor"), any(RuntimeException.class));

        //No se ejecutan
        verify(commonUtil,never()).mostrarMensaje(anyString());

    }

    @Test
    void procesarBajaAutor_cuandoAutorEsNull_deberiaNoEjecutarNingunaAccion () {
        // Arrange

        doReturn(null).when(consolaSpy).obtenerAutor();

        // Act
        consolaSpy.procesarBajaAutor();

        // Assert
        verifyNoInteractions(autorService);
        verifyNoInteractions(commonUtil);
    }

    @Test
    void procesarConsultaAutor_cuandoDatosValidos_deberiaMostrarAutorYMostrarLibros (){
        //Arranque
        Autor autorMock = new Autor();
        autorMock.setId(1);
        autorMock.setNombre("PEPE GARCIA");

        Libro libroMock = new Libro();
        libroMock.setTitulo("El pollo Pepe");

        autorMock.setLibro(List.of(libroMock));

        doReturn(autorMock).when(consolaSpy).obtenerAutor();

        // Act
        consolaSpy.procesarConsultaAutor();

        // Assert
        verify(consolaSpy).mostrarDatos(autorMock);
        verify(consolaSpy).mostrarLibros(autorMock);

    }


    @Test
    void procesarConsultaAutor_cuandoAutorEsNull_deberiaNoEjecutarNingunaAccion () {
        // Arrange
        doReturn(null).when(consolaSpy).obtenerAutor();

        // Act
        consolaSpy.procesarConsultaAutor();

        // Assert
        verify(consolaSpy, never()).mostrarDatos(any());
        verify(consolaSpy, never()).mostrarLibros(any());
    }


    @Test
    void mostrarDatos_deberiaImprimirDatosDelAutor() {
        Autor autor = new Autor();
        autor.setId(1);
        autor.setNombre("JULIO CORTÁZAR");

        consolaSpy.mostrarDatos(autor);

        String salida = outContent.toString();
        assertTrue(salida.contains("Datos del Autor"));
        assertTrue(salida.contains("ID: 1"));
        assertTrue(salida.contains("Nombre: JULIO CORTÁZAR"));
    }


    @Test
    void mostrarLibros_cuandoNoHaylibros_deberiaMostrarMensaje() {
        Autor autor = new Autor();
        autor.setLibro(List.of());

        consolaSpy.mostrarLibros(autor);

        verify(commonUtil,times(1)).mostrarMensaje("Este autor no tiene libros registrados");

        //Verificamos que no hay mas iteraciones de commonUtil
        verifyNoMoreInteractions(commonUtil);
    }

    @Test
    void mostrarLibros_cuandoHayLibros_deberiaMostrarTitulos() {
        //Arranque
        Libro libro1 = new Libro();
        libro1.setTitulo("El Quijote");

        Libro libro2 = new Libro();
        libro2.setTitulo("Noche estrellada");

        Autor autor = new Autor();
        autor.setLibro(List.of(libro1, libro2));

        //Act
        consolaSpy.mostrarLibros(autor);

        //Verificaciones
        verify(commonUtil).mostrarMensaje("Titulo libro: El Quijote");
        verify(commonUtil).mostrarMensaje("Titulo libro: Noche estrellada");

        //Verificamos que no hay mas iteraciones de commonUtil
        verifyNoMoreInteractions(commonUtil);

    }

    //El autor no existe
    @Test
    void obtenerAutor_cuandoAutorNoExiste_deberiaMostrarMensaje() {
        doReturn("AUTOR INEXISTENTE").when(consolaSpy).solicitarAutorByNombre();
        when(autorService.buscarAutorByNombre("AUTOR INEXISTENTE")).thenReturn(null);

        Autor resultado = consolaSpy.obtenerAutor();

        assertNull(resultado);
        verify(commonUtil).mostrarMensaje("Autor inexistente en la biblioteca");

        //Verificamos que no hay mas iteraciones de commonUtil
        verifyNoMoreInteractions(commonUtil);
    }

    //El autor existe y tiene libros en la biblioteca
    @Test
    void obtenerAutor_cuandoAutorTieneLibros_deberiaMostrarMensaje() {
        doReturn("AUTOR CON LIBROS").when(consolaSpy).solicitarAutorByNombre();

        Autor autorMock = new Autor();
        autorMock.setNombre("AUTOR CON LIBROS");
        autorMock.setLibro(List.of(new Libro())); // tiene libros

        when(autorService.buscarAutorByNombre("AUTOR CON LIBROS")).thenReturn(autorMock);

        Autor resultado = consolaSpy.obtenerAutor();

        assertNull(resultado);
        verify(commonUtil).mostrarMensaje("El autor \"AUTOR CON LIBROS\" no se puede dar de baja. Tiene libros en la biblioteca");

        //Verificamos que no hay mas iteraciones de commonUtil
        verifyNoMoreInteractions(commonUtil);

    }

    //El autor existe y no tiene libros en la biblioteca
    @Test
    void obtenerAutor_cuandoAutorExisteYNoTieneLibros_deberiaDevolverAutor() {
        doReturn("AUTOR LIBRE").when(consolaSpy).solicitarAutorByNombre();

        Autor autorMock = new Autor();
        autorMock.setNombre("AUTOR LIBRE");
        autorMock.setLibro(List.of()); // sin libros

        when(autorService.buscarAutorByNombre("AUTOR LIBRE")).thenReturn(autorMock);

        Autor resultado = consolaSpy.obtenerAutor();

        assertNotNull(resultado);
        assertEquals("AUTOR LIBRE", resultado.getNombre());
        verify(commonUtil, never()).mostrarMensaje(anyString());
    }


    @Test
    void solicitarAutorByNombre_deberiaLeerEntradaYMostrarMensaje() {
        // Arrange
        when(commonUtil.leerEntrada()).thenReturn("CLARICE LISPECTOR");

        // Act
        String nombre = consolaSpy.solicitarAutorByNombre();

        // Assert
        assertEquals("CLARICE LISPECTOR", nombre);
        verify(commonUtil).mostrarMensaje("Nombre del autor");
    }

    @Test
    void menuAutor_deberiaEjecutarOpcionesYSalirCorrectamente() {
        ConsolaAutor consolaSpy = spy(new ConsolaAutor(autorService, commonService, commonUtil));

        // El mock de commonUtil debe devolver la secuencia de opciones
        when(commonUtil.leerEntrada()).thenReturn("A","B", "C","S");

        doNothing().when(consolaSpy).procesarAltaAutor();
        doNothing().when(consolaSpy).procesarBajaAutor();
        doNothing().when(consolaSpy).procesarConsultaAutor();
        doNothing().when(commonService).volverMenuPrincipal();

        // Act
        consolaSpy.menuAutor();

        // Assert
        verify(consolaSpy,times(1)).procesarAltaAutor();
        verify(consolaSpy,times(1)).procesarBajaAutor();
        verify(consolaSpy,times(1)).procesarConsultaAutor();
        verify(commonService,times(1)).volverMenuPrincipal();
        verify(commonService,never()).mostrarError();

        // (opcional) el menú se mostró 4 veces (A,B,C,S)
        verify(consolaSpy, times(4)).mostrarMenuAutor();

        // (opcional) se leyó entrada 4 veces
        verify(commonUtil, times(4)).leerEntrada();
    }

    @Test
    public void mostrarMenuAutor () {
        consolaSpy.mostrarMenuAutor();

        String actual = outContent.toString(StandardCharsets.UTF_8);
        actual = actual.replace("\r\n", "\n"); // Normaliza saltos de línea

        String expectedText="""
                \n-- Menu autor ---
                A.- Alta autor
                B.- Baja autor
                C.- Consulta autor
                S.- Salir al menú principal
                Elige una opción:""";

        // Como usas println, se añade un salto extra al final
        String expected = expectedText + "\n";

        //Assert
        assertEquals(expected, actual);

    }
}
