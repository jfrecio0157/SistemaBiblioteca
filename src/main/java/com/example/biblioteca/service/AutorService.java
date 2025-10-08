package com.example.biblioteca.service;

import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.dto.UsuarioDTO;
import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional

public class AutorService {

    @Autowired
    private AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public void insertarAutor(String nombre) {
        Autor autor = new Autor(nombre);
        autorRepository.save(autor);
    }

    public Autor buscarAutorByNombre (String nombre){
       Optional<Autor> autor = autorRepository.findByNombre(nombre);
       return autor.orElse(null);
    }

    public void eliminarAutorById (int id){
        if (autorRepository.existsById(id)) {
            autorRepository.deleteById(id);
        }
    }

}
