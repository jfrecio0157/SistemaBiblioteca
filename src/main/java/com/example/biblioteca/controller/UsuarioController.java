package com.example.biblioteca.controller;

import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.dto.UsuarioDTO;
import com.example.biblioteca.service.LibroService;
import com.example.biblioteca.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Usuarios")
public class UsuarioController {
    private  final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    //Consutar usuario por nombre
    @GetMapping("/{nombre}")
    public UsuarioDTO consultarUsuario(@PathVariable String nombre) {
        return usuarioService.consultarUsuarioDTO(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No existe el usuario: '" + nombre.trim() + "'"));
    }

    //Alta usuario
    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody UsuarioDTO dto) {
        var usuarioCreado = usuarioService.guardarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }
    /*

    //Actualizar un libro dado su id
    @PutMapping("/{id}")
    public ResponseEntity<LibroDTO> actualizarLibro(@PathVariable int id, @RequestBody LibroDTO dto) {
        var actualizado = libroService.actualizarLibro(id,dto);
        return ResponseEntity.ok(actualizado);
    }

    //Eliminar un libro dado su id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable int id) {
        libroService.eliminarLibroById(id);
        return ResponseEntity.noContent().build();
    }
    */

}

