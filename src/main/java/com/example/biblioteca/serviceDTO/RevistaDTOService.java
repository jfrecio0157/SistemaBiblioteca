package com.example.biblioteca.serviceDTO;

import com.example.biblioteca.dto.RevistaDTO;
import com.example.biblioteca.expection.RevistaNoEncontradoException;
import com.example.biblioteca.expection.RevistaPrestadaException;
import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.model.Revista;
import com.example.biblioteca.repository.RevistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RevistaDTOService {

    @Autowired
    private RevistaRepository revistaRepository;


    public RevistaDTOService(RevistaRepository revistaRepository) {
        this.revistaRepository = revistaRepository;
    }


    //******************************************
    //Métodos para APIs Rest
    //******************************************
    /**
     * Convierte una entidad {@link Revista} en un objeto {@link RevistaDTO} para su uso en la capa de presentación.
     *
     * Este método extrae los datos relevantes de la revista, incluyendo su ID, título,
     * número de edición, periodicidad, totales y disponibles, y los empaqueta en un DTO que puede ser
     * fácilmente serializado como JSON en una API REST.
     *
     * @param revista la entidad {@link Revista} que se desea convertir.
     * @return un objeto {@link RevistaDTO} que representa los datos del libro de forma simplificada.
     */

    public RevistaDTO convertirARevistaDTO(Revista revista) {
        return new RevistaDTO(
                revista.getId(),
                revista.getTitulo(),
                revista.getNumeroEdicion(),
                revista.getPeriodicidad(),
                revista.getTotales(),
                revista.getDisponibles()
        );
    }

    /**
     * Convierte un {@link RevistaDTO} en una entidad {@link Revista}.
     *
     * Asigna los campos básicos (ISBN, año de publicación y título) y resuelve la lista
     * de autores a partir de sus nombres: por cada nombre en {@code dto.getNombresAutores()},
     * intenta recuperar un {@link Autor} existente con {@code autorRepository.findByNombre(nombre)};
     * si no existe, crea y persiste un nuevo {@link Autor} con ese nombre y lo añade a la lista.
     * Finalmente, asocia la lista de autores al libro y devuelve la entidad construida.
     *
     * >Efectos secundarios
     *   Puede persistir nuevos autores en la base de datos mediante {@code autorRepository.save}.
     *   Realiza una consulta por cada nombre de autor (coste aproximado O(n)).
     *
     * Precondiciones
     *   {@code dto} no debe ser {@code null}.
     *   {@code dto.getNombresAutores()} no debe ser {@code null} (puede estar vacía).
     *   Los nombres de autor se usan tal cual para búsqueda/creación.
     *
     * Postcondiciones
     *   >El {@link Libro} devuelto no se persiste automáticamente; solo los autores nuevos
     *       pueden haber sido insertados en la base de datos.
     *
     *Ejemplo de uso
     * Libro libro = servicio.convertirDesdeDTO(dto);
     *
     * @param dto DTO de entrada con los datos del libro (ISBN, año de publicación, título) y los nombres de sus autores.
     * @return Entidad {@link Libro} creada a partir del contenido del DTO.
     * @throws NullPointerException si {@code dto} es {@code null} o si {@code dto.getNombresAutores()} es {@code null}.
     * @throws org.springframework.dao.DataAccessException si ocurre un error de acceso a datos al consultar o guardar autores.
     *
     */

    public Revista convertirDesdeDTO(RevistaDTO dto) {
        // Validaciones explícitas de nulidad
        Objects.requireNonNull(dto, "El DTO de revista no puede ser null");

        Revista revista = new Revista();
        //El id es la clave, autogenerada.
        //revista.setId(dto.getId()); //La clave se autogenera
        revista.setTitulo(dto.getTitulo().toUpperCase());
        revista.setNumeroEdicion(dto.getNumeroEdicion());
        revista.setPeriodicidad(dto.getPeriodicidad().toUpperCase());
        revista.setTotales(dto.getTotales());
        revista.setDisponibles(dto.getDisponibles());

        return revista;
    }


    //Para hacer el Get desde Postman - Listar revistas
    public List<RevistaDTO> listarRevistasDTO() {
        return revistaRepository.findAll().stream()
                .map(this::convertirARevistaDTO)
                .toList();
    }

    //Para hacer el Post desde Postman. - Insertar revista
    public RevistaDTO guardarRevista(RevistaDTO dto) {
        var revista = convertirDesdeDTO(dto);
        var revistaGuardada = revistaRepository.save(revista);
        return convertirARevistaDTO(revistaGuardada);
    }

    //Para hacer el Put desde Postman. Actualizar una revista
    public RevistaDTO actualizarRevista(String titulo, RevistaDTO dto) {

        Revista revista = revistaRepository.findByTitulo(titulo.toUpperCase())
                .orElseThrow(() -> new RevistaNoEncontradoException("Revista no encontrada con titulo:  '" + titulo + "'"));

        //revista.setId(dto.getId()); //El id es una clave interna que no se conoce y no se puede ni pedir ni cambiar
        revista.setTitulo(dto.getTitulo().toUpperCase()); //El titulo del postman es para localizar la revista en la bbdd, la del body para actualizarla
        revista.setNumeroEdicion(dto.getNumeroEdicion());
        revista.setPeriodicidad(dto.getPeriodicidad().toUpperCase());
        revista.setTotales(dto.getTotales());
        revista.setDisponibles(dto.getDisponibles());

        return convertirARevistaDTO(revistaRepository.save(revista));
    }

    //Para hacer el Delete desde Postman. Borrar una revista dado su titulo
    public void eliminarRevista(String titulo) {
        //Se comprueba que la revista exista
        Revista revista = revistaRepository.findByTitulo(titulo.toUpperCase())
                .orElseThrow(() -> new RevistaNoEncontradoException("Revista no encontrada con titulo:  '" + titulo + "'"));

        //Se comprueba que no está prestada
        if (revista.getTotales() > revista.getDisponibles()){
            throw new RevistaPrestadaException("Revista no se puede dar de baja. Está prestada");
        }

        revistaRepository.deleteById(revista.getId());
    }

    //Body del Postman para el Post y el Put.
    /*
    {
        "titulo": "El espacio infinito",
            "numeroEdicion": 5,
            "periodicidad": "Mensual",
            "totales": 13,
            "disponibles": 12
    }
    */
}