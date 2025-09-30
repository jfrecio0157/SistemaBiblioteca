package com.example.biblioteca.repository;

import com.example.biblioteca.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutorRepository extends JpaRepository<Autor,Integer> {
    /*
    LEFT JOIN FETCH a.libros
    carga los libros si existen, pero no excluye el autor si no tiene ningun libro asociado
    JOIN indica que quieres unir la entidad Autor con su colección libros.
    FETCH indica que quieres cargar los libros inmediatamente, no de forma perezosa.
    Esto evita que Hibernate haga una segunda consulta cuando accedas a autor.getLibros().
    */
    @Query("SELECT DISTINCT a FROM Autor a LEFT JOIN FETCH a.libro WHERE a.nombre = :nombre")
    Optional<Autor> findByNombre(
            @Param("nombre") String nombre)
            ;

}
