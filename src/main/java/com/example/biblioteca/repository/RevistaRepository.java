package com.example.biblioteca.repository;

import com.example.biblioteca.model.Revista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RevistaRepository extends JpaRepository<Revista, Integer> {
    Optional<Revista> findByTitulo(String titulo); //Devuelve un Optional para poder manejarlo en caso de que no exista

}
