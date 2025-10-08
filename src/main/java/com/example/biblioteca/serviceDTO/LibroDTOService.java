package com.example.biblioteca.serviceDTO;

import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.expection.AutorNoEncontradoException;
import com.example.biblioteca.expection.LibroNoEncontradoException;
import com.example.biblioteca.expection.LibroPrestadoException;
import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class LibroDTOService {

    @Autowired
    private final LibroRepository libroRepository;

    @Autowired
    private final AutorRepository autorRepository;

    public LibroDTOService(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }


    //******************************************
    //Métodos para APIs Rest
    //******************************************
    /**
     * Convierte una entidad {@link Libro} en un objeto {@link LibroDTO} para su uso en la capa de presentación.
     *
     * Este método extrae los datos relevantes del libro, incluyendo su ID, ISBN, año de publicación,
     * título y los nombres de los autores asociados, y los empaqueta en un DTO que puede ser
     * fácilmente serializado como JSON en una API REST.
     *
     * @param libro la entidad {@link Libro} que se desea convertir.
     * @return un objeto {@link LibroDTO} que representa los datos del libro de forma simplificada.
     */

    public LibroDTO convertirALibroDTO(Libro libro) {
        // Validaciones explícitas de nulidad
        Objects.requireNonNull(libro.getAutores(),"La lista de autores no puede ser null");

        //Se filtra por lo no nulos.
        var nombresAutores = libro.getAutores().stream()
                .filter(Objects::nonNull)
                .map(Autor::getNombre)
                .toList(); // Desde Java 16 en adelante, .toList() es una forma segura y concisa

        return new LibroDTO(
                libro.getId(),
                libro.getIsbn(),
                libro.getAñoPublicacion(),
                libro.getTitulo(),
                nombresAutores,
                libro.getTotales(),
                libro.getDisponibles()
        );
    }

    /**
     * Convierte un {@link LibroDTO} en una entidad {@link Libro}.
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

    public Libro convertirDesdeDTO(LibroDTO dto) {
        // Validaciones explícitas de nulidad
        Objects.requireNonNull(dto, "El DTO de libro no puede ser null");
        Objects.requireNonNull(dto.getNombresAutores(), "La lista de nombres de autores no puede ser null");

        Libro libro = new Libro();
        libro.setIsbn(dto.getIsbn());
        libro.setAñoPublicacion(dto.getAñoPublicacion());
        libro.setTitulo(dto.getTitulo().toUpperCase());

        // Normalizar nombres: descartar nulos/vacíos, recortar espacios y deduplicar
        List<String> nombresAutoresLimpios = dto.getNombresAutores().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(nombre -> !nombre.isEmpty())
                .distinct()
                .toList();

        List<Autor> autorList = new ArrayList<>();

        for (String nombre : nombresAutoresLimpios) {
            // Buscar si ya existe un autor con ese nombre
            Optional<Autor> autorExistente = autorRepository.findByNombre(nombre);

            //Si el autor no existe, se da de alta
            Autor autor = autorExistente.orElseGet(() -> {
                Autor nuevo = new Autor();
                nuevo.setNombre(nombre.toUpperCase());
                return autorRepository.save(nuevo);
            });

            autorList.add(autor);
        }

        libro.setAutores(autorList);
        libro.setTotales(dto.getTotales());
        libro.setDisponibles(dto.getDisponibles());

        return libro;
    }


    //Para hacer el Get desde Postman - Listar libros
    public List<LibroDTO> listarLibrosDTO() {
        return libroRepository.findAll().stream()
                .map(this::convertirALibroDTO)
                .toList();
    }


    //Para hacer el Post desde Postman. - Insertar libro
    public LibroDTO guardarLibro(LibroDTO dto) {
        var libro = convertirDesdeDTO(dto);
        var libroGuardado = libroRepository.save(libro);
        return convertirALibroDTO(libroGuardado);
    }

    //Para hacer el Put desde Postman. Actualizar un libro
    public LibroDTO actualizarLibro(String titulo, LibroDTO dto) {

        //Buscar el libro dado el título. Si no lo encuentra, lanza una excepción.
        Libro libro = libroRepository.findByTitulo(titulo)
                .orElseThrow(() -> new LibroNoEncontradoException("Libro no encontrado con titulo: " + titulo));

        libro.setIsbn(dto.getIsbn());
        libro.setAñoPublicacion(dto.getAñoPublicacion());
        libro.setTitulo(dto.getTitulo().toUpperCase()); //En la línea de comandos se pone el título viejo y en el body el título nuevo.

        //Dado una lista de autores, se obtiene cada uno de los autores.
        //Si no existiera el autor, se devuelve una excepción.
        List<Autor> autorList = new ArrayList<>();
        for (String nombre : dto.getNombresAutores()){
            Optional<Autor> autorExistente = autorRepository.findByNombre(nombre);
            Autor autor = autorExistente.orElseThrow(() -> new AutorNoEncontradoException("Autor no existente: " + nombre));
            autorList.add(autor);
        }
        libro.setAutores(autorList);
        libro.setTotales(dto.getTotales());
        libro.setDisponibles(dto.getDisponibles());

        //Se salva el libro y se devuelve el libro en formato DTO.
        return convertirALibroDTO(libroRepository.save(libro));
    }

    //Para hacer el Delete desde Postman. Borrar un libro dado su título
    public void eliminarLibro(String titulo) {
        Optional<Libro> libro = libroRepository.findByTitulo(titulo.toUpperCase());

        //Comprobar que el libro existe
        if (libro.isEmpty()) {
            throw new LibroNoEncontradoException("Libro no encontrado con titulo '" + titulo + "'");
        }

        //Comprobar que no está prestado.
        if (libro.get().getTotales() > libro.get().getDisponibles()){
            throw new LibroPrestadoException("Libro no se puede dar de baja. Está prestado.");
        }

        libroRepository.deleteById(libro.get().getId());
    }
}