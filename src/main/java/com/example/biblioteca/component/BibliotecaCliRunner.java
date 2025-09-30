package com.example.biblioteca.component;

import com.example.biblioteca.service.CommonService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@ConditionalOnProperty(name = "app.console.enabled", havingValue = "true", matchIfMissing = false)
public class BibliotecaCliRunner implements CommandLineRunner {

    private final Consola consola;
    private final ConsolaUsuario consolaUsuario;
    private final ConsolaAutor consolaAutor;
    private final ConsolaLibro consolaLibro;
    private final ConsolaRevista consolaRevista;
    private final ConsolaPrestamo consolaPrestamo;
    private final CommonService commonService;

    public BibliotecaCliRunner(Consola consola,
                               ConsolaUsuario consolaUsuario,
                               ConsolaAutor consolaAutor,
                               ConsolaLibro consolaLibro,
                               ConsolaRevista consolaRevista,
                               ConsolaPrestamo consolaPrestamo,
                               CommonService commonService) {
        this.consola = consola;
        this.consolaUsuario = consolaUsuario;
        this.consolaAutor = consolaAutor;
        this.consolaLibro = consolaLibro;
        this.consolaRevista = consolaRevista;
        this.consolaPrestamo = consolaPrestamo;
        this.commonService = commonService;
    }

    @Override
    public void run(String... args) {
        final String OPCION_USUARIO  = "1";
        final String OPCION_LIBRO    = "2";
        final String OPCION_REVISTA  = "3";
        final String OPCION_PRESTAMO = "4";
        final String OPCION_AUTOR    = "5";
        final String OPCION_SALIR    = "S";

        final Scanner scanner = new Scanner(System.in);
        String opcion;

        do {
            consola.mostrarMenuPrincipal();
            opcion = scanner.next().trim().toUpperCase();

            switch (opcion) {
                case OPCION_USUARIO  -> consolaUsuario.menuUsuario();
                case OPCION_LIBRO    -> consolaLibro.menuLibro();
                case OPCION_REVISTA  -> consolaRevista.menuRevista();
                case OPCION_PRESTAMO -> consolaPrestamo.menuPrestamo();
                case OPCION_AUTOR    -> consolaAutor.menuAutor();
                case OPCION_SALIR    -> System.out.println("---- !!! Hasta pronto !!! ----");
                default              -> commonService.mostrarError();
            }
        } while (!OPCION_SALIR.equals(opcion));
    }
}
