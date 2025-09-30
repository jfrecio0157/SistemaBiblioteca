package com.example.biblioteca.component;

import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.service.PrestamoService;
import com.example.biblioteca.service.UsuarioService;
import com.example.biblioteca.util.CommonUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

import static com.example.biblioteca.util.ConstantesMenu.*;

@Component
public class ConsolaUsuario {
    private final Scanner scanner = new Scanner(System.in);
    private final UsuarioService usuarioService;
    private final PrestamoService prestamoService;
    private final CommonService commonService;
    private final CommonUtil commonUtil;

    public ConsolaUsuario(UsuarioService usuarioService,
                          PrestamoService prestamoService,
                          CommonService commonService,
                          CommonUtil commonUtil
                          ) {
        this.usuarioService = usuarioService;
        this.prestamoService = prestamoService;
        this.commonService = commonService;
        this.commonUtil = commonUtil;
    }

    public void menuUsuario() {
        String opcion;

        do {
            mostrarMenuUsuario();
            opcion = commonUtil.leerEntrada();

            switch (opcion) {
                case OPCION_ALTA -> procesarAltaUsuario(); // Alta:
                case OPCION_BAJA -> procesarBajaUsuario(); // Baja
                case OPCION_CONSULTA_BY_USUARIO -> procesarConsultaUsuario(); // Consulta por usuario
                case OPCION_SALIR -> commonService.volverMenuPrincipal(); // Salir: se vuelve al menu principal
                default -> commonService.mostrarError(); // Otra opcion:
            }
        } while (!OPCION_SALIR.equals(opcion));
    }

    void procesarAltaUsuario() {
        Usuario usuario = solicitarDatosUsuario();

        try {
            usuarioService.insertarUsuario(usuario);
            commonUtil.mostrarMensaje("Usuario dado de alta correctamente");
        }catch (Exception e) {
            commonUtil.mostrarMensajeError("alta Usuario", e);
        }
    }

    void procesarBajaUsuario() {
        Usuario usuario = obtenerUsuario();
        if (usuario == null) return;

        //Se pregunta se realmente se quiere eliminar
        if (!(commonService.preguntarSiBorrar())) {
            //El nombre del usuario se muestra entre comillas dobles
            commonUtil.mostrarMensaje("El usuario \"" + usuario.getNombre() + "\" no ha sido eliminado.");
            return;
        }

        // Comprobar si el usuario tiene material prestado
        List<Prestamo> prestamoList = prestamoService.buscarPrestamoByUsuarioIdAndActivo(usuario.getId());

        if (!(prestamoList == null || prestamoList.isEmpty())){
            commonUtil.mostrarMensaje("El usuario no se puede dar de baja. Tiene material prestado.");
            return;
        }

        // Se elimina el usuario
        try{
            usuarioService.eliminarUsuarioById(usuario.getId());
            commonUtil.mostrarMensaje("El usuario \"" + usuario.getNombre() + "\" ha sido eliminado.");
        } catch (Exception e){
            commonUtil.mostrarMensajeError("baja usuario", e);
        }
    }

    public void procesarConsultaUsuario() {
        Usuario usuario = obtenerUsuario();
        if (usuario == null) return;

        mostrarDatosUsuario(usuario);
    }

    void mostrarMenuUsuario(){
        System.out.println("""
                \n--- Menú usuario ---
                A.- Alta usuario
                B.- Baja usuario
                C.- Consulta usuario
                S.- Salir al menu principal
                Elige una opcion:""");
    }

    public void mostrarDatosUsuario(Usuario usuario){
        System.out.println("\nDatos del Usuario");
        System.out.printf("""
                        ID: %d
                        Nombre: %s
                        Correo electrónico: %s
                        """,
                        usuario.getId(), usuario.getNombre(), usuario.getEmail()
                );
    }


    Usuario solicitarDatosUsuario(){
        Usuario usuario = new Usuario();

        //Nombre del autor
        commonUtil.mostrarMensaje("Nombre del usuario" );
        usuario.setNombre(commonUtil.leerEntrada());

        //Direccion de correo
        commonUtil.mostrarMensaje("Dirección de correo electrónico: ");
        usuario.setEmail(commonUtil.leerEntrada());

        return usuario;
    }

    String solicitarUsuarioByNombre () {
        commonUtil.mostrarMensaje("Nombre del usuario: " );
        return commonUtil.leerEntrada();
    }

    Usuario obtenerUsuario(){
        Usuario usuario = usuarioService.consultarUsuarioByNombre(solicitarUsuarioByNombre());

        if (usuario == null) {
            commonUtil.mostrarMensaje("Usuario no encontrado");
            return null;
        }
        return usuario;
    }

}

