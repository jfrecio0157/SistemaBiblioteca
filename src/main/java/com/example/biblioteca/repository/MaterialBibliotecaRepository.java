package com.example.biblioteca.repository;

import com.example.biblioteca.model.MaterialBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaterialBibliotecaRepository extends JpaRepository<MaterialBiblioteca, Integer> {
    MaterialBiblioteca findByTitulo(String titulo); //Devuelve un Optional para poder manejarlo en caso de que no exista

    /*
    //Devuelve el libro y sus autores
    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.autores WHERE l.isbn = :isbn")
    Optional<Libro> findLibroConAutores(@Param("isbn") String isbn);

    Optional<Libro> deleteByIsbn (String isbn);
    */

}
