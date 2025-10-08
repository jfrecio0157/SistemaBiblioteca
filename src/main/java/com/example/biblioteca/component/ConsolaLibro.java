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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.example.biblioteca.util.ConstantesMenu.*;

@Component
public class ConsolaLibro {
    private final Scanner scanner = new Scanner(System.in);
    private final LibroService libroService;
    private final MaterialBibliotecaService materialBibliotecaService;
    private final AutorService autorService;
    private final CommonService commonService;
    private final CommonUtil commonUtil;

    @Autowired
    public ConsolaLibro(LibroService libroService,
                        MaterialBibliotecaService materialBibliotecaService,
                        AutorService autorService,
                        CommonService commonService,
                        CommonUtil commonUtil) {
        this.libroService = libroService;
        this.materialBibliotecaService = materialBibliotecaService;
        this.autorService = autorService;
        this.commonService = commonService;
        this.commonUtil = commonUtil;
    }

    public void menuLibro() {
        String opcion;

        do {
            mostrarMenuLibro();
            opcion = commonUtil.leerEntrada();

            switch (opcion) {
                case OPCION_ALTA -> procesarAltaLibro(); //Alta
                case OPCION_BAJA -> procesarBajaLibro(); //Baja
                case OPCION_CONSULTA_BY_ISBN -> procesarConsultaByIsbn(); // Consulta por isbn
                case OPCION_CONSULTA_BY_TITULO -> procesarConsultaByTitulo(); // Consulta por título
                case OPCION_CONSULTA_COMPLETA -> procesarConsultaTotal(); // Consulta completa
                case OPCION_SALIR -> commonService.volverMenuPrincipal(); //Salir
                default -> commonService.mostrarError(); // Otra opcion

            }
        } while (!OPCION_SALIR.equals(opcion));
    }

    /* Alta:
     * Se solicitan los datos del libro y sus autores
     * Se da de alta en la bbdd
     */
    void procesarAltaLibro() {
        try {
            Libro libroCompleto = solicitarDatosLibro();
            libroService.insertarLibro(libroCompleto);
            commonUtil.mostrarMensaje("Libro insertado correctamente.");

        } catch (ConstraintViolationException e) {
            commonUtil.mostrarMensaje("Error al insertar el libro:");
            for (ConstraintViolation<?> v : e.getConstraintViolations()) {
                System.out.println("- " + v.getPropertyPath() + ": " + v.getMessage());
            }

        } catch (Exception e) {
            commonUtil.mostrarMensajeError("alta libro", e);
        }
    }

    /* Baja:
     * Se solicita el nombre del título del libro y se borra fisicamente el usuario de la bbdd
     */
    void procesarBajaLibro() {
        String titulo = solicitarLibroByTitulo();

        Libro libroEncontrado = buscarLibro(titulo);
        if (libroEncontrado == null) return;

        Libro libroCompleto = libroService.buscarLibroByIsbn(libroEncontrado.getIsbn());

        //Si el libro está prestado no se puede eliminar
        MaterialBiblioteca materialBiblioteca = materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo);
        if (materialBiblioteca.getDisponibles() < materialBiblioteca.getTotales()) {
            commonUtil.mostrarMensaje("El libro \"" + titulo + "\" no se puede eliminar. Esta prestado.");
            return;
        }

        // Preguntar si se quiere realmente eliminar.
        if (!commonService.preguntarSiBorrar()) {
            //El título se muestra entre comillas dobles
            commonUtil.mostrarMensaje("El libro \"" + titulo + "\" no ha sido eliminado.");
            return;
        }

