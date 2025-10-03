package com.example.biblioteca.controller;

import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.service.LibroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class LibroController {
    private  final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    //Listar libros
    @GetMapping
    public List<LibroDTO> listarLibro () {
        return libroService.listarLibrosDTO();
    }

    //Alta libro
    @PostMapping
    public ResponseEntity<LibroDTO> crearLibro(@RequestBody LibroDTO dto) {
        var libroCreado = libroService.guardarLibro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(libroCreado);
    }


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
}

