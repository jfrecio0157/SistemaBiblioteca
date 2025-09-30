package com.example.biblioteca.component;

import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.service.PrestamoService;
import com.example.biblioteca.service.UsuarioService;
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

public class ConsolaUsuarioTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @Mock
    private PrestamoService prestamoService;

    @Mock
    private CommonService commonService;

    @Mock
    private CommonUtil commonUtil;

    @Mock
    private UsuarioService usuarioService;

    @Spy @InjectMocks
    private ConsolaUsuario consolaSpy;

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
    public void procesarAltaUsuario_cuandoDatosValidos_deberiaInsertarUsuarioYMostrarMensaje() {
        // Simular que solicitarDatosUsuario devuelve un Usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        //Act
        consolaSpy.procesarAltaUsuario();

        //Assert
        verify(usuarioService).insertarUsuario(usuarioMock);
        verify(commonUtil).mostrarMensaje("Usuario dado de alta correctamente");
    }

    @Test
    public void procesarAltaUsuario_cuandoDatosNoValidos_deberiaMostrarMensajeError() {
        // Simular que solicitarDatosUsuario devuelve un Usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");
        doReturn(usuarioMock).when(consolaSpy).solicitarDatosUsuario();

        Exception exception = new RuntimeException();
        doThrow(exception).when(usuarioService).insertarUsuario(usuarioMock);

        //Act
        consolaSpy.procesarAltaUsuario();

        //Assert
        verify(commonUtil).mostrarMensajeError("alta Usuario", exception);
    }

    @Test
    public void procesarBajaUsuario_cuandoDatosValidos_deberiaEliminarUsuarioYMostrarMensaje() {
        // Simular que solicitarDatosUsuario devuelve un Usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");
        usuarioMock.setId(1);
        doReturn(usuarioMock).when(consolaSpy).obtenerUsuario();

        //PreguntarSiBorrar devuelve true
        doReturn(true).when(commonService).preguntarSiBorrar();

        //Sin prestamos activos.
        doReturn(null).when(prestamoService).buscarPrestamoByUsuarioIdAndActivo(1);

        //Act
        consolaSpy.procesarBajaUsuario();

        //Assert
        verify(usuarioService).eliminarUsuarioById(1);
        verify(commonUtil).mostrarMensaje("El usuario \"" + usuarioMock.getNombre() + "\" ha sido eliminado.");
    }

    @Test
    public void procesarBajaUsuario_cuandoNoSeObtieneUsuario_deberiaMostrarMensaje() {
        // Simular que solicitarDatosUsuario devuelve un Usuario
        doReturn(null).when(consolaSpy).obtenerUsuario();

        //Act
        consolaSpy.procesarBajaUsuario();

        //Assert
        //Verificamos que no se llama al resto de procesos posteriores
        verify(commonService,never()).preguntarSiBorrar();
        verify(prestamoService,never()).buscarPrestamoByMaterialId(anyInt());
        verify(usuarioService,never()).eliminarUsuarioById(anyInt());

    }

    @Test
    public void procesarBajaUsuario_cuandoNoSeConfirmaBorrar_deberiaMostrarMensaje(){
        // Simular que solicitarDatosUsuario devuelve un Usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");
        usuarioMock.setId(1);
        doReturn(usuarioMock).when(consolaSpy).obtenerUsuario();

        //PreguntarSiBorrar devuelve false
        doReturn(false).when(commonService).preguntarSiBorrar();

        //Act
        consolaSpy.procesarBajaUsuario();

        //Assert
        verify(commonUtil,times(1)).mostrarMensaje("El usuario \"" + usuarioMock.getNombre() + "\" no ha sido eliminado.");
        //Verificamos que el resto de procesos posteriores no se ejecutan
        verify(prestamoService,never()).buscarPrestamoByUsuarioIdAndActivo(anyInt());
        verify(usuarioService,never()).eliminarUsuarioById(anyInt());

    }

    @Test
    public void procesarBajaUsuario_cuandoElUsuarioTieneMaterialPrestado_deberiaMostrarMensaje(){

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");
        usuarioMock.setId(1);
        doReturn(usuarioMock).when(consolaSpy).obtenerUsuario();

        //PreguntarSiBorrar devuelve true
        doReturn(true).when(commonService).preguntarSiBorrar();

        //Con prestamos activos.
        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setUsuario(usuarioMock);
        prestamoMock.setId(1);
        prestamoMock.setActivo(true);

        List<Prestamo> prestamoListMock = List.of(prestamoMock);
        doReturn(prestamoListMock).when(prestamoService).buscarPrestamoByUsuarioIdAndActivo(1);

        //Act
        consolaSpy.procesarBajaUsuario();

        //Assert
        verify(commonUtil,times(1)).mostrarMensaje("El usuario no se puede dar de baja. Tiene material prestado.");
        //Verificamos que el resto de procesos posteriores no se ejecutan
        verify(usuarioService,never()).eliminarUsuarioById(anyInt());
    }

    @Test
    public void procesarBajaUsuario_cuandoHayExcepcion_deberiaMostrarMensajeError(){
        // Simular que solicitarDatosUsuario devuelve un Usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");
        usuarioMock.setId(1);
        doReturn(usuarioMock).when(consolaSpy).obtenerUsuario();

        //PreguntarSiBorrar devuelve true
        doReturn(true).when(commonService).preguntarSiBorrar();

        //Sin prestamos activos.
        doReturn(null).when(prestamoService).buscarPrestamoByUsuarioIdAndActivo(1);

        Exception exception = new RuntimeException();
        doThrow(exception).when(usuarioService).eliminarUsuarioById(1);

        //Act
        consolaSpy.procesarBajaUsuario();

        //Assert
        verify(commonUtil).mostrarMensajeError("baja usuario", exception);
    }

    @Test
    public void procesarConsultaUsuario_cuandoDatosValidos_deberiaMostrarDatosUsuario(){
        // Simular que solicitarDatosUsuario devuelve un Usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");
        usuarioMock.setEmail("franciscogar@gmail.com");

        doReturn(usuarioMock).when(consolaSpy).obtenerUsuario();

        //Act
        consolaSpy.procesarConsultaUsuario();

        //Assert
        verify(consolaSpy).mostrarDatosUsuario(usuarioMock);
    }

    @Test
    public void procesarConsultaUsuario_cuandoUsuarioNoExiste_deberiaNoHacerNada (){
        // Simular que solicitarDatosUsuario devuelve un Usuario
        doReturn(null).when(consolaSpy).obtenerUsuario();

        //Act
        consolaSpy.procesarConsultaUsuario();

        //Assert
        //Verificamos que no se ejecutan los procesos posteriores
        verify(consolaSpy,never()).mostrarDatosUsuario(new Usuario());
    }

    @Test
    public void mostrarMenuUsuario(){
        consolaSpy.mostrarMenuUsuario();

        String actual = outContent.toString(StandardCharsets.UTF_8);
        actual = actual.replace("\r\n", "\n"); // Normaliza saltos de línea

        String expectedText ="""
                \n--- Menú usuario ---
                A.- Alta usuario
                B.- Baja usuario
                C.- Consulta usuario
                S.- Salir al menu principal
                Elige una opcion:""";


        // Como usas println, se añade un salto extra al final
        String expected = expectedText + "\n";

        //Assert
        assertEquals(expected, actual);

    }
    @Test
    void mostrarDatosUsuario_imprimeElTextoFormateadoCorrectamente() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNombre("FRANCISCO GARCIA");
        usuario.setEmail("franciscogar@gmail.com");

        // Act
        consolaSpy.mostrarDatosUsuario(usuario);

        // Assert
        String actual = outContent.toString(StandardCharsets.UTF_8);
        // Normalizamos finales de línea para entorno multiplataforma
        actual = actual.replace("\r\n", "\n");

        // Construimos el esperado EXACTO:
        // - println de "\nDatos del Usuario" -> produce "\nDatos del Usuario\n"
        // - printf del bloque -> imprime:
        //   "ID: 1\nNombre: ...\nCorreo electrónico: ...\n"
        String expected = "\nDatos del Usuario\n" +
                "ID: 1\n" +
                "Nombre: FRANCISCO GARCIA\n" +
                "Correo electrónico: franciscogar@gmail.com\n";

        assertEquals(expected, actual);
    }

    @Test
    public void solicitarDatosUsuario(){
        String nombre="FRANCISCO GARCIA";
        String email="FRANCISCOGAR@GMAIL.COM";

        //En la primera llamada a leerEntrada() devuelve el nombre
        //En la segunda llamada a leerEntrada() devuelve el email.
        when(commonUtil.leerEntrada())
                .thenReturn(nombre)
                .thenReturn(email);

        //Act
        Usuario usuarioMock = consolaSpy.solicitarDatosUsuario();

        //Assert
        assertEquals(nombre,usuarioMock.getNombre());
        assertEquals(email,usuarioMock.getEmail());

        // Assert: interacciones y orden
        InOrder inOrder = inOrder(commonUtil);
        inOrder.verify(commonUtil).mostrarMensaje("Nombre del usuario");
        inOrder.verify(commonUtil).leerEntrada();
        inOrder.verify(commonUtil).mostrarMensaje("Dirección de correo electrónico: ");
        inOrder.verify(commonUtil).leerEntrada();

        verifyNoMoreInteractions(commonUtil);
    }

    @Test
    public void solicitarUsuarioByNombre (){
        String nombre = "FRANCISCO GARCIA";

        //Es lo mismo que when(commonUtil.leerEntrada()).thenReturn(nombre)
        doReturn(nombre).when(commonUtil).leerEntrada();

        //Act
        String nombreMock = consolaSpy.solicitarUsuarioByNombre();

        //Assert
        assertEquals(nombre,nombreMock);

        // Assert: interacciones y orden
        InOrder inOrder = inOrder(commonUtil);
        inOrder.verify(commonUtil).mostrarMensaje("Nombre del usuario: ");
        inOrder.verify(commonUtil).leerEntrada();
    }

    @Test
    public void obtenerUsuario_cuandoDatosValidos_deberiaDevolverUsuario (){
        String nombre = "FRANCISCO GARCIA";
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("FRANCISCO GARCIA");

        doReturn(nombre).when(consolaSpy).solicitarUsuarioByNombre();
        doReturn(usuarioMock).when(usuarioService).consultarUsuarioByNombre(nombre);

        //Act
        usuarioMock = consolaSpy.obtenerUsuario();

        //Assert
        assertNotNull(usuarioMock);
        assertEquals(nombre, usuarioMock.getNombre());

        verify(usuarioService).consultarUsuarioByNombre(nombre);
        //Verificamos que no se ejecutan procesos posteriores
        verify(commonUtil,never()).mostrarMensaje("Usuario no encontrado");
        verifyNoMoreInteractions(usuarioService, commonUtil);

    }

    @Test
    public void obtenerUsuario_cuandoUsuarioNull_deberiaMostrarMensaje (){
        String nombre = "FRANCISCO GARCIA";

        doReturn(nombre).when(consolaSpy).solicitarUsuarioByNombre();
        doReturn(null).when(usuarioService).consultarUsuarioByNombre(nombre);

        //Act
        Usuario usuarioMock = consolaSpy.obtenerUsuario();

        //Assert
        verify(commonUtil).mostrarMensaje("Usuario no encontrado");
        assertNull(usuarioMock);

    }

    @Test
    void menuUsuario_deberiaEjecutarOpcionesYSalirCorrectamente() {
        ConsolaUsuario consolaSpy = spy(new ConsolaUsuario(usuarioService, prestamoService, commonService, commonUtil));

        // El mock de commonUtil debe devolver la secuencia de opciones
        when(commonUtil.leerEntrada()).thenReturn("A","B", "C","S");

        doNothing().when(consolaSpy).procesarAltaUsuario();
        doNothing().when(consolaSpy).procesarBajaUsuario();
        doNothing().when(consolaSpy).procesarConsultaUsuario();
        doNothing().when(commonService).volverMenuPrincipal();
        //doNothing().when(commonService).mostrarError(); //No se llama a esta opcion

        // Act
        consolaSpy.menuUsuario();

        // Assert
        verify(consolaSpy, times(1)).procesarAltaUsuario();
        verify(consolaSpy, times(1)).procesarBajaUsuario();
        verify(consolaSpy, times(1)).procesarConsultaUsuario();
        verify(commonService, times(1)).volverMenuPrincipal();
        verify(commonService, never()).mostrarError();


        // (opcional) el menú se mostró 4 veces (A,B,C,S)
        verify(consolaSpy, times(4)).mostrarMenuUsuario();

        // (opcional) se leyó entrada 4 veces
        verify(commonUtil, times(4)).leerEntrada();

    }
}

