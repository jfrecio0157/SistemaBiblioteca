package com.example.biblioteca.repository;

import com.example.biblioteca.model.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembresiaRepository extends JpaRepository <Membresia, Integer> {
}
