package com.example.biblioteca.service;
import com.example.biblioteca.dto.UsuarioDTO;
import com.example.biblioteca.expection.OperacionNoPermitidaException;
import com.example.biblioteca.expection.UsuarioNoEncontradoException;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.PrestamoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PrestamoRepository prestamoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository
                          ) {
        this.usuarioRepository=usuarioRepository;
    }

    public void insertarUsuario (Usuario usuario){
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario consultarUsuarioByNombre(String nombre) {
        Optional<Usuario> usuario = usuarioRepository.findByNombre(nombre);
        return usuario.orElse(null);
    }

    public void eliminarUsuarioById (int id){
        usuarioRepository.deleteById(id);
    }

}

