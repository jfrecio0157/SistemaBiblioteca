package com.example.biblioteca.component;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.service.AutorService;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.service.LibroService;
import com.example.biblioteca.service.MaterialBibliotecaService;
import com.example.biblioteca.util.CommonUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class ConsolaLibroTest {
    @Mock
    MaterialBibliotecaService materialBibliotecaService;

    @Mock
    LibroService libroService;

    @Mock
    AutorService autorService;

    @Mock
    private CommonService commonService;

    @Mock
    private CommonUtil commonUtil;

    @Spy @InjectMocks
    private ConsolaLibro consolaSpy;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void menuLibro_deberiaEjecutarOpcionesYSalirCorrectamente() {
        ConsolaLibro consolaSpy = spy(new ConsolaLibro(libroService,materialBibliotecaService,autorService,commonService,commonUtil));

        // Simular entrada: A (alta), B (baja), C (consulta), S (salir)
        when(commonUtil.leerEntrada()).thenReturn("A","B","C","D","E","S");

        doNothing().when(consolaSpy).procesarAltaLibro();
        doNothing().when(consolaSpy).procesarBajaLibro();
        doNothing().when(consolaSpy).procesarConsultaByIsbn();
        doNothing().when(consolaSpy).procesarConsultaByTitulo();
        doNothing().when(consolaSpy).procesarConsultaTotal();
        doNothing().when(commonService).volverMenuPrincipal();
        //doNothing().when(commonService).mostrarError();

        // Act
        consolaSpy.menuLibro();

        // Assert
        verify(consolaSpy,times(1)).procesarAltaLibro();
        verify(consolaSpy,times(1)).procesarBajaLibro();
        verify(consolaSpy,times(1)).procesarConsultaByIsbn();
        verify(consolaSpy,times(1)).procesarConsultaByTitulo();
        verify(consolaSpy,times(1)).procesarConsultaTotal();
        verify(commonService,times(1)).volverMenuPrincipal();
        verify(commonService,never()).mostrarError();

        verify(consolaSpy,times(6)).mostrarMenuLibro();
        verify(commonUtil,times(6)).leerEntrada();

    }

    @Test
    void procesarAltaLibro_cuandoDatosValidos_deberiaInsertarLibroYMostrarMensaje (){
        // Arranque
        Libro libroMock = new Libro();
        libroMock.setTitulo("TITULO LIBRO");

        doReturn(libroMock).when(consolaSpy).solicitarDatosLibro();

        //Act
        consolaSpy.procesarAltaLibro();

        //Assert
        verify(libroService).insertarLibro(libroMock);
        verify(commonUtil).mostrarMensaje("Libro insertado correctamente.");

        verifyNoMoreInteractions(commonUtil);
    }

    @Test
    void procesarAltaLibro_cuandoViolacionRestricciones_deberiaMostrarMensajesDeError() {
        // Arranque
        ConstraintViolationException exceptionMock = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violacionMock = mock(ConstraintViolation.class);
        Path pathMock = mock(Path.class);

        when(pathMock.toString()).thenReturn("titulo");
        when(violacionMock.getPropertyPath()).thenReturn(pathMock);
        when(violacionMock.getMessage()).thenReturn("no puede estar vacío");
        when(exceptionMock.getConstraintViolations()).thenReturn(Set.of(violacionMock));

        doReturn(new Libro()).when(consolaSpy).solicitarDatosLibro();
        doThrow(exceptionMock).when(libroService).insertarLibro(any());

        // Act
        consolaSpy.procesarAltaLibro();

        // Assert
        verify(commonUtil).mostrarMensaje("Error al insertar el libro:");

        verifyNoMoreInteractions(commonUtil);
    }

    @Test
    void procesarAltaLibro_cuandoExcepcionGeneral_deberiaMostrarMensajeError() {
        // Arranque
        // Simulamos que solicitarDatosLibro devuelve algo válido
        Libro libroMock = new Libro();
        doReturn(libroMock).when(consolaSpy).solicitarDatosLibro();

        // Simulamos una excepción general al insertar
        Exception exception = new RuntimeException("Error inesperado");
        doThrow(exception).when(libroService).insertarLibro(libroMock);

        // Act
        consolaSpy.procesarAltaLibro();

        // Assert
        verify(commonUtil).mostrarMensajeError("alta libro", exception);
        verifyNoMoreInteractions(commonUtil);
    }

    @Test
    void procesarBajaLibro_cuandoDatosValidos_deberiaBajaLibroYMostrarMensaje (){
        // Arranque
        String titulo = "TITULO LIBRO";
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        Libro libroMock = new Libro();
        libroMock.setTitulo(titulo);
        libroMock.setIsbn("1234");
        doReturn(libroMock).when(consolaSpy).buscarLibro(titulo);
        doReturn(libroMock).when(libroService).buscarLibroByIsbn("1234");

        //Se mockea MaterialBiblioteca porque es una clase abstracta
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        doReturn(true).when(commonService).preguntarSiBorrar();


        //Act
        consolaSpy.procesarBajaLibro();

        //Assert
        verify(libroService).eliminarLibroById("1234");
        verify(commonUtil).mostrarMensaje("El libro ha sido eliminado correctamente.");

        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    void procesarBajaLibro_cuandoLibroNoExiste_deberiaMostrarMensajeError (){
        // Arranque
        String titulo = "TITULO LIBRO";
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        doReturn(null).when(consolaSpy).buscarLibro(titulo);

        //Act
        consolaSpy.procesarBajaLibro();

        //Assert

        //Verificamos que no se llama a ningún método posterior
        verify(libroService,never()).buscarLibroByIsbn(anyString());
        verify(libroService,never()).eliminarLibroById(anyString());
        verify(materialBibliotecaService,never()).obtenerMaterialDelPrestamoByTitulo(anyString());

        verify(commonService,never()).preguntarSiBorrar();

        verify(commonUtil,never()).mostrarMensaje(anyString());
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    void procesarBajaLibro_cuandoLibroEstaPrestado_deberiaMostrarMensajeError (){
        // Arranque
        String titulo = "TITULO LIBRO";
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        Libro libroMock = new Libro();
        libroMock.setTitulo("TITULO LIBRO");
        libroMock.setIsbn("1234");
        doReturn(libroMock).when(consolaSpy).buscarLibro(titulo);
        doReturn(libroMock).when(libroService).buscarLibroByIsbn(libroMock.getIsbn());

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(3);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //Act
        consolaSpy.procesarBajaLibro();

        //Assert
        // Para asegurar que mostrarMensaje solo se llama una vez. La segunda, que corresponde al try, no se llama
        verify(commonUtil, times(1)).mostrarMensaje("El libro \"TITULO LIBRO\" no se puede eliminar. Esta prestado.");
        //Verificamos que no se llama a ningún método posterior
        verify(commonService,never()).preguntarSiBorrar();
        verify(libroService,never()).eliminarLibroById(anyString());
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    void procesarBajaLibro_cuandoNoSeQuiereBorrar_deberiaMostrarMensajeError (){
        // Arranque
        String titulo = "TITULO LIBRO";
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        Libro libroMock = new Libro();
        libroMock.setTitulo("TITULO LIBRO");
        libroMock.setIsbn("1234");
        doReturn(libroMock).when(consolaSpy).buscarLibro(titulo);
        doReturn(libroMock).when(libroService).buscarLibroByIsbn(libroMock.getIsbn());

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        doReturn(false).when(commonService).preguntarSiBorrar();

        //Act
        consolaSpy.procesarBajaLibro();

        //Assert
        // Para asegurar que mostrarMensaje solo se llama una vez. La segunda, que corresponde al try, no se llama
        verify(commonUtil, times(1)).mostrarMensaje("El libro \"TITULO LIBRO\" no ha sido eliminado.");
        //Verificamos que no se llama a ningún método posterior
        verify(libroService,never()).eliminarLibroById(anyString());
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    void procesarBajaLibro_cuandoExcepcion_deberiaMostrarMensajeError (){
        // Arranque
        String titulo = "TITULO LIBRO";
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        Libro libroMock = new Libro();
        libroMock.setTitulo(titulo);
        libroMock.setIsbn("1234");
        doReturn(libroMock).when(consolaSpy).buscarLibro(titulo);
        doReturn(libroMock).when(libroService).buscarLibroByIsbn("1234");

        //Se mockea MaterialBiblioteca porque es una clase abstracta
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        doReturn(true).when(commonService).preguntarSiBorrar();

        // Simulamos una excepción general al insertar
        Exception exception = new RuntimeException("Error inesperado");
        doThrow(exception).when(libroService).eliminarLibroById("1234");

        //Act
        consolaSpy.procesarBajaLibro();

        //Assert
        verify(commonUtil).mostrarMensajeError("baja libro", exception);
        verify(commonUtil,never()).mostrarMensaje(anyString());

    }

    @Test
    void procesarConsultaByIsbn_cuandoLibroExiste_deberiaMostrarDatosPorConsola() {
        // Redirigir salida estándar
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Arranque
        String isbn = "1234";

        doReturn(isbn).when(commonUtil).leerEntrada();
        doReturn(isbn).when(consolaSpy).solicitarLibroByIsbn();

        Libro libroMock = new Libro();
        libroMock.setIsbn(isbn);
        libroMock.setTitulo("Título de prueba");
        libroMock.setAñoPublicacion(2020);
        libroMock.setTotales(5);
        libroMock.setDisponibles(5);
        libroMock.setAutores(List.of(new Autor("Autor Uno"), new Autor("Autor Dos")));

        doReturn(libroMock).when(libroService).buscarLibroByIsbn(isbn);

        // Act
        consolaSpy.procesarConsultaByIsbn();

        // Assert
        String salida = outContent.toString();
        assertTrue(salida.contains("Datos del Libro"));
        assertTrue(salida.contains("ISBN: 1234"));
        assertTrue(salida.contains("Título: Título de prueba"));
        assertTrue(salida.contains("Año de publicación: 2020"));
        assertTrue(salida.contains("Autores:"));
        assertTrue(salida.contains("- Autor Uno"));
        assertTrue(salida.contains("- Autor Dos"));
        assertTrue(salida.contains("Número de ejemplares totales: 5"));
        assertTrue(salida.contains("Número de ejemplares disponibles: 5"));

        // Restaurar salida estándar
        System.setOut(System.out);
    }

    @Test
    void procesarConsultaByIsbn_cuandoLibroNoExiste_deberiaMostrarMensaje () {
        // Redirigir salida estándar
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Arranque
        String isbn = "1234";

        doReturn(isbn).when(commonUtil).leerEntrada();
        doReturn(isbn).when(consolaSpy).solicitarLibroByIsbn();

        doReturn(null).when(libroService).buscarLibroByIsbn(isbn);

        // Act
        consolaSpy.procesarConsultaByIsbn();

        // Assert
        verify(commonUtil).mostrarMensaje("Libro inexistente en la biblioteca");
        //Verficamos que no se ejecuta mostrarDatos()
        verify(consolaSpy,never()).mostrarDatos(any());

        // Restaurar salida estándar
        System.setOut(System.out);
    }

    @Test
    void procesarConsultaByTitulo_cuandoLibroExiste_deberiaMostrarDatosPorConsola() {
        // Redirigir salida estándar
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Arranque
        String titulo = "TITULO PRUEBA";

        doReturn(titulo).when(commonUtil).leerEntrada();
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        Libro libroMock = new Libro();
        libroMock.setIsbn("1234");
        libroMock.setTitulo("TITULO PRUEBA");
        libroMock.setAñoPublicacion(2020);
        libroMock.setTotales(5);
        libroMock.setDisponibles(5);
        libroMock.setAutores(List.of(new Autor("Autor Uno"), new Autor("Autor Dos")));


        doReturn(libroMock).when(libroService).buscarLibroByTitulo("TITULO PRUEBA");
        doReturn(libroMock).when(consolaSpy).buscarLibro("TITULO PRUEBA");
        doReturn(libroMock).when(libroService).buscarLibroByIsbn("1234");

        // Act
        consolaSpy.procesarConsultaByTitulo();

        // Assert
        String salida = outContent.toString();
        assertTrue(salida.contains("Datos del Libro"));
        assertTrue(salida.contains("ISBN: 1234"));
        assertTrue(salida.contains("Título: TITULO PRUEBA"));
        assertTrue(salida.contains("Año de publicación: 2020"));
        assertTrue(salida.contains("Autores:"));
        assertTrue(salida.contains("- Autor Uno"));
        assertTrue(salida.contains("- Autor Dos"));
        assertTrue(salida.contains("Número de ejemplares totales: 5"));
        assertTrue(salida.contains("Número de ejemplares disponibles: 5"));

        // Restaurar salida estándar
        System.setOut(System.out);
    }

    @Test
    void procesarConsultaByTitulo_cuandoLibroNoExiste_deberiaMostrarMensajeError() {
        // Arranque
        String titulo = "TITULO PRUEBA";

        //Simulamos que se solicita el título
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        //Simulamos que no se encuentra el libro en el servicio
        doReturn(null).when(libroService).buscarLibroByTitulo(titulo);

        // Act
        consolaSpy.procesarConsultaByTitulo();

        // Assert
        verify(commonUtil).mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
        // Verificamos que no se llama al resto de procesos posteriores
        verify(libroService,never()).buscarLibroByIsbn(anyString());
        verify(consolaSpy,never()).mostrarDatos(any());

    }

    @Test
    void procesarConsultaByTitulo_cuandoLibroNoExiste_deberiaNoMostrarMensajeError() {
        // Arranque
        String titulo = "TITULO PRUEBA";

        Libro libroMock = new Libro();
        libroMock.setIsbn("1234");

        //Simulamos que se solicita el título
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        //Simulamos que no se encuentra el libro en el servicio
        doReturn(libroMock).when(libroService).buscarLibroByTitulo(titulo);

        //Simulamos que no se encuentra el libro en el servicio
        doReturn(null).when(libroService).buscarLibroByIsbn("1234");

        // Act
        consolaSpy.procesarConsultaByTitulo();

        // Assert
        // Verificamos que no se ejecuta
        verify(consolaSpy,never()).mostrarDatos(any());

    }

    @Test
    void procesarConsultaTotal_cuandoLosLibrosExisten_deberiaMostrarDatosPorConsola() {
        // Redirigir salida estándar
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Arranque

        Libro libroCompleto1Mock = new Libro();
        libroCompleto1Mock.setIsbn("1234");
        libroCompleto1Mock.setTitulo("TITULO 1 PRUEBA");
        libroCompleto1Mock.setAñoPublicacion(2020);
        libroCompleto1Mock.setTotales(5);
        libroCompleto1Mock.setDisponibles(5);
        libroCompleto1Mock.setAutores(List.of(new Autor("Autor Uno"), new Autor("Autor Dos")));

        Libro libroCompleto2Mock = new Libro();
        libroCompleto2Mock.setIsbn("5678");
        libroCompleto2Mock.setTitulo("TITULO 2 PRUEBA");
        libroCompleto2Mock.setAñoPublicacion(2021);
        libroCompleto2Mock.setTotales(6);
        libroCompleto2Mock.setDisponibles(6);
        libroCompleto2Mock.setAutores(List.of(new Autor("Autor Tres"), new Autor("Autor Cuatro")));

        List<Libro> libroListMock = List.of(libroCompleto1Mock, libroCompleto2Mock);

        doReturn(libroListMock).when(libroService).listarLibros();
        doReturn(libroCompleto1Mock).when(libroService).buscarLibroByIsbn("1234");
        doReturn(libroCompleto2Mock).when(libroService).buscarLibroByIsbn("5678");

        // Act
        consolaSpy.procesarConsultaTotal();

        // Assert
        String salida = outContent.toString();
        assertTrue(salida.contains("Datos del Libro"));
        assertTrue(salida.contains("Título: TITULO 1 PRUEBA"));
        assertTrue(salida.contains("Año de publicación: 2020"));
        assertTrue(salida.contains("Autores:"));
        assertTrue(salida.contains("- Autor Uno"));
        assertTrue(salida.contains("- Autor Dos"));
        assertTrue(salida.contains("Número de ejemplares totales: 5"));
        assertTrue(salida.contains("Número de ejemplares disponibles: 5"));

        assertTrue(salida.contains("Datos del Libro"));
        assertTrue(salida.contains("Título: TITULO 2 PRUEBA"));
        assertTrue(salida.contains("Año de publicación: 2021"));
        assertTrue(salida.contains("Autores:"));
        assertTrue(salida.contains("- Autor Tres"));
        assertTrue(salida.contains("- Autor Cuatro"));
        assertTrue(salida.contains("Número de ejemplares totales: 6"));
        assertTrue(salida.contains("Número de ejemplares disponibles: 6"));

        // Restaurar salida estándar
        System.setOut(System.out);
    }

    @Test
    void procesarConsultaTotal_cuandoNoHayLibros_deberiaMostrarMensajeError() {
        //Devuelve una lista vacía
        doReturn(List.of()).when(libroService).listarLibros();

        // Act
        consolaSpy.procesarConsultaTotal();

        // Assert
        verify(commonUtil).mostrarMensaje("No hay libros en la biblioteca");
        // Verificamos que no se llama al resto de procesos posteriores
        verify(libroService, never()).buscarLibroByIsbn(anyString());
        verify(consolaSpy,never()).mostrarDatos(any());

    }

    @Test
    void solicitarDatosLibro_cuandoDatosValidos_deberiaDevolverLibroCompleto() {
        // Arranque

        // Simulamos entradas del usuario
        when(commonUtil.leerEntrada())
                .thenReturn("1234")         // ISBN
                .thenReturn("Título de prueba") // Título
                .thenReturn("Autor Uno")    // Autor 1
                .thenReturn("S")            // ¿Otro autor?
                .thenReturn("Autor Dos")    // Autor 2
                .thenReturn("N");           // Fin autores

        when(commonUtil.leerEntero())
                .thenReturn(2020)           // Año de publicación
                .thenReturn(5);             // Número de ejemplares

        // Simulamos autores válidos
        Autor autor1 = new Autor("Autor Uno");
        Autor autor2 = new Autor("Autor Dos");

        when(autorService.buscarAutorByNombre("Autor Uno")).thenReturn(autor1);
        when(autorService.buscarAutorByNombre("Autor Dos")).thenReturn(autor2);

        // Act
        Libro libro = consolaSpy.solicitarDatosLibro();

        // Assert
        assertEquals("1234", libro.getIsbn());
        assertEquals("Título de prueba", libro.getTitulo());
        assertEquals(2020, libro.getAñoPublicacion());
        assertEquals(5, libro.getTotales());
        assertEquals(5, libro.getDisponibles());

        List<Autor> autores = libro.getAutores();
        assertNotNull(autores);
        assertEquals(2, autores.size());
        assertTrue(autores.stream().anyMatch(a -> a.getNombre().equals("Autor Uno")));
        assertTrue(autores.stream().anyMatch(a -> a.getNombre().equals("Autor Dos")));
    }

    @Test
    void solicitarDatosLibro_cuandoAutorNoExiste_deberiaMostrarMensajeError() {
        // Arranque

        // Simulamos entradas del usuario
        when(commonUtil.leerEntrada())
                .thenReturn("1234")         // ISBN
                .thenReturn("Título de prueba") // Título
                .thenReturn("Autor Uno")    // Autor 1
                .thenReturn("N");           // Fin autores

        when(commonUtil.leerEntero())
                .thenReturn(2020);          // Año de publicación


        // Simulamos autor no valido
        Autor autor1 = new Autor("Autor Uno");

        doReturn(null).when(autorService).buscarAutorByNombre("Autor Uno");

        // Act
        Libro libro = consolaSpy.solicitarDatosLibro();

        // Assert
        verify(commonUtil,times(1)).mostrarMensaje("El autor \"" + autor1.getNombre() + "\" no existe en la biblioteca");

    }

    @Test
    void buscarLibro_cuandoExisteLibro_deberiaDevolverLibro() {
        // Arranque

        String titulo = "TITULO PRUEBA";

        Libro libroCompletoMock = new Libro();
        libroCompletoMock.setIsbn("1234");
        libroCompletoMock.setTitulo("TITULO PRUEBA");
        libroCompletoMock.setAñoPublicacion(2020);
        libroCompletoMock.setTotales(5);
        libroCompletoMock.setDisponibles(5);
        libroCompletoMock.setAutores(List.of(new Autor("Autor Uno"), new Autor("Autor Dos")));

        doReturn(libroCompletoMock).when(libroService).buscarLibroByTitulo(titulo);

        // Act
        Libro libro = consolaSpy.buscarLibro(titulo);

        // Assert
        assertEquals("1234", libro.getIsbn());
        assertEquals("TITULO PRUEBA", libro.getTitulo());
        assertEquals(2020, libro.getAñoPublicacion());
        assertEquals(5, libro.getTotales());
        assertEquals(5, libro.getDisponibles());

        List<Autor> autores = libro.getAutores();
        assertNotNull(autores);
        assertEquals(2, autores.size());
        assertTrue(autores.stream().anyMatch(a -> a.getNombre().equals("Autor Uno")));
        assertTrue(autores.stream().anyMatch(a -> a.getNombre().equals("Autor Dos")));

        assertNotNull(libro);
        verify(commonUtil,never()).mostrarMensaje(anyString());
    }

    @Test
    void buscarLibro_cuandoNoExisteLibro_deberiaMostrarMensaje() {
        // Arranque
        String titulo = "TITULO PRUEBA";

        doReturn(null).when(libroService).buscarLibroByTitulo(titulo);

        // Act
        Libro libro = consolaSpy.buscarLibro(titulo);

        // Assert
        verify(commonUtil).mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
        //Se devuelve un libro null
        assertNull(libro);
    }

    @Test
    void procesarSolicitarLibroByIsbn_cuandoTieneTitulo_deberiaDevolverElTitulo() {
        // Arranque
        String isbn = "1234";
        doReturn(isbn).when(commonUtil).leerEntrada();

        // Act
        isbn = consolaSpy.solicitarLibroByIsbn();

        // Assert
        assertEquals("1234", isbn);
        assertNotNull(isbn);

        // Assert: interacciones y orden
        InOrder inOrder = inOrder(commonUtil);
        inOrder.verify(commonUtil).mostrarMensaje("Isbn del libro");
        inOrder.verify(commonUtil).leerEntrada();
    }

    @Test
    void procesarSolicitarLibroByTitulo_cuandoTieneTitulo_deberiaDevolverElTitulo() {
        // Arranque
        String titulo = "TITULO PRUEBA";
        doReturn(titulo).when(commonUtil).leerEntrada();

        // Act
        titulo = consolaSpy.solicitarLibroByTitulo();

        // Assert
        assertEquals("TITULO PRUEBA", titulo);
        assertNotNull(titulo);

        // Assert: interacciones y orden
        InOrder inOrder = inOrder(commonUtil);
        inOrder.verify(commonUtil).mostrarMensaje("Titulo del libro");
        inOrder.verify(commonUtil).leerEntrada();
    }
}
