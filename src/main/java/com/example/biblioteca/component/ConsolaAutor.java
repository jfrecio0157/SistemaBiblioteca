package com.example.biblioteca.component;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.service.AutorService;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

import static com.example.biblioteca.util.ConstantesMenu.*;

@Component
public class ConsolaAutor {
    
    private final Scanner scanner = new Scanner(System.in);
    private final AutorService autorService;
    private final CommonService commonService;
    private final CommonUtil commonUtil;

    @Autowired
    public ConsolaAutor(AutorService autorService,
                        CommonService commonService,
                        CommonUtil commonUtil){
        this.autorService = autorService;
        this.commonService = commonService;
        this.commonUtil = commonUtil;
    }

    public void menuAutor () {
        String opcion;

        do {
            mostrarMenuAutor();
            opcion = commonUtil.leerEntrada();

            switch (opcion) {
                case OPCION_ALTA -> procesarAltaAutor(); //Alta
                case OPCION_BAJA -> procesarBajaAutor(); //Baja
                case OPCION_CONSULTA -> procesarConsultaAutor(); //Consulta
                case OPCION_SALIR -> commonService.volverMenuPrincipal(); //Volver al menu principal
                default -> commonService.mostrarError(); //Otra opcion
            }
        }while (!OPCION_SALIR.equals(opcion));
    }

    public void procesarAltaAutor(){
        Autor autor=solicitarDatosAutor();

        try{
            autorService.insertarAutor(autor.getNombre());
            commonUtil.mostrarMensaje("Autor dado de alta correctamente");
        }catch (Exception e){
            commonUtil.mostrarMensajeError("alta Autor", e);
        }

    }

    //Baja de un autor
    //-- Un autor se puede eliminar si no tiene libros en la biblioteca
    //Pedir el nombre del autor y obtener su id de la tabla autor
    //Con el id del autor ir a autor_libro y comprobar si existe.
    //Si existe, entonces no se puede dar de baja el autor
    //Si no existe, se puede dar de baja el autor
    public void procesarBajaAutor() {
        Autor autor = obtenerAutor();
        if (autor == null) return;

        try{
            autorService.eliminarAutorById(autor.getId());
            commonUtil.mostrarMensaje("Autor dado de baja correctamente");
        }catch (Exception e){
            commonUtil.mostrarMensajeError("baja Autor", e);
        }
    }

    //Consulta por autor
    public void procesarConsultaAutor() {
        Autor autor = obtenerAutor();
        if (autor == null) return;

        mostrarDatos(autor);
        mostrarLibros(autor);
    }


    void mostrarMenuAutor() {
        System.out.println("""
                \n-- Menu autor ---
                A.- Alta autor
                B.- Baja autor
                C.- Consulta autor
                S.- Salir al menú principal
                Elige una opción:""");
    }

    void mostrarDatos(Autor autor){
        System.out.printf("""
                        \nDatos del Autor
                        ID: %d
                        Nombre: %s
                        """,
                        autor.getId(),
                        autor.getNombre()
                );
    }

    Autor obtenerAutor() {
        Autor autor = autorService.buscarAutorByNombre(solicitarAutorByNombre());

        if (autor == null) {
            commonUtil.mostrarMensaje("Autor inexistente en la biblioteca");
            return null;
        }

        if (!(autor.getLibro().isEmpty())) {
            commonUtil.mostrarMensaje("El autor \"" + autor.getNombre() + "\" no se puede dar de baja. Tiene libros en la biblioteca");
            return null;
        }
        return autor;
    }

    void  mostrarLibros(Autor autor){
        List<Libro> librosL = autor.getLibro();
        if (librosL == null || librosL.isEmpty()){
            commonUtil.mostrarMensaje("Este autor no tiene libros registrados");
        }else {
            librosL.forEach(libro -> commonUtil.mostrarMensaje("Titulo libro: " + libro.getTitulo()));
        }
    }


    Autor solicitarDatosAutor (){
        Autor autor = new Autor();

        //Nombre del autor
        autor.setNombre(solicitarAutorByNombre());
        return autor;
    }

    String solicitarAutorByNombre () {
        commonUtil.mostrarMensaje("Nombre del autor" );
        return commonUtil.leerEntrada();
    }

}

