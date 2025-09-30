package com.example.biblioteca.service;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
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
