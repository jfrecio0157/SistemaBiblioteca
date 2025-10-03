package com.example.biblioteca.service;

import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.repository.LibroRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private Validator validator;

    public LibroService(LibroRepository libroRepository, Validator validator) {
        this.libroRepository = libroRepository;
        this.validator = validator;
    }


    public void insertarLibro (Libro libro){
        Set<ConstraintViolation<Libro>> violations = validator.validate(libro);

        //Si el conjunto violations está vacío, no se lanza excepción y el libro se guarda
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        libroRepository.save(libro);
    }

    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    public Libro buscarLibroByIsbn (String isbn){
        return libroRepository.findLibroConAutores(isbn).orElse(null);
    }

    public Libro buscarLibroByTitulo (String titulo){
       Optional<Libro> libro = libroRepository.findByTitulo(titulo);
       return libro.orElse(null);
    }

    public void eliminarLibroById (int id) {
        libroRepository.deleteById(id);
    }

    public void eliminarLibroByIsbn (String isbn) {
        libroRepository.deleteByIsbn(isbn);
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
                nombresAutores
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
        libro.setTitulo(dto.getTitulo());

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

            Autor autor = autorExistente.orElseGet(() -> {
                Autor nuevo = new Autor();
                nuevo.setNombre(nombre);
                return autorRepository.save(nuevo);
            });

            autorList.add(autor);
        }

        libro.setAutores(autorList);

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
    public LibroDTO actualizarLibro(int id, LibroDTO dto) {

        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con id: " + id));

        libro.setIsbn(dto.getIsbn());
        libro.setAñoPublicacion(dto.getAñoPublicacion());
        libro.setTitulo(dto.getTitulo());

        List<Autor> autorList = new ArrayList<>();
        for (String nombre : dto.getNombresAutores()){
            Optional<Autor> autorExistente = autorRepository.findByNombre(nombre);
            Autor autor = autorExistente.orElseThrow(() -> new EntityNotFoundException("Autor no existente: " + nombre));
            autorList.add(autor);
        }
        libro.setAutores(autorList);

        return convertirALibroDTO(libroRepository.save(libro));
    }

    //Para hacer el Delete desde Postman. Borrar un libro dado su id
    public void eliminarLibro(int id) {
        if (!libroRepository.existsById(id)) {
            throw new EntityNotFoundException("Libro no encontrado con id: " + id);
        }
        libroRepository.deleteById(id);
    }
}