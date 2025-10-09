package com.example.biblioteca.component;

import com.example.biblioteca.model.*;
import com.example.biblioteca.service.*;
import com.example.biblioteca.util.CommonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsolaPrestamoTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @Mock
    private PrestamoService prestamoService;
    @Mock
    private MaterialBibliotecaService materialBibliotecaService;
    @Mock
    private LibroService libroService;
    @Mock
    private RevistaService revistaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private CommonService commonService;
    @Mock
    private CommonUtil commonUtil;

    @Spy
    @InjectMocks
    private ConsolaPrestamo consolaSpy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void menuPrestamo(){
        when(commonUtil.leerEntrada()).thenReturn("A","B","C","D","S");

        doNothing().when(consolaSpy).procesarAltaPrestamo();
        doNothing().when(consolaSpy).procesarDevolucionPrestamo();
        doNothing().when(consolaSpy).procesarConsultaPorUsuario();
        doNothing().when(consolaSpy).procesarConsultaPorTitulo();
        doNothing().when(commonService).volverMenuPrincipal();

        consolaSpy.menuPrestamo();

        //Assert
        verify(consolaSpy,times(1)).procesarAltaPrestamo();
        verify(consolaSpy,times(1)).procesarDevolucionPrestamo();
        verify(consolaSpy,times(1)).procesarConsultaPorUsuario();
        verify(consolaSpy,times(1)).procesarConsultaPorTitulo();
        verify(commonService,times(1)).volverMenuPrincipal();
        verify(commonService,never()).mostrarError();

        //Verificamos que se ejecuta 5 veces el leerEntrada()
        verify(commonUtil,times(5)).leerEntrada();
        //Verficamos que se ejecuta 5 veces el mostrarMenuPrestamo()
        verify(consolaSpy,times(5)).mostrarMenuPrestamo();

    }

    @Test
    public void procesarAltaPrestamo_cuandoDatosValidos_deberiaRegistrarPrestamosYMostrarMensaje() {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        //Se solicita el titulo del préstamo
        doReturn(materialMock).when(consolaSpy).solicitarTituloPrestamo();

        //Existe el material y hay ejemplares disponibles
        doReturn(true).when(consolaSpy).comprobarExistencia(materialMock);

        //Solicitar datos del usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //Crear el préstamo
        Prestamo prestamoMock = new Prestamo();
        doReturn(prestamoMock).when(consolaSpy).crearPrestamo(usuarioMock, materialMock);

        //Act
        consolaSpy.procesarAltaPrestamo();

        //Assert
        verify(consolaSpy).registrarPrestamo(prestamoMock, materialMock);
        verify(commonUtil).mostrarMensaje("Préstamo registrado correctamente");
        //Verificamos que no se ejecutan los procesos
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());


    }

    @Test
    public void procesarAltaPrestamo_cuandoNoExisteElMaterial_deberiaNoHacerNada() {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        //Se solicita el titulo del préstamo
        doReturn(materialMock).when(consolaSpy).solicitarTituloPrestamo();

        //No existe el material o no hay ejemplares disponibles
        doReturn(false).when(consolaSpy).comprobarExistencia(materialMock);

        //Act
        consolaSpy.procesarAltaPrestamo();

        //Assert
        //Verificamos que no se ejecutan los procesos posteriores
        verify(consolaSpy, never()).solicitarDatosUsuario();
        verify(consolaSpy, never()).crearPrestamo(any(), any());
        verify(consolaSpy, never()).registrarPrestamo(any(), any());

        //Verificamos que no hay ninguna iteración con commonUtil
        verifyNoInteractions(commonUtil);

    }

    @Test
    public void procesarAltaPrestamo_cuandoUsuarioNoExiste_deberiaNoHacerNada() {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        //Se solicita el titulo del préstamo
        doReturn(materialMock).when(consolaSpy).solicitarTituloPrestamo();

        //Existe el material y hay ejemplares disponibles
        doReturn(true).when(consolaSpy).comprobarExistencia(materialMock);

        //El usuario no existe
        doReturn(null).when(consolaSpy).solicitarDatosUsuario();

        //Act
        consolaSpy.procesarAltaPrestamo();

        //Assert
        //Verificamos que no se ejecutan los procesos posteriores.
        verify(consolaSpy, never()).crearPrestamo(any(), any());
        verify(consolaSpy, never()).registrarPrestamo(any(), any());

        //Verificamos que no hay ninguna iteración con commonUtil
        verifyNoInteractions(commonUtil);

    }

    @Test
    public void procesarAltaPrestamo_cuandoHayExcepcion_deberiaMostrarMensajeError() {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        //Se solicita el titulo del préstamo
        doReturn(materialMock).when(consolaSpy).solicitarTituloPrestamo();

        //Existe el material y hay ejemplares disponibles
        doReturn(true).when(consolaSpy).comprobarExistencia(materialMock);

        //El usuario existe
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //Alta del préstamo
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(1);

        doReturn(prestamoMock).when(consolaSpy).crearPrestamo(usuarioMock, materialMock);

        //registarPrestamo da una excepcion
        Exception exception = new RuntimeException();
        doThrow(exception).when(consolaSpy).registrarPrestamo(prestamoMock, materialMock);

        //Act
        consolaSpy.procesarAltaPrestamo();

        //Assert
        verify(commonUtil, times(1)).mostrarMensajeError("alta préstamo", exception);

        //Verificamos que no se ejecutan los procesos posteriores.
        verify(commonUtil, never()).mostrarMensaje(anyString());

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoDatosValidos_deberiaActualizarDisponibilidadYPrestamoYMostrarMensaje(){
        String titulo = "TITULO PRUEBA";
        doReturn(titulo).when(consolaSpy).obtenerTituloMaterial();

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getDisponibles()).thenReturn(1);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //El material esta prestado
        doReturn(true).when(consolaSpy).validarMaterialPrestado(materialMock,titulo);

        //El usuario existe
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(42);
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //Se obtienen todos los prestamos activos del usuario
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(100);

        List<Prestamo> prestamoListMock = List.of(prestamoMock);
        doReturn(prestamoListMock).when(consolaSpy).obtenerPrestamoList(usuarioMock.getId());

        List<Integer> idPrestamosListMock = List.of(prestamoMock.getId());
        List<Integer> idMaterialListMock = List.of(materialMock.getId());

        when(prestamoService.obtenerMaterialIdsPorPrestamos(idPrestamosListMock))
                .thenReturn(idMaterialListMock);

        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        verify(materialBibliotecaService).actualizarDisponible(materialMock, 2);
        verify(prestamoService).desactivarPrestamoConMaterial(idPrestamosListMock, materialMock.getId());
        verify(commonUtil).mostrarMensaje("Préstamo devuelto");

        ///Verificamos que no se ejecuta ningún proceso posterior
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoNoSeObtieneTituloMaterial_deberiaNoEjecutarNingunaAccion() {
        //No existe el titulo
        doReturn(null).when(consolaSpy).obtenerTituloMaterial();

        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        //Verificamos que no se ejecuta ningún proceso posterior
        verify(materialBibliotecaService, never()).obtenerMaterialDelPrestamoByTitulo(anyString());
        verify(materialBibliotecaService, never()).actualizarDisponible(any(), anyInt());
        verify(prestamoService, never()).obtenerMaterialIdsPorPrestamos(anyList());
        verify(prestamoService, never()).desactivarPrestamoConMaterial(anyList(), anyInt());

        verify(consolaSpy, never()).validarMaterialPrestado(any(), anyString());
        verify(consolaSpy, never()).solicitarDatosUsuario();
        verify(consolaSpy, never()).obtenerPrestamoList(anyInt());

        verify(commonUtil,never()).mostrarMensaje(anyString());
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

        //Aseguramos que ningún método de un mock fue llamado
        verifyNoInteractions(materialBibliotecaService, prestamoService, commonUtil);

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoMaterialNoExisteONoEstaPrestado_deberiaNoEjecutarNingunaAccion () {
        //Se obtiene el titulo del material
        String titulo = "TITULO PRUEBA";
        when(consolaSpy.obtenerTituloMaterial()).thenReturn(titulo);

        //Se obtiene el material.
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        materialMock.setId(1);
        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);

        //No se valida el material prestado. O no existe o no esta prestado.
        when(consolaSpy.validarMaterialPrestado(materialMock, titulo)).thenReturn(false);

        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        //Verificamos que no se ejecutan los procesos posteriores
        verify(prestamoService,never()).obtenerMaterialIdsPorPrestamos(anyList());
        verify(prestamoService,never()).desactivarPrestamoConMaterial(anyList(), anyInt());
        verify(materialBibliotecaService,never()).actualizarDisponible(any(), anyInt());

        verify(consolaSpy,never()).solicitarDatosUsuario();
        verify(consolaSpy,never()).obtenerPrestamoList(anyInt());

        //No se puede poner en mostrarMensaje() un anyString() porque si se usa en otros metodos
        verify(commonUtil,never()).mostrarMensaje("Préstamo devuelto");
        verify(commonUtil,never()).mostrarMensajeError(anyString(),any());

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoUsuarioNoExiste_deberiaNoEjecutarNingunaAccion () {
        //Se obtiene el titulo del material
        String titulo = "TITULO PRUEBA";
        when(consolaSpy.obtenerTituloMaterial()).thenReturn(titulo);

        //Se obtiene el material.
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        materialMock.setId(1);
        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);

        //No se valida el material prestado. O no existe o no esta prestado.
        when(consolaSpy.validarMaterialPrestado(materialMock, titulo)).thenReturn(true);

        //Usuario no existe
        doReturn(null).when(consolaSpy).solicitarDatosUsuario();

        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        //Verificamos que no se ejecutan los procesos posteriores
        verify(prestamoService,never()).obtenerMaterialIdsPorPrestamos(anyList());
        verify(prestamoService,never()).desactivarPrestamoConMaterial(anyList(), anyInt());
        verify(materialBibliotecaService,never()).actualizarDisponible(any(), anyInt());

        verify(consolaSpy,never()).obtenerPrestamoList(anyInt());

        //No se puede poner en mostrarMensaje() un anyString() porque si se usa en otros metodos
        verify(commonUtil,never()).mostrarMensaje("Préstamo devuelto");
        verify(commonUtil,never()).mostrarMensajeError(anyString(),any());

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoNoSeObtieneListaMaterialprestado_deberiaNoEjecutarNingunaAccion () {
        //Se obtiene el titulo del material
        String titulo = "TITULO PRUEBA";
        when(consolaSpy.obtenerTituloMaterial()).thenReturn(titulo);

        //Se obtiene el material.
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        materialMock.setId(1);
        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);

        //No se valida el material prestado. O no existe o no esta prestado.
        when(consolaSpy.validarMaterialPrestado(materialMock, titulo)).thenReturn(true);

        //Usuario existe
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(99);
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //No se obtiene list con material prestado
        doReturn(null).when(consolaSpy).obtenerPrestamoList(usuarioMock.getId());

        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        //Verificamos que no se ejecutan los procesos posteriores
        verify(prestamoService,never()).obtenerMaterialIdsPorPrestamos(anyList());
        verify(prestamoService,never()).desactivarPrestamoConMaterial(anyList(), anyInt());
        verify(materialBibliotecaService,never()).actualizarDisponible(any(), anyInt());

        //No se puede poner en mostrarMensaje() un anyString() porque si se usa en otros metodos
        verify(commonUtil,never()).mostrarMensaje("Préstamo devuelto");
        verify(commonUtil,never()).mostrarMensajeError(anyString(),any());

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoNoTienePrestado_deberiaNoEjecutarNingunaAccion (){
        String titulo = "TITULO PRUEBA";
        doReturn(titulo).when(consolaSpy).obtenerTituloMaterial();

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getDisponibles()).thenReturn(1);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //El material esta prestado
        doReturn(true).when(consolaSpy).validarMaterialPrestado(materialMock,titulo);

        //El usuario existe
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(42);
        usuarioMock.setNombre("FRANCISCO GARCIA");
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //Se obtienen todos los prestamos activos del usuario
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(100);

        List<Prestamo> prestamoListMock = List.of(prestamoMock);
        doReturn(prestamoListMock).when(consolaSpy).obtenerPrestamoList(usuarioMock.getId());

        List<Integer> idPrestamosListMock = List.of(prestamoMock.getId());
        List<Integer> idMaterialListMock = List.of(2);

        when(prestamoService.obtenerMaterialIdsPorPrestamos(idPrestamosListMock))
                .thenReturn(idMaterialListMock);


        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        verify(consolaSpy).mostrarMensajeNoTienePrestado("FRANCISCO GARCIA", "TITULO PRUEBA");

        ///Verificamos que no se ejecuta ningún proceso posterior
        verify(materialBibliotecaService,never()).actualizarDisponible(any(), anyInt());
        verify(prestamoService,never()).desactivarPrestamoConMaterial(anyList(), anyInt());

        verify(commonUtil,never()).mostrarMensaje("Préstamo devuelto");
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    public void procesarDevolucionPrestamo_cuandoHayExcepcion_deberiaMostrarMensajeError (){
        String titulo = "TITULO PRUEBA";
        doReturn(titulo).when(consolaSpy).obtenerTituloMaterial();

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getDisponibles()).thenReturn(1);
        doReturn(materialMock).when(materialBibliotecaService).obtenerMaterialDelPrestamoByTitulo(titulo);

        //El material esta prestado
        doReturn(true).when(consolaSpy).validarMaterialPrestado(materialMock,titulo);

        //El usuario existe
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(42);
        usuarioMock.setNombre("FRANCISCO GARCIA");
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //Se obtienen todos los prestamos activos del usuario
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(100);

        List<Prestamo> prestamoListMock = List.of(prestamoMock);
        doReturn(prestamoListMock).when(consolaSpy).obtenerPrestamoList(usuarioMock.getId());

        List<Integer> idPrestamosListMock = List.of(prestamoMock.getId());
        List<Integer> idMaterialListMock = List.of(materialMock.getId());

        when(prestamoService.obtenerMaterialIdsPorPrestamos(idPrestamosListMock))
                .thenReturn(idMaterialListMock);

        Exception exception = new RuntimeException();
        doThrow(exception).when(materialBibliotecaService).actualizarDisponible(materialMock, 2);

        //Act
        consolaSpy.procesarDevolucionPrestamo();

        //Assert
        verify(commonUtil).mostrarMensajeError("devolución préstamo", exception);

        ///Verificamos que no se ejecuta ningún proceso posterior
        verify(prestamoService,never()).desactivarPrestamoConMaterial(anyList(), anyInt());
        verify(commonUtil,never()).mostrarMensaje("Préstamo devuelto");

    }

    @Test
    public void procesarConsultaPorUsuario_cuandoDatosValidos_deberiaMostrarPrestamos (){
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setId(1);

        when(consolaSpy.solicitarDatosUsuario()).thenReturn(usuarioMock);

        MaterialBiblioteca material1Mock = mock(MaterialBiblioteca.class);
        when(material1Mock.getId()).thenReturn(1);
        when(material1Mock.getTitulo()).thenReturn("TITULO 1");

        MaterialBiblioteca material2Mock = mock(MaterialBiblioteca.class);
        when(material2Mock.getId()).thenReturn(2);
        when(material2Mock.getTitulo()).thenReturn("TITULO 2");


        Prestamo prestamo1Mock = new Prestamo();
        prestamo1Mock.setId(10);
        prestamo1Mock.setUsuario(usuarioMock);
        prestamo1Mock.setMateriales(List.of(material1Mock, material2Mock));

        List<Prestamo> prestamoListMock = List.of(prestamo1Mock);

        when(consolaSpy.obtenerPrestamoList(1)).thenReturn(prestamoListMock);

        when(materialBibliotecaService.obtenerMaterialesDelPrestamoById(10)).thenReturn(List.of(material1Mock, material2Mock));

        //Act
        consolaSpy.procesarConsultaPorUsuario();

        //Assert
        String actual = outContent.toString(StandardCharsets.UTF_8);

        assertTrue(actual.contains("Material id: 1"));
        assertTrue(actual.contains("Título: TITULO 1"));
        assertTrue(actual.contains("Material id: 2"));
        assertTrue(actual.contains("Título: TITULO 2"));

        //Verificamos que no se ejecuta ningún proceso posterior
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    public void procesarConsultaPorUsuario_cuandoUsuarioNoExiste_deberiaNoEjecutarNingunaAccion () {
        when(consolaSpy.solicitarDatosUsuario()).thenReturn(null);

        //Act
        consolaSpy.procesarConsultaPorUsuario();

        //Assert
        verify(consolaSpy,never()).obtenerPrestamoList(anyInt());
        verify(materialBibliotecaService,never()).obtenerMaterialesDelPrestamoById(anyInt());
        verify(commonUtil,never()).mostrarMensajeError(anyString(),any());

    }

    @Test
    public void procesarConsultaPorUsuario_cuandoNoHayPrestamos_deberiaNoEjecutarNingunaAccion () {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1);

        when(consolaSpy.solicitarDatosUsuario()).thenReturn(usuarioMock);

        when(consolaSpy.obtenerPrestamoList(usuarioMock.getId())).thenReturn(null);

        //Act
        consolaSpy.procesarConsultaPorUsuario();

        //Assert
        verify(materialBibliotecaService,never()).obtenerMaterialesDelPrestamoById(anyInt());
        verify(commonUtil,never()).mostrarMensajeError(anyString(),any());

    }

    @Test
    public void procesarConsultaPorUsuario_cuandoHayExcepcion_deberiaMostrarMensajeError (){
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setId(1);

        when(consolaSpy.solicitarDatosUsuario()).thenReturn(usuarioMock);

        MaterialBiblioteca material1Mock = mock(MaterialBiblioteca.class);
        when(material1Mock.getId()).thenReturn(1);
        when(material1Mock.getTitulo()).thenReturn("TITULO 1");

        MaterialBiblioteca material2Mock = mock(MaterialBiblioteca.class);
        when(material2Mock.getId()).thenReturn(2);
        when(material2Mock.getTitulo()).thenReturn("TITULO 2");


        Prestamo prestamo1Mock = new Prestamo();
        prestamo1Mock.setId(10);
        prestamo1Mock.setUsuario(usuarioMock);
        prestamo1Mock.setMateriales(List.of(material1Mock, material2Mock));

        List<Prestamo> prestamoListMock = List.of(prestamo1Mock);

        when(consolaSpy.obtenerPrestamoList(1)).thenReturn(prestamoListMock);

        Exception exception = new RuntimeException();
        doThrow(exception).when(materialBibliotecaService).obtenerMaterialesDelPrestamoById(10);

        //Act
        consolaSpy.procesarConsultaPorUsuario();

        //Assert
        verify(commonUtil).mostrarMensajeError("consulta por usuario", exception);

        //Verificamos que no se imprime los materiales
        String actual = outContent.toString(StandardCharsets.UTF_8);

        assertFalse(actual.contains("Material id: 1"));
        assertFalse(actual.contains("Título: TITULO 1"));
        assertFalse(actual.contains("Material id: 2"));
        assertFalse(actual.contains("Título: TITULO 2"));
    }

    @Test
    public void procesarConsultaPorTitulo_cuandoDatosValidos_deberiaMostrarMensaje () {
        String titulo = "TITULO PRUEBA";
        Libro libroMock = new Libro();
        when(consolaSpy.solicitarLibroByTitulo()).thenReturn(titulo);

        when(libroService.buscarLibroByTitulo(titulo)).thenReturn(libroMock);

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);

        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);
        doReturn(true).when(consolaSpy).validarMaterialPrestado(materialMock, titulo);

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(1);
        prestamoMock.setActivo(true);
        prestamoMock.setUsuario(usuarioMock);
        List<Prestamo> prestamoListMock = List.of(prestamoMock);

        doReturn(prestamoListMock).when(prestamoService).buscarPrestamoByMaterialId(1);

        //Act
        consolaSpy.procesarConsultaPorTitulo();

        //Assert
        verify(commonUtil).mostrarMensaje("El libro \"" + titulo + "\" está prestado a " + usuarioMock.getNombre());

    }

    @Test
    public void procesarConsultaPorTitulo_cuandoLibroNoExiste_deberiaMostrarMensaje () {
        String titulo = "TITULO PRUEBA";
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        //No encuentra el libro
        doReturn(null).when(libroService).buscarLibroByTitulo(titulo);

        //Act
        consolaSpy.procesarConsultaPorTitulo();

        //Assert
        verify(commonUtil).mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
        //Verificamos que el resto de procesos posteriores no se ejecutan
        verify(materialBibliotecaService,never()).obtenerMaterialDelPrestamoByTitulo(anyString());
        verify(prestamoService,never()).buscarPrestamoByMaterialId(anyInt());
        verifyNoMoreInteractions(commonUtil);
    }

    @Test
    public void procesarConsultaPorTitulo_cuandoLibroNoPrestado_deberiaMostrarMensaje () {
        String titulo = "TITULO PRUEBA";
        Libro libroMock = new Libro();
        when(consolaSpy.solicitarLibroByTitulo()).thenReturn(titulo);

        when(libroService.buscarLibroByTitulo(titulo)).thenReturn(libroMock);

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);

        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);
        doReturn(false).when(consolaSpy).validarMaterialPrestado(materialMock, titulo);

        //Act
        consolaSpy.procesarConsultaPorTitulo();

        //Assert
        verify(prestamoService, never()).buscarPrestamoByMaterialId(anyInt());
        verify(commonUtil,never()).mostrarMensaje(contains("está prestado a "));
    }

    @Test
    public void procesarConsultaPorTitulo_cuandoNohayPrestamosActivos_deberiaMostrarMensaje () {
        String titulo = "TITULO PRUEBA";
        Libro libroMock = new Libro();
        when(consolaSpy.solicitarLibroByTitulo()).thenReturn(titulo);

        when(libroService.buscarLibroByTitulo(titulo)).thenReturn(libroMock);

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);

        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);
        doReturn(true).when(consolaSpy).validarMaterialPrestado(materialMock, titulo);

        //Devuelve una lista vacía. No hay préstamos activos para ese material
        doReturn(List.of()).when(prestamoService).buscarPrestamoByMaterialId(1);

        //Act
        consolaSpy.procesarConsultaPorTitulo();

        //Assert
        verify(commonUtil).mostrarMensaje("No hay prestamos activos para ese material");
        verify(commonUtil,never()).mostrarMensaje(contains("está prestado a "));
    }

    @Test
    public void obtenerTituloMaterial_cuandoMaterialEsLibroYDatosValidos_deberiaDevolverElTitulo (){
        String titulo = "TITULO LIBRO";
        doReturn(true).when(consolaSpy).solicitarDevolucionMaterial();

        //Solicitar el titulo del libro
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        Libro libroMock = new Libro();
        libroMock.setTitulo("TITULO LIBRO");
        doReturn(libroMock).when(libroService).buscarLibroByTitulo(titulo);

        String resultado = consolaSpy.obtenerTituloMaterial();

        //Asset
        assertNotNull(resultado);
        assertEquals(titulo, resultado);
        //Verificamos que no se ejecutan
        verify(consolaSpy,never()).solicitarRevistaByTitulo();
        verify(revistaService,never()).buscarRevistaByTitulo(anyString());
        verify(commonUtil,never()).mostrarMensaje(contains("La revista "));
    }

    @Test
    public void obtenerTituloMaterial_cuandoMaterialEsLibroYDatosNoValidos_deberiaMostrarMensajeYNoDevolverNada (){
        String titulo = "TITULO LIBRO";
        doReturn(true).when(consolaSpy).solicitarDevolucionMaterial();

        //Solicitar el titulo del libro
        doReturn(titulo).when(consolaSpy).solicitarLibroByTitulo();

        //Libro no encontrado
        doReturn(null).when(libroService).buscarLibroByTitulo(titulo);

        String resultado = consolaSpy.obtenerTituloMaterial();

        //Asset
        verify(commonUtil).mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
        assertNull(resultado);
        assertNotEquals(titulo, resultado);
        //Verificamos que no se ejecutan
        verify(consolaSpy,never()).solicitarRevistaByTitulo();
        verify(revistaService,never()).buscarRevistaByTitulo(anyString());
        verify(commonUtil,never()).mostrarMensaje(contains("La revista "));

    }

    @Test
    public void obtenerTituloMaterial_cuandoMaterialEsRevistaYDatosValidos_deberiaDevolverElTitulo (){
        String titulo = "TITULO REVISTA";
        doReturn(false).when(consolaSpy).solicitarDevolucionMaterial();

        //Solicitar el titulo de la revista
        doReturn(titulo).when(consolaSpy).solicitarRevistaByTitulo();

        Revista revistaMock = new Revista();
        revistaMock.setTitulo("TITULO REVISTA");
        doReturn(revistaMock).when(revistaService).buscarRevistaByTitulo(titulo);

        String resultado = consolaSpy.obtenerTituloMaterial();

        //Asset
        assertNotNull(resultado);
        assertEquals(titulo, resultado);
        //Verificamos que no se ejecutan
        verify(consolaSpy,never()).solicitarLibroByTitulo();
        verify(libroService,never()).buscarLibroByTitulo(anyString());
        verify(commonUtil,never()).mostrarMensaje(contains("Libro "));

    }

    @Test
    public void obtenerTituloMaterial_cuandoMaterialEsRevistaYDatosVNoValidos_deberiaMostrarMensajeYNoDevolverNada (){
        String titulo = "TITULO REVISTA";
        doReturn(false).when(consolaSpy).solicitarDevolucionMaterial();

        //Solicitar el titulo de la revista
        doReturn(titulo).when(consolaSpy).solicitarRevistaByTitulo();

        doReturn(null).when(revistaService).buscarRevistaByTitulo(titulo);

        String resultado = consolaSpy.obtenerTituloMaterial();

        //Asset
        verify(commonUtil).mostrarMensaje("La revista \"" + titulo + "\" inexistente en la biblioteca");
        assertNull(resultado);
        assertNotEquals(titulo, resultado);
        //Verificamos que no se ejecutan
        verify(consolaSpy,never()).solicitarLibroByTitulo();
        verify(libroService,never()).buscarLibroByTitulo(anyString());
        verify(commonUtil,never()).mostrarMensaje(contains("Libro "));

    }

    @Test
    public void validarMaterialPrestado_cuandoDatosValidos_deberiaDevolverTrue () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(3);

        String titulo = "TITULO PRUEBA";

        //Act
        boolean resultado = consolaSpy.validarMaterialPrestado(materialMock, titulo );

        //Assert
        //Debe devolver un true
        assertTrue(resultado);
        //Validamos que no se ejecutan los procesos
        verify(commonUtil,never()).mostrarMensaje("El material \"" + titulo + "\" no existe en la biblioteca");
        verify(commonUtil,never()).mostrarMensaje("El material \"" + titulo + "\" no está prestado");

    }

    @Test
    public void validarMaterialPrestado_cuandoMaterialNoExiste_deberiaDevolverFalse () {
        MaterialBiblioteca materialMock = null;

        String titulo = "TITULO PRUEBA";

        //Act
        boolean resultado = consolaSpy.validarMaterialPrestado(materialMock, titulo );

        //Assert
        //Debe devolver un false
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("El material \"" + titulo + "\" no existe en la biblioteca");
        //Validamos que no se ejecutan los procesos
        verify(commonUtil,never()).mostrarMensaje("El material \"" + titulo + "\" no está prestado");

    }

    @Test
    public void validarMaterialPrestado_cuandoTotalesEsNull_deberiaDevolverFalse () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getTotales()).thenReturn(null);

        String titulo = "TITULO PRUEBA";

        //Act
        boolean resultado = consolaSpy.validarMaterialPrestado(materialMock, titulo );

        //Assert
        //Debe devolver un false
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("El material \"" + titulo + "\" no está prestado");
        //Validamos que no se ejecutan los procesos
        verify(commonUtil,never()).mostrarMensaje("El material \"" + titulo + "\" no existe en la biblioteca");

    }

    @Test
    public void validarMaterialPrestado_cuandoDisponiblesEsNull_deberiaDevolverFalse () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(null);

        String titulo = "TITULO PRUEBA";

        //Act
        boolean resultado = consolaSpy.validarMaterialPrestado(materialMock, titulo );

        //Assert
        //Debe devolver un false
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("El material \"" + titulo + "\" no está prestado");
        //Validamos que no se ejecutan los procesos
        verify(commonUtil,never()).mostrarMensaje("El material \"" + titulo + "\" no existe en la biblioteca");

    }

    @Test
    public void validarMaterialPrestado_cuandoTotalesEsIgualQueDisponibles_deberiaDevolverFalse () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);

        String titulo = "TITULO PRUEBA";

        //Act
        boolean resultado = consolaSpy.validarMaterialPrestado(materialMock, titulo );

        //Assert
        //Debe devolver un false
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("El material \"" + titulo + "\" no está prestado");
        //Validamos que no se ejecutan los procesos
        verify(commonUtil,never()).mostrarMensaje("El material \"" + titulo + "\" no existe en la biblioteca");

    }

    @Test
    public void mostrarMensajeTienePrestado () {
        String nombre = "FRANCISCO GARCIA";
        String titulo = "EL CRIMEN PERFECTO";

        //Act
        consolaSpy.mostrarMensajeNoTienePrestado(nombre, titulo);

        //Assert
        verify(commonUtil).mostrarMensaje("El usuario \"" + nombre + "\" no tiene prestado el material " + titulo);

    }

    @Test
    public void solicitarDatosUsuario_cuandoDatosValidos_deberiaDevolverUsuario (){
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");

        String nombre = "FRANCISCO GARCIA";
        when(commonUtil.leerEntrada()).thenReturn(nombre);
        when(usuarioService.consultarUsuarioByNombre(nombre)).thenReturn(usuarioMock);

        //Act
        Usuario resultado = consolaSpy.solicitarDatosUsuario();

        //Assert
        assertEquals(usuarioMock, resultado);
        verify(commonUtil).mostrarMensaje("Introduce el nombre del usuario");
        //Verificamos que no se ejecuta
        verify(commonUtil,never()).mostrarMensaje("El usuario no existe");

    }

    @Test
    public void solicitarDatosUsuario_cuandoUsuarioNoExiste_deberiaMostrarMensajeYDevolverNull (){
        String nombre = "FRANCISCO GARCIA";
        when(commonUtil.leerEntrada()).thenReturn(nombre);
        when(usuarioService.consultarUsuarioByNombre(nombre)).thenReturn(null);

        //Act
        Usuario resultado = consolaSpy.solicitarDatosUsuario();

        //Assert
        //Verificamos que se devuelve un null
        assertNull(resultado);
        verify(commonUtil).mostrarMensaje("Introduce el nombre del usuario");
        verify(commonUtil).mostrarMensaje("El usuario no existe");

    }

    @Test
    public void comprobarExistencia_cuandoDatosValidos_deberiaDevolverTrue () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);
        when(materialMock.getTotales()).thenReturn(5);
        when(materialMock.getDisponibles()).thenReturn(5);

        //Act
        boolean resultado = consolaSpy.comprobarExistencia(materialMock);

        //Assert
        assertTrue(resultado);
        //Verificamos que no se han ejecutado
        verify(commonUtil,never()).mostrarMensaje(contains("no existe en la biblioteca"));
        verify(commonUtil,never()).mostrarMensaje(contains("No hay ejemplares disponibles del material"));
    }

    @Test
    public void comprobarExistencia_cuandoMaterialNoExiste_deberiaMostrarMensajeYDevolverFalse () {
        //Act
        boolean resultado = consolaSpy.comprobarExistencia(null);

        //Assert
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("El material no existe en la biblioteca");
        //Verificamos que no se han ejecutado
        verify(commonUtil,never()).mostrarMensaje(contains("No hay ejemplares disponibles del material"));
    }

    @Test
    public void comprobarExistencia_cuandoDisponiblesEsNull_deberiaMostrarMensajeYDevolverFalse () {
        String titulo = "CRIMEN PERFECTO";
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getDisponibles()).thenReturn(null);
        when(materialMock.getTitulo()).thenReturn("CRIMEN PERFECTO");

        //Act
        boolean resultado = consolaSpy.comprobarExistencia(materialMock);

        //Assert
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("No hay ejemplares disponibles del material \"" + titulo + "\"");
        //Verificamos que no se han ejecutado
        verify(commonUtil,never()).mostrarMensaje("El material no existe en la biblioteca");
    }

    @Test
    public void comprobarExistencia_cuandoDisponiblesEsCero_deberiaMostrarMensajeYDevolverFalse () {
        String titulo = "CRIMEN PERFECTO";
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getDisponibles()).thenReturn(0);
        when(materialMock.getTitulo()).thenReturn("CRIMEN PERFECTO");

        //Act
        boolean resultado = consolaSpy.comprobarExistencia(materialMock);

        //Assert
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("No hay ejemplares disponibles del material \"" + titulo + "\"");
        //Verificamos que no se han ejecutado
        verify(commonUtil,never()).mostrarMensaje("El material no existe en la biblioteca");
    }

    @Test
    public void obtenerPrestamoList_cuandoDatosValidos_deberiaDevolverListaPrestamos () {
        int usuarioIdMock = 1;

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("FRANCISCOGAR@GMAIL.COM");

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setId(1);
        prestamoMock.setUsuario(usuarioMock);
        List<Prestamo> prestamoListMock = List.of(prestamoMock);

        when(prestamoService.buscarPrestamoByUsuarioIdAndActivo(usuarioIdMock)).thenReturn(prestamoListMock);

        //Act
        List<Prestamo> resultadoList = consolaSpy.obtenerPrestamoList(usuarioIdMock);

        //Assert
        assertEquals(prestamoListMock, resultadoList);
        //Verificamos que no se ejecuta
        verify(commonUtil,never()).mostrarMensaje(contains("no tiene préstamos"));

    }

    @Test
    public void obtenerPrestamoList_cuandoElUsuarioNoTienePrestamos_deberiaMostrarMensajeYDevolverNull () {
        int usuarioIdMock = 1;

        when(prestamoService.buscarPrestamoByUsuarioIdAndActivo(usuarioIdMock)).thenReturn(List.of());

        //Act
        List<Prestamo> resultadoList = consolaSpy.obtenerPrestamoList(usuarioIdMock);

        //Assert
        assertNull(resultadoList);
        verify(commonUtil).mostrarMensaje("El usuario \"" + usuarioIdMock + "\" no tiene préstamos");

    }

    @Test
    public void mostrarMenuPrestamo(){
        consolaSpy.mostrarMenuPrestamo();

        String actual = outContent.toString(StandardCharsets.UTF_8);
        actual = actual.replace("\r\n", "\n"); // Normaliza saltos de línea

        String expectedText ="""
                \n--- Menú Préstamo ---
                A.- Alta préstamo
                B.- Devolución préstamo
                C.- Consulta préstamo por Usuario
                D.- Consulta préstamo por Titulo
                S.- Salir al menu principal
                Elige una opción:""";


        // Como usas println, se añade un salto extra al final
        String expected = expectedText + "\n";

        //Assert
        assertEquals(expected, actual);

    }

    @Test
    public void crearPrestamo (){
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTitulo()).thenReturn("SUEÑO");

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setUsuario(usuarioMock);
        prestamoMock.setFechaPrestamo(LocalDate.now());
        prestamoMock.setMateriales(List.of(materialMock));
        prestamoMock.setActivo(true);

        //Act
        Prestamo resultado = consolaSpy.crearPrestamo(usuarioMock, materialMock);

        //Assert
        assertEquals(prestamoMock, resultado);

    }

    @Test
    public void registrarPrestamo_cuandoDatosValidos_deberiaNoDevolverNada () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTitulo()).thenReturn("SUEÑO");
        when(materialMock.getDisponibles()).thenReturn(2);

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setUsuario(usuarioMock);
        prestamoMock.setFechaPrestamo(LocalDate.now());
        prestamoMock.setMateriales(List.of(materialMock));
        prestamoMock.setActivo(true);

        prestamoService.insertarPrestamo(prestamoMock);
        materialBibliotecaService.actualizarDisponible(materialMock,1);

        //Act
        consolaSpy.registrarPrestamo(prestamoMock, materialMock);

        //Assert
        verify(commonUtil).mostrarMensaje("Préstamo registrado");
        verify(commonUtil).mostrarMensaje("Disponibilidad actualizada");
        //Verificamos que no se ejecutan
        verify(commonUtil,never()).mostrarMensajeError(anyString(), any());

    }

    @Test
    public void registrarPrestamo_cuandoExcepcionEnRegistrarPrestamos_deberiaMostrarMensaje () {
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setActivo(true);

        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTitulo()).thenReturn("SUEÑO");
        when(materialMock.getDisponibles()).thenReturn(2);

        Exception exception = new RuntimeException();
        doThrow(exception).when(prestamoService).insertarPrestamo(prestamoMock);

        //Act
        consolaSpy.registrarPrestamo(prestamoMock, materialMock);

        //Assert
        verify(commonUtil).mostrarMensajeError("registrar Préstamo", exception);
        //Verificamos que no se ejecutan
        verify(materialBibliotecaService,never()).actualizarDisponible(any(), anyInt());
        verify(commonUtil,never()).mostrarMensaje("Préstamo registrado");
        verify(commonUtil,never()).mostrarMensaje("Disponibilidad actualizada");
        verify(commonUtil,never()).mostrarMensajeError("actualizar disponibilidad", exception);

    }

    @Test
    public void registrarPrestamo_cuandoExcepcionEnActualizarDisponibilidad_deberiaMostrarMensaje () {
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getTitulo()).thenReturn("SUEÑO");
        when(materialMock.getDisponibles()).thenReturn(2);

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setUsuario(usuarioMock);
        prestamoMock.setFechaPrestamo(LocalDate.now());
        prestamoMock.setMateriales(List.of(materialMock));
        prestamoMock.setActivo(true);

        prestamoService.insertarPrestamo(prestamoMock);


        Exception exception = new RuntimeException();
        doThrow(exception).when(materialBibliotecaService).actualizarDisponible(materialMock, 1);

        //Act
        consolaSpy.registrarPrestamo(prestamoMock, materialMock);

        //Assert
        verify(commonUtil).mostrarMensaje("Préstamo registrado");
        verify(commonUtil).mostrarMensajeError("actualizar disponibilidad", exception);
        //Verificamos que no se ejecutan
        verify(commonUtil,never()).mostrarMensajeError("registrar Préstamo", exception);
        verify(commonUtil,never()).mostrarMensaje("Disponibilidad actualizada");

    }

    @Test
    public void solicitarTituloPrestamo () {
        String titulo = "TITULO PRUEBA";
        MaterialBiblioteca materialMock = mock(MaterialBiblioteca.class);
        when(materialMock.getId()).thenReturn(1);

        //Simula la entrada del usuario
        when(commonUtil.leerEntrada()).thenReturn(titulo);

        //Simula el resultado del servicio
        when(materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo)).thenReturn(materialMock);

        //Act
        MaterialBiblioteca resultado = consolaSpy.solicitarTituloPrestamo();

        //Assert
        assertEquals(materialMock, resultado);
        verify(commonUtil).mostrarMensaje("Titulo del libro o revista");
    }

    @Test
    public void solicitarDevolucionMaterial_cuandoEsLibro_deberiaDevolverTrue () {
        when(commonUtil.leerEntrada()).thenReturn("LIBRO");

        //Act
        boolean resultado = consolaSpy.solicitarDevolucionMaterial();

        //Assert
        assertTrue(resultado);
        verify(commonUtil).mostrarMensaje("¿Qué se quiere devolver (Libro / Revista): ?");

    }

    @Test
    public void solicitarDevolucionMaterial_cuandoEsRevista_deberiaDevolverFalse () {
        when(commonUtil.leerEntrada()).thenReturn("REVISTA");

        //Act
        boolean resultado = consolaSpy.solicitarDevolucionMaterial();

        //Assert
        assertFalse(resultado);
        verify(commonUtil).mostrarMensaje("¿Qué se quiere devolver (Libro / Revista): ?");

    }

    @Test
    public void solicitarLibroByTitulo () {
        String titulo = "TITULO LIBRO";
        when(commonUtil.leerEntrada()).thenReturn(titulo);

        //Act
        String resultado = consolaSpy.solicitarLibroByTitulo();

        //Assert
        assertEquals(titulo, resultado);
        verify(commonUtil).mostrarMensaje("Titulo del libro");
    }

    @Test
    public void solicitarRevistaByTitulo () {
        String titulo = "TITULO REVISTA";
        when(commonUtil.leerEntrada()).thenReturn(titulo);

        //Act
        String resultado = consolaSpy.solicitarRevistaByTitulo();

        //Assert
        assertEquals(titulo, resultado);
        verify(commonUtil).mostrarMensaje("Titulo de la revista");
    }
}
