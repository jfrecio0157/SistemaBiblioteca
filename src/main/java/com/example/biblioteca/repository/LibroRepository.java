package com.example.biblioteca.repository;

import com.example.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Integer> {
    Optional<Libro> findByTitulo(String titulo); //Devuelve un Optional para poder manejarlo en caso de que no exista

    //Devuelve el libro y sus autores
    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.autores WHERE l.isbn = :isbn")
    Optional<Libro> findLibroConAutores(@Param("isbn") String isbn);

    //Dado el nombre de un autor, obtener un libro
    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.autores a WHERE a.nombre = :nombre")
    List<Optional<Libro>> findLibroByAutor(@Param("nombre") String nombre);


    Optional<Libro> deleteByIsbn (String isbn);
    Optional<Libro> findByIsbn (String isbn);


}
