package com.example.biblioteca.serviceDTO;

import com.example.biblioteca.dto.PrestamoDTO;
import com.example.biblioteca.expection.*;
import com.example.biblioteca.model.*;
import com.example.biblioteca.repository.MaterialBibliotecaRepository;
import com.example.biblioteca.repository.PrestamoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PrestamoDTOService {
    @Autowired
    private final PrestamoRepository prestamoRepository;

    @Autowired
    private final UsuarioRepository usuarioRepository;

    @Autowired
    private final MaterialBibliotecaRepository materialBibliotecaRepository;

    public PrestamoDTOService(PrestamoRepository prestamoRepository,
                              UsuarioRepository usuarioRepository,
                              MaterialBibliotecaRepository materialBibliotecaRepository) {
        this.prestamoRepository= prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.materialBibliotecaRepository = materialBibliotecaRepository;
    }


    //******************************************
    //Métodos para APIs Rest
    //******************************************
    /**
     * Convierte una entidad {@link Prestamo} en un objeto {@link PrestamoDTO} para su uso en la capa de presentación.
     *
     * Este método extrae los datos relevantes del préstamo, incluyendo su ID, año de publicación, usuario id,
     * ids de materiales y activo, y los empaqueta en un DTO que puede ser
     * fácilmente serializado como JSON en una API REST.
     *
     * @param prestamo la entidad {@link Prestamo} que se desea convertir.
     * @return un objeto {@link PrestamoDTO} que representa los datos del libro de forma simplificada.
     */

    public PrestamoDTO convertirAPrestamoDTO(Prestamo prestamo) {
        Usuario usuario = prestamo.getUsuario();

        List<String> materialTituloList = new ArrayList<>();

        List<MaterialBiblioteca> materialesList = prestamo.getMateriales();
        if (!materialesList.isEmpty()){
            for (MaterialBiblioteca materialBiblioteca : materialesList){
                materialTituloList.add(materialBiblioteca.getTitulo());
            }
        }

        return new PrestamoDTO(
                prestamo.getId(),
                prestamo.getFechaPrestamo(),
                usuario.getNombre(),
                materialTituloList,
                prestamo.isActivo()
        );

    }

    /**
     * Convierte un {@link PrestamoDTO} en una entidad {@link Prestamo}.
     */

    public Prestamo convertirDesdeDTO(PrestamoDTO dto) {
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(dto.getFechaPrestamo());
        prestamo.setActivo(true);

        //Se busca el usuario dado su id. Si no lo encuentra, se devuelve una excepcion.
        Usuario usuario = usuarioRepository.findByNombre(dto.getNombreUsuario().toUpperCase())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con nombre: '" + dto.getNombreUsuario() + "'"));

        prestamo.setUsuario(usuario);

        //Se busca la lista de materiales
        List<MaterialBiblioteca> materialBibliotecaList = new ArrayList<>();

        if (!dto.getTituloMateriales().isEmpty()){
            for (String tituloMaterial : dto.getTituloMateriales()){
                MaterialBiblioteca materialBiblioteca = buscarMaterialByTitulo(tituloMaterial);

                materialBibliotecaList.add(materialBiblioteca);
            }
        }
        prestamo.setMateriales(materialBibliotecaList);

        return prestamo;
    }

    public MaterialBiblioteca buscarMaterialByTitulo(String tituloMaterial) {

        return Optional.of(materialBibliotecaRepository.findByTitulo(tituloMaterial))
                .orElseThrow(() -> new MaterialNoEncontradoException("Material no encontrado con tituloMaterial: '" + tituloMaterial + "'"));
    }


    //Para hacer el Post desde Postman. - Insertar préstamo
    public PrestamoDTO guardarPrestamo(PrestamoDTO dto) {
        var prestamo = convertirDesdeDTO(dto);
        disminuirMaterialDisponible(dto.getTituloMateriales());
        var prestamoGuardado = prestamoRepository.save(prestamo);

        return convertirAPrestamoDTO(prestamoGuardado);
    }


    //Para hacer el Put desde Postman. Devolver un préstamo
    //Error -> desactiva un préstamo completo cuando es posible que tenga más de un título en el préstamo y solo se devuelva uno de ellos.
    public PrestamoDTO actualizarPrestamo(String titulo, PrestamoDTO dto) {

        //Ir a materialBiblioteca y con el título del libro/revista obtener el material
        MaterialBiblioteca materialBiblioteca = materialBibliotecaRepository.findByTitulo(titulo);

        //Comprobar que está prestado
        if (   materialBiblioteca.getTotales() == null
                || materialBiblioteca.getDisponibles() == null
                || materialBiblioteca.getTotales().equals(materialBiblioteca.getDisponibles())){
                throw new MaterialNoEncontradoException("El libro / revista con titulo '" + titulo + "' no esta prestado");
        }

        //Se obtiene una lista con todos los prestamosId activos del usuario de la tabla prestamos
        Optional<Usuario> usuario = usuarioRepository.findByNombre(dto.getNombreUsuario());
        if (usuario.isPresent()) {
            List<Prestamo> prestamoList = prestamoRepository.findByUsuario_idAndActivo(usuario.get().getId());
            if (!prestamoList.isEmpty()) {
                List<Integer> idPrestamosList = prestamoList.stream()
                        .map(Prestamo::getId) //transforma cada préstamo en su ID.
                        .toList(); //recoge los IDs en una nueva lista (List<Integer>).

                List<Integer> idMaterialList = obtenerMaterialIdsPorPrestamos(idPrestamosList);

                if (!idMaterialList.contains(materialBiblioteca.getId())) {
                    throw new MaterialNoEncontradoException("El libro / revista con titulo '" + titulo + "' no esta prestado para el usuario: '" + dto.getNombreUsuario() + "'");
                }

                //Actualizar disponibilidad
                aumentarMaterialDisponible(materialBiblioteca, materialBiblioteca.getDisponibles() + 1);

                //Devolver un préstamo -> Actualizar préstamo como no prestado.
                desactivarPrestamoConMaterial(idPrestamosList, materialBiblioteca.getId());
            }
        }

        return dto;
    }

    public void  disminuirMaterialDisponible(List<String> materialTituloList){
        for (String materialTitulo : materialTituloList){
            MaterialBiblioteca materialBiblioteca = buscarMaterialByTitulo(materialTitulo);
            materialBiblioteca.setDisponibles(materialBiblioteca.getDisponibles() - 1);
            materialBibliotecaRepository.save(materialBiblioteca);
        }
    }

    public void aumentarMaterialDisponible(MaterialBiblioteca materialBiblioteca, int disponible){
        materialBiblioteca.setDisponibles(disponible);
        materialBibliotecaRepository.save(materialBiblioteca);
    }

    public void  desactivarPrestamoConMaterial(List<Integer> prestamoIds, int materialId) {
        List<Prestamo> prestamos = prestamoRepository.findAllWithMaterialesById(prestamoIds);

        prestamos.stream()
                .filter(prestamo -> prestamo.getMateriales().stream()
                        .anyMatch(material -> material.getId() == materialId))
                .findFirst()
                .ifPresent(prestamo -> {
                    prestamo.setActivo(false);
                    prestamoRepository.save(prestamo);
                });
    }
    public  List<Integer> obtenerMaterialIdsPorPrestamos(List<Integer> idPrestamosList){
        List<Prestamo> prestamos = prestamoRepository.findAllById(idPrestamosList);

        return prestamos.stream()
                .flatMap(prestamo -> prestamo.getMateriales().stream())
                .map(MaterialBiblioteca::getId)
                .distinct()
                .toList();
    }

}