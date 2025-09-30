package com.example.biblioteca.service;


import com.example.biblioteca.model.Revista;
import com.example.biblioteca.repository.RevistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RevistaService {

    @Autowired
    private RevistaRepository revistaRepository;

    public RevistaService(RevistaRepository revistaRepository) {
        this.revistaRepository = revistaRepository;
    }

    public void insertarRevista (Revista revista){
        revistaRepository.save(revista);
    }

    public Revista buscarRevistaByTitulo (String titulo){
       Optional<Revista> revista = revistaRepository.findByTitulo(titulo);
       return revista.orElse(null);
    }

    public void eliminarRevistaById (int id) {
        revistaRepository.deleteById(id);
    }

    public List<Revista> listarRevistas (){
        return revistaRepository.findAll();
    }
}