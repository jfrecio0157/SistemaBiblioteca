package com.example.biblioteca.component;

import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.model.Revista;
import com.example.biblioteca.service.CommonService;
import com.example.biblioteca.service.MaterialBibliotecaService;
import com.example.biblioteca.service.RevistaService;
import com.example.biblioteca.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Scanner;

import static com.example.biblioteca.util.ConstantesMenu.*;


@Component
@Transactional
public class ConsolaRevista {

    private final Scanner scanner = new Scanner(System.in);
    private final RevistaService revistaService;
    private final MaterialBibliotecaService materialBibliotecaService;
    private final CommonService commonService;
    private final CommonUtil commonUtil;

    @Autowired
    public ConsolaRevista(RevistaService revistaService,
                          MaterialBibliotecaService materialBibliotecaService,
                          CommonService commonService,
                          CommonUtil commonUtil) {
        this.revistaService = revistaService;
        this.materialBibliotecaService = materialBibliotecaService;
        this.commonService = commonService;
        this.commonUtil = commonUtil;
    }


    public void menuRevista () {
        String opcion;

        do {
            mostrarMenuRevista();
            opcion = scanner.nextLine().trim().toUpperCase();

            switch (opcion) {
                case OPCION_ALTA -> procesarAltaRevista(); //Alta
                case OPCION_BAJA -> procesarBajaRevista(); // Baja
                case OPCION_CONSULTA_TITULO -> procesarConsultaByTitulo(); // Consulta por título
                case OPCION_CONSULTA_TOTAL -> procesarConsultaTotal(); // Consulta de todas las revistas existentes
                case OPCION_SALIR -> commonService.volverMenuPrincipal(); // Salir
                default -> commonService.mostrarError(); //Otra opcion
            }
        }while (!OPCION_SALIR.equals(opcion));
    }

    /* Alta:
     * Se solicitan los datos de la revista
     * Se da de alta en la bbdd
     */
    void procesarAltaRevista() {
        try {
            Revista revistaCompleta = solicitarDatosRevista();
            revistaService.insertarRevista(revistaCompleta);
            commonUtil.mostrarMensaje("Revista insertada correctamente.");

        } catch (Exception e) {
            commonUtil.mostrarMensajeError("alta de la revista: ", e);
        }
    }

    /* Baja:
     * Se solicita el nombre del titulo de la revista
     * y se borra fisicamente la revista de la bbdd
     */
    void procesarBajaRevista() {
        String titulo = solicitarRevistaByTitulo();
        Revista revistaEncontrada = revistaService.buscarRevistaByTitulo(titulo);

        if (revistaEncontrada == null) {
            commonUtil.mostrarMensaje("Revista \"" + titulo + "\" inexistente en la biblioteca");
            return;
        }

        //Si la revista esta prestada, no se puede eliminar
        MaterialBiblioteca materialBiblioteca = materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo);
        if (materialBiblioteca.getDisponibles() < materialBiblioteca.getTotales() ){
            commonUtil.mostrarMensaje("La revista \"" + titulo + "\" no se puede eliminar. Esta prestada.");
            return;
        }

        //Se pregunta se realmente se quiere eliminar
        if (!(commonService.preguntarSiBorrar())) {
            //El título se muestra entre comillas dobles
            commonUtil.mostrarMensaje("La revista \"" + titulo + "\" no ha sido eliminada.");
            return;
        }

        //Eliminacion de la revista
        try {
            revistaService.eliminarRevistaById(revistaEncontrada.getId());
            commonUtil.mostrarMensaje("La revista ha sido eliminada");
        } catch (Exception e) {
            commonUtil.mostrarMensajeError("baja revista: ", e);
        }
    }

    /* Consulta por título:
     * Se solicita el título de la revista
     * y se devuelven los datos de la revista por consola
     */
    void procesarConsultaByTitulo() {
        String titulo = solicitarRevistaByTitulo();
        Revista revistaEncontrada = revistaService.buscarRevistaByTitulo(titulo);

        if (revistaEncontrada == null) {
            commonUtil.mostrarMensaje("Revista \"" + titulo + "\" inexistente en la biblioteca");
            return;
        }

        mostrarDatos(revistaEncontrada);
    }

    /* Consulta de todas las revistas existentes
     * Se devuelven los datos de todas las revistas de la biblioteca
     */
    void procesarConsultaTotal() {
        List<Revista> revistaList = revistaService.listarRevistas();

        if (revistaList.isEmpty()) {
            commonUtil.mostrarMensaje("No hay revistas en la biblioteca");
            return;
        }

        revistaList.forEach(this::mostrarDatos);
    }

    private void mostrarMenuRevista() {
        System.out.println("""
                \n--- Menú Revista ---
                A.- Alta revista
                B.- Baja revista
                C.- Consulta revista por Título
                D.- Consulta todas las revistas
                S.- Salir al menú principal
                Elige una opción: """);
    }

    private void mostrarDatos(Revista revista) {
        System.out.printf("""
                \nDatos de la revista
                id: %d
                Título: %s
                Periodicidad: %s
                Número edición: %d
                Ejemplares totales: %d
                Ejemplares disponibles: %d
                """,
                revista.getId(),
                revista.getTitulo(),
                revista.getPeriodicidad(),
                revista.getNumeroEdicion(),
                revista.getTotales(),
                revista.getDisponibles());
    }

    Revista solicitarDatosRevista(){
        Revista revista = new Revista();

        //Titulo de la revista
        commonUtil.mostrarMensaje("Titulo de la revista" );
        revista.setTitulo(commonUtil.leerEntrada());

        //Periodicidad de la revista
        commonUtil.mostrarMensaje("Periodicidad de la revista" );
        revista.setPeriodicidad(commonUtil.leerEntrada());

        //Número edición
        commonUtil.mostrarMensaje("Número edición:");
        revista.setNumeroEdicion(commonUtil.leerEntero());


        // Numero de ejemplares
        commonUtil.mostrarMensaje("Número de ejemplares en la biblioteca");
        int ejemplares = commonUtil.leerEntero();
        revista.setTotales(ejemplares);
        //En el alta el numero de ejemplares disponibles es el mismo que el numero de ejemplares totales
        revista.setDisponibles(ejemplares);

        return revista;
    }


    String solicitarRevistaByTitulo() {
        commonUtil.mostrarMensaje("Titulo de la revista" );
        return commonUtil.leerEntrada();
    }

}
