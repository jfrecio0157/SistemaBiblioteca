package com.example.biblioteca.component;

import com.example.biblioteca.model.*;
import com.example.biblioteca.service.*;
import com.example.biblioteca.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static com.example.biblioteca.util.ConstantesMenu.*;

@Component
public class ConsolaPrestamo {
    private final Scanner scanner = new Scanner(System.in);
    private final PrestamoService prestamoService;
    private final MaterialBibliotecaService materialBibliotecaService;
    private final LibroService libroService;
    private final RevistaService revistaService;
    private final UsuarioService usuarioService;
    private final CommonService commonService;
    private final CommonUtil commonUtil;


    @Autowired
    public ConsolaPrestamo(PrestamoService prestamoService,
                           MaterialBibliotecaService materialBibliotecaService,
                           LibroService libroService,
                           RevistaService revistaService,
                           UsuarioService usuarioService,
                           CommonService commonService,
                           CommonUtil commonUtil){
        this.prestamoService = prestamoService;
        this.materialBibliotecaService = materialBibliotecaService;
        this.libroService = libroService;
        this.revistaService = revistaService;
        this.usuarioService = usuarioService;
        this.commonService = commonService;
        this.commonUtil = commonUtil;
    }


    public void menuPrestamo () {
        String opcion;

        do {
            mostrarMenuPrestamo();
            opcion = commonUtil.leerEntrada();

            switch (opcion) {
                case OPCION_ALTA -> procesarAltaPrestamo(); //Alta
                case OPCION_DEVOLUCION -> procesarDevolucionPrestamo(); //Devolución
                case OPCION_CONSULTA_BY_USUARIO -> procesarConsultaPorUsuario(); // Consulta de préstamo por usuario
                case OPCION_CONSULTA_BY_TITULO -> procesarConsultaPorTitulo(); // Consulta de préstamo por título
                case OPCION_SALIR -> commonService.volverMenuPrincipal(); //Salir
                default -> commonService.mostrarError(); //Otra opcion
            }
        }while (!OPCION_SALIR.equals(opcion));
    }

    /* Alta:
     * Se solicita el titulo del libro a prestar y se comprueba que exista y este disponible
     * Se solicita el nombre del usuario. Se comprueba que exista
     * Se da de alta el prestamo
     * Se actualiza que hay un ejemplar menos disponible en la biblioteca para poder prestar
     */
    void procesarAltaPrestamo() {
        //Solicitar titulo del libro a prestar
        MaterialBiblioteca materialBiblioteca = solicitarTituloPrestamo();

        //Comprobar que existe el material y que hay ejemplares disponibles.
        if (!comprobarExistencia(materialBiblioteca)) return;

        //Solicitar el nombre del usuario
        Usuario usuario = solicitarDatosUsuario();
        if (usuario == null) return;

        //Alta del préstamo
        Prestamo prestamo = crearPrestamo(usuario, materialBiblioteca);

        //Insertar préstamo y actualizar disponibilidad
        try {
            registrarPrestamo(prestamo, materialBiblioteca);
            commonUtil.mostrarMensaje("Préstamo registrado correctamente");
        }catch (Exception e){
            commonUtil.mostrarMensajeError ("alta préstamo", e);
        }
    }

    /* Devolución:
     * Se solicita el titulo del libro a devolver y se comprueba que exista y que esta prestado
     * Se solicita el nombre del usuario. Se comprueba que exista
     * Se comprueba si el usuario quiere devolver un titulo prestado
     * Se actualiza que hay un ejemplar mas disponible en la biblioteca para poder prestar
     * Se actualiza que el usuario ya no tiene prestado el libro.
     */
    public void procesarDevolucionPrestamo(){

        String titulo = obtenerTituloMaterial();
        if (titulo == null) return;

        //Ir a materialBiblioteca y con el título del libro/revista obtener el idLibro.
        MaterialBiblioteca materialBiblioteca = materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo);
        if (!validarMaterialPrestado (materialBiblioteca,titulo)) return;

        //Solicitar el nombre del usuario
        Usuario usuario = solicitarDatosUsuario();
        if (usuario == null) return;

        //Se obtiene una lista con todos los prestamosId activos del usuario de la tabla prestamos
        List<Prestamo> prestamoList = obtenerPrestamoList(usuario.getId());
        if (prestamoList == null) return;


        List <Integer> idPrestamosList = prestamoList.stream()
                .map (Prestamo::getId) //transforma cada préstamo en su ID.
                .toList(); //recoge los IDs en una nueva lista (List<Integer>).

        List<Integer> idMaterialList = prestamoService.obtenerMaterialIdsPorPrestamos(idPrestamosList);

