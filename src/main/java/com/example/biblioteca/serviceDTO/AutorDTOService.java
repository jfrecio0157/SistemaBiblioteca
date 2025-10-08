package com.example.biblioteca.serviceDTO;
import com.example.biblioteca.dto.AutorDTO;
import com.example.biblioteca.expection.AutorNoEncontradoException;
import com.example.biblioteca.expection.LibroNoEncontradoException;
import com.example.biblioteca.expection.OperacionNoPermitidaException;
import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.repository.LibroRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class AutorDTOService {
    @Autowired
    AutorRepository autorRepository;

    @Autowired
    LibroRepository libroRepository;

    public AutorDTOService(AutorRepository autorRepository,
                           LibroRepository libroRepository) {
        this.autorRepository=autorRepository;
        this.libroRepository=libroRepository;
    }

    //******************************************
    //Métodos para APIs Rest
    //******************************************
    /**
     * Convierte una entidad {@link Autor} en un objeto {@link AutorDTO} para su uso en la capa de presentación.
     *
     * Este método extrae los datos relevantes del Usuario, incluyendo su ID, nombre y libros asociados,
     * y los empaqueta en un DTO que puede ser
     * fácilmente serializado como JSON en una API REST.
     *
     * @param autor la entidad {@link Autor} que se desea convertir.
     * @return un objeto {@link AutorDTO} que representa los datos del autor de forma simplificada.
     */

    public AutorDTO convertirAAutorDTO (Autor autor){
        Objects.requireNonNull(autor, "El autor no puede ser nulo");

        var listaLibrosId = (autor.getLibro() == null ? List.<Libro>of() : autor.getLibro())
                .stream()
                .filter(Objects::nonNull)
                .map(Libro::getId)
                .toList()
                ;

        return new AutorDTO(
                autor.getId(),
                autor.getNombre(),
                listaLibrosId
        );
    }

    public Autor convertirAAutor(AutorDTO dto){
        Autor autor = new Autor();

        //El id, que es la clave, se autogenera
        //autor.setId(dto.getId());
        autor.setNombre(dto.getNombre().toUpperCase());

        List<Libro> libroList = new ArrayList<>();
        if (!dto.getLibrosIds().isEmpty()) {
            for (Integer libroId : dto.getLibrosIds()) {
                libroList.add(obtenerPorId(libroId));
            }
        }

        autor.setLibro(libroList);
        return autor;
    }

    public Libro obtenerPorId(Integer id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException("Libro no encontrado con id: " + id));
    }

    //Para hacer el Get desde Postman - Consultar autor por nombre
    @Transactional
    public Optional<AutorDTO> consultarAutorDTO(String nombre){
        Objects.requireNonNull(nombre, "El nombre del autor a consultar no puede ser nulo o vacío");

        String nombreValido = validarNombre(nombre);

        return autorRepository.findByNombre(nombreValido).stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(this::convertirAAutorDTO)
                ;
    }

    private String validarNombre (String nombre){
        if (nombre == null || nombre.isEmpty()){
            return null;
        }
        return nombre.trim();
    }


    //Para hacer el Post desde Postman. - Guardar autor
    public AutorDTO guardarAutor(AutorDTO dto){
        var autor = convertirAAutor(dto);
        var autorGuardado = autorRepository.save(autor);
        return convertirAAutorDTO(autorGuardado);
    }



    //Para hacer el Put desde Postman. Actualizar un Autor.
    public AutorDTO actualizarAutor(String nombreAutor, AutorDTO dto) {
        //Buscar el autor dado el nombreAutor. Si no lo encuentra, lanza una excepción.
        Autor autor = autorRepository.findByNombre(nombreAutor)
                .orElseThrow(() -> new AutorNoEncontradoException("El autor + '"+ nombreAutor + "' no existe en la biblioteca"));

        //El id no se puede cambiar porque es la clave autogenerada
        //autor.setId(dto.getId());
        autor.setNombre(dto.getNombre().toUpperCase()); //Con nombreAutor se busca el autor tal como está en la bbdd. Con dto.getnombre se guarda el nuevo nombre.

        List<Libro> libroList = new ArrayList<>();
        for (Integer libroId : dto.getLibrosIds()){
            Libro libro = libroRepository.findById(libroId)
                . orElseThrow(() -> new LibroNoEncontradoException("Libro no encontrado con id: " + libroId));

            libroList.add(libro);
        }
        autor.setLibro(libroList);

        return (convertirAAutorDTO(autorRepository.save(autor)));
    }


    public List<Optional<Libro>> obtenerPorNombre(String nombre) {
        return libroRepository.findLibroByAutor(nombre);
    }

    //Para hacer el Delete desde Postman. Borrar un autor dado su nombre
    public void eliminarAutor(String nombre) {
        Optional<Autor> autor = autorRepository.findByNombre(nombre.toUpperCase());

        //Spring detecta que esta excepción no fue capturada en el controlador.
        //Busca un manejador en las clases anotadas con @ControllerAdvice.-> que esta en ManejadorGlobalDeErrores
        if (autor.isEmpty()){
            throw new AutorNoEncontradoException("Autor no encontrado con nombre: '" + nombre + "'");
        }

        //Comprobar que no tiene libros en la biblioteca
        List<Optional<Libro>> libro = obtenerPorNombre(nombre.toUpperCase());

        if (!libro.isEmpty()){
            throw new OperacionNoPermitidaException("Autor '" + nombre + "' no se puede dar de baja. Tiene libros en la biblioteca");
        }

        autorRepository.deleteById(autor.get().getId());

    }

}