        //Eliminación del libro
        try {
            libroService.eliminarLibroByIsbn(libroCompleto.getIsbn());
            commonUtil.mostrarMensaje("El libro ha sido eliminado correctamente.");
        } catch (Exception e) {
            commonUtil.mostrarMensajeError("baja libro", e);
        }
    }

    /* Consulta por isbn:
     * Se solicita el isbn del libro
     * Se devuelven los datos del libro y sus autores por consola
     */
    void procesarConsultaByIsbn() {
        Libro libro = libroService.buscarLibroByIsbn(solicitarLibroByIsbn());

        if (libro == null) {
            commonUtil.mostrarMensaje("Libro inexistente en la biblioteca");
            return;
        }

        mostrarDatos(libro);

    }

    /* Consulta por título:
     * Se solicita el título del libro
     * Con el título se obtiene su isbn, para poder después obtener los autores
     * y se devuelven los datos del libro y sus autores por consola
     */
    void procesarConsultaByTitulo() {
        String titulo = solicitarLibroByTitulo();

        Libro libroEncontrado = buscarLibro(titulo);
        if (libroEncontrado == null) return;

        Libro libroCompleto = libroService.buscarLibroByIsbn(libroEncontrado.getIsbn());
        if (libroCompleto == null) return;

        mostrarDatos(libroCompleto);
    }

    /* Consulta completa:
     * Se devuelven todos los libros de la biblioteca
     */
    void procesarConsultaTotal() {
        List<Libro> libroList = libroService.listarLibros();

        if (libroList.isEmpty()) {
            commonUtil.mostrarMensaje("No hay libros en la biblioteca");
            return;
        }

        //Añade a cada libro sus autores
        libroList.stream() //Convierte libroList en un stream. Tengo libros basicos
                .map(libro -> libroService.buscarLibroByIsbn(libro.getIsbn())) //A cada libro le añado el autor, convirtiendolo en un libro completo
                .forEach(this::mostrarDatos); // muestra los datos del libro completo.

        /*Tambien se podria hacer asi:

        Optional.of(libroList)
                .filter(libroL -> !libroList.isEmpty())
                .ifPresentOrElse(
                        libroL -> libroL.stream()
                                .map(libro -> libroService.buscarLibroByIsbn(libro.getIsbn()))
                                .forEach(this::mostrarDatos),
                        () -> commonUtil.mostrarMensaje("No hay libros en la biblioteca")
                );

         */

    }

    void mostrarMenuLibro() {
        System.out.println("""
                        \\n--- Menú libro ---
                        A.- Alta libro
                        B.- Baja libro
                        C.- Consulta libro por Isbn
                        D.- Consulta libro por Titulo
                        E.- Consulta todos los libros
                        S.- Salir al menu principal
                        Elige una opción:
                        """);
    }

    void mostrarDatos(Libro libro) {
        System.out.printf("""
                \nDatos del Libro
                ISBN: %s
                Título: %s
                Año de publicación: %d
                Autores:
                """, libro.getIsbn(), libro.getTitulo(), libro.getAñoPublicacion());

        libro.getAutores().forEach(autor ->
                System.out.printf("- %s%n", autor.getNombre()));

        System.out.printf("""
                Número de ejemplares totales: %d
                Número de ejemplares disponibles: %d
                """,
                libro.getTotales(), libro.getDisponibles());
    }

    Libro solicitarDatosLibro() {
        Libro libro = new Libro();
        String nombreAutor, masAutores;
        List<Autor> autorList = new ArrayList<>();

        //Isbn del libro
        commonUtil.mostrarMensaje("Isbn del libro");
        libro.setIsbn(commonUtil.leerEntrada());

        //Titulo del libro
        commonUtil.mostrarMensaje("Titulo del libro");
        libro.setTitulo(commonUtil.leerEntrada());

        //Año de publicacion
        commonUtil.mostrarMensaje("Año de publicación");
        libro.setAñoPublicacion(commonUtil.leerEntero());

        //Solicitar autores
        do {
            commonUtil.mostrarMensaje("Autor del libro");
            nombreAutor = commonUtil.leerEntrada();

            //Se valida que el autor existe en la tabla Autor
            Autor autor =autorService.buscarAutorByNombre(nombreAutor);
            if (autor==null) {
                commonUtil.mostrarMensaje("El autor \"" + nombreAutor + "\" no existe en la biblioteca");
            }else{
                autorList.add(autor);
            }

            commonUtil.mostrarMensaje("¿Desea añadir otro autor (S/N): ? ");
            masAutores = commonUtil.leerEntrada();

        } while ("S".equals(masAutores));

        if (autorList.isEmpty()) return null;

        libro.setAutores(autorList);

        // Número de ejemplares
        commonUtil.mostrarMensaje("Número de ejemplares en la biblioteca");
        int numeroEjemplares = commonUtil.leerEntero();
        libro.setTotales(numeroEjemplares);
        //En el alta el número de ejemplares disponibles es el mismo que el número de ejemplares totales
        libro.setDisponibles(numeroEjemplares);

        return libro;
    }

    String solicitarLibroByIsbn() {
        commonUtil.mostrarMensaje("Isbn del libro");
        return commonUtil.leerEntrada();
    }

    String solicitarLibroByTitulo() {
        commonUtil.mostrarMensaje("Titulo del libro");
        return commonUtil.leerEntrada();
    }

    Libro buscarLibro(String titulo){
        Libro libroEncontrado = libroService.buscarLibroByTitulo(titulo);

        if (libroEncontrado == null) {
            commonUtil.mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
            return null;
        }
        return libroEncontrado;
    }

}