        if (!idMaterialList.contains(materialBiblioteca.getId())) {
            mostrarMensajeNoTienePrestado(usuario.getNombre(), titulo);
            return;
        }

        try {
            //Actualizar disponibilidad
            materialBibliotecaService.actualizarDisponible(materialBiblioteca, materialBiblioteca.getDisponibles() + 1);

            //Devolver un préstamo -> Actualizar préstamo como no prestado.
            prestamoService.desactivarPrestamoConMaterial(idPrestamosList, materialBiblioteca.getId());

            commonUtil.mostrarMensaje("Préstamo devuelto");
        }catch (Exception e){
            commonUtil.mostrarMensajeError("devolución préstamo", e);
        }

    }

    /* Consulta de préstamo por usuario:
     * Se solicita el nombre del usuario
     * Se devuelven los datos de todos los prestamos vigentes del usuario
     */
    public void procesarConsultaPorUsuario(){

        //Solicitar el nombre del usuario
        Usuario usuario = solicitarDatosUsuario();
        if (usuario == null) return;

        //Se obtiene una lista con todos los prestamosId del usuario de la tabla prestamos
        List<Prestamo> prestamoList = obtenerPrestamoList(usuario.getId());
        if (prestamoList == null) return;


        try {
            //Con cada prestamoId se va a la tabla materialBiblioteca a recuperar el id y el título, y mostrarlo en consola
            prestamoList.stream()
                    //transforma cada préstamo en su lista de materiales.
                    .map(prestamo -> materialBibliotecaService.obtenerMaterialesDelPrestamoById(prestamo.getId()))
                    //aplana todas las listas en un solo flujo de materiales.
                    .flatMap(List::stream)
                    //imprime los datos de cada material.
                    .forEach(material -> {
                        System.out.printf("Material id: %d%n", material.getId());
                        System.out.printf("Título: %s%n", material.getTitulo());
                    });
        } catch (Exception e) {
            commonUtil.mostrarMensajeError("consulta por usuario", e);
        }

    }

    /* Consulta por título:
     * Se solicita el título del libro
     * Con el titulo se obtiene su isbn, para poder despues obtener los autores
     * y se devuelven los datos del libro y sus autores por consola
     */
    public void procesarConsultaPorTitulo(){

        String titulo=solicitarLibroByTitulo();
        Libro libroEncontrado=libroService.buscarLibroByTitulo(titulo);

        if (libroEncontrado == null){
            commonUtil.mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
            return;
        }

        //- Con el titulo del libro, ir a materialBiblioteca y comprobar si el libro esta prestado
        MaterialBiblioteca materialBiblioteca = materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo);
        if (!validarMaterialPrestado (materialBiblioteca,titulo)) return;

        // - Con ese id ir a prestamoMaterial y recuperar el prestamoId
        List<Prestamo> prestamoList = prestamoService.buscarPrestamoByMaterialId(materialBiblioteca.getId());

        if (prestamoList.isEmpty()){
            commonUtil.mostrarMensaje("No hay prestamos activos para ese material");
            return;
        }

        // - Con el prestamoId ir a prestamo y recuperar el usuarioId
        // - Con el usuarioId ir a usuario y recuperar el nombre
        prestamoList.stream()
                .map(Prestamo::getUsuario)
                .map(Usuario::getNombre)
                .forEach(nombre -> commonUtil.mostrarMensaje("El libro \"" + titulo + "\" está prestado a " + nombre));

        /* Es lo mismo que arriba, pero lo de arriba es más eficiente
        prestamoList.forEach(prestamo1 -> {
            Usuario usuario1 = prestamo1.getUsuario();
            System.out.println("El libro \"" + titulo + "\" esta prestado a " + usuario1.getNombre() );

        });
         */

    }

    public String obtenerTituloMaterial(){
        String titulo;
        if (solicitarDevolucionMaterial()) {
            //Solicitar el titulo del libro
            titulo = solicitarLibroByTitulo();
            Libro libroEncontrado = libroService.buscarLibroByTitulo(titulo);

            if (libroEncontrado == null) {
                commonUtil.mostrarMensaje("Libro \"" + titulo + "\" inexistente en la biblioteca");
                return null;
            }
            // O se quiere devolver una revista
        }else{
            //Solicitar el titulo de la revista
            titulo = solicitarRevistaByTitulo();
            Revista revistaEncontrada = revistaService.buscarRevistaByTitulo(titulo);

            if (revistaEncontrada == null) {
                commonUtil.mostrarMensaje("La revista \"" + titulo + "\" inexistente en la biblioteca");
                return null;
            }
        }
        return titulo;
    }

    public boolean validarMaterialPrestado(MaterialBiblioteca materialBiblioteca, String titulo){
        if (materialBiblioteca == null){
            commonUtil.mostrarMensaje("El material \"" + titulo + "\" no existe en la biblioteca");
            return false;
        }

        if (   materialBiblioteca.getTotales() == null
                || materialBiblioteca.getDisponibles() == null
                || materialBiblioteca.getTotales().equals(materialBiblioteca.getDisponibles())){
            commonUtil.mostrarMensaje("El material \"" + titulo + "\" no está prestado");
            return false;
        }
        return true;
    }

    void mostrarMensajeNoTienePrestado(String nombre, String titulo){
        commonUtil.mostrarMensaje("El usuario \"" + nombre + "\" no tiene prestado el material " + titulo);

    }

    Usuario solicitarDatosUsuario() {
        Usuario usuario;
        commonUtil.mostrarMensaje("Introduce el nombre del usuario");
        usuario = usuarioService.consultarUsuarioByNombre(commonUtil.leerEntrada());
        if (usuario==null){
            commonUtil.mostrarMensaje("El usuario no existe");
            return null;
        }
        return usuario;
    }

    public boolean comprobarExistencia(MaterialBiblioteca materialBiblioteca) {
        if (materialBiblioteca == null){
            commonUtil.mostrarMensaje("El material no existe en la biblioteca");
            return false;
        }

        if (materialBiblioteca.getDisponibles()==null || materialBiblioteca.getDisponibles()<=0){
            commonUtil.mostrarMensaje("No hay ejemplares disponibles del material \"" + materialBiblioteca.getTitulo() + "\"");
            return false;
        }

        return true;
    }
    public List<Prestamo> obtenerPrestamoList(int id){
        List<Prestamo> prestamoList = prestamoService.buscarPrestamoByUsuarioIdAndActivo(id);

        if (prestamoList.isEmpty()){
            commonUtil.mostrarMensaje("El usuario \"" + id + "\" no tiene préstamos");
            return null;
        }
        return prestamoList;
    }

    void mostrarMenuPrestamo(){
        System.out.println("""
                \n--- Menú Préstamo ---
                A.- Alta préstamo
                B.- Devolución préstamo
                C.- Consulta préstamo por Usuario
                D.- Consulta préstamo por Titulo
                S.- Salir al menu principal
                Elige una opción:""");
    }

    Prestamo crearPrestamo(Usuario usuario, MaterialBiblioteca materialBiblioteca) {
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setAñoPublicacion(LocalDate.now().getYear());
        prestamo.setMateriales(List.of(materialBiblioteca));
        prestamo.setActivo(true);
        return prestamo;
    }

    void registrarPrestamo(Prestamo prestamo, MaterialBiblioteca materialBiblioteca) {
        boolean prestamoRegistrado = false;

        //Registrar prestamo
        try {
            prestamoService.insertarPrestamo(prestamo);
            commonUtil.mostrarMensaje("Préstamo registrado");
            prestamoRegistrado = true;
        }catch (Exception e){
            commonUtil.mostrarMensajeError("registrar Préstamo", e);
        }

        //Actualizar disponibilidad
        if (prestamoRegistrado) {
            try {
                materialBibliotecaService.actualizarDisponible(materialBiblioteca, materialBiblioteca.getDisponibles() - 1);
                commonUtil.mostrarMensaje("Disponibilidad actualizada");
            } catch (Exception e) {
                commonUtil.mostrarMensajeError("actualizar disponibilidad", e);
            }
        }

    }

    public MaterialBiblioteca solicitarTituloPrestamo (){
        String titulo;

        //Titulo del libro o revista
        commonUtil.mostrarMensaje("Titulo del libro o revista" );
        titulo=commonUtil.leerEntrada();
        return materialBibliotecaService.obtenerMaterialDelPrestamoByTitulo(titulo);
    }

    //Este método devuelve true si el usuario escribe exactamente "LIBRO" (en mayúsculas),
    // y false en cualquier otro caso.
    public boolean solicitarDevolucionMaterial() {
        commonUtil.mostrarMensaje("¿Qué se quiere devolver (Libro / Revista): ?");
        String respuesta = commonUtil.leerEntrada();
        return "LIBRO".equals(respuesta);
    }

    public String solicitarLibroByTitulo () {
        commonUtil.mostrarMensaje("Titulo del libro");
        return commonUtil.leerEntrada();
    }

    public String solicitarRevistaByTitulo () {
        commonUtil.mostrarMensaje("Titulo de la revista");
        return commonUtil.leerEntrada();
    }

}
