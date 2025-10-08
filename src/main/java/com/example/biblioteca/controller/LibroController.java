package com.example.biblioteca.controller;

import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.serviceDTO.LibroDTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Libros")
public class LibroController {
    private LibroDTOService libroDTOService;

    public LibroController(LibroDTOService libroDTOService) {
        this.libroDTOService = libroDTOService;
    }


    //Listar libros
    @GetMapping
    public List<LibroDTO> listarLibro () {
        return libroDTOService.listarLibrosDTO();
    }

    //Alta libro
    @PostMapping
    public ResponseEntity<LibroDTO> crearLibro(@RequestBody LibroDTO dto) {
        var libroCreado = libroDTOService.guardarLibro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(libroCreado);
    }

    //Actualizar un libro dado su titulo
    @PutMapping("/{titulo}")
    public ResponseEntity<LibroDTO> actualizarLibro(@PathVariable String titulo, @RequestBody LibroDTO dto) {
        var actualizado = libroDTOService.actualizarLibro(titulo,dto);
        return ResponseEntity.ok(actualizado);
    }

    //Eliminar un libro dado su titulo
    @DeleteMapping("/{titulo}")
    public ResponseEntity<String> eliminarLibro(@PathVariable String titulo) {
        libroDTOService.eliminarLibro(titulo);
        return ResponseEntity.ok("El libro ha sido eliminado correctamente");
    }

    //Body LibroDTO - Para el PostMan, para el Post o el Put
    /*
    {
        "isbn": "15678",
            "añoPublicacion": 2007,
            "titulo": "Filosofia de andar por casa",
            "nombresAutores": ["Cristina Recio", "Paco Recio"],
            "totales": 12,
            "disponibles": 12
    }
    */
}

