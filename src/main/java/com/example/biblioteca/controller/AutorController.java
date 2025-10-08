package com.example.biblioteca.controller;

import com.example.biblioteca.dto.AutorDTO;
import com.example.biblioteca.expection.AutorNoEncontradoException;
import com.example.biblioteca.serviceDTO.AutorDTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Autores")
public class AutorController {
    private final AutorDTOService autorDTOService;


    public AutorController(AutorDTOService autorDTOService) {
        this.autorDTOService = autorDTOService;
    }

    //Consutar autor por nombre
    @GetMapping("/{nombre}")
    public AutorDTO consultarAutor(@PathVariable String nombre) {
        return autorDTOService.consultarAutorDTO(nombre)
                .orElseThrow(() -> new AutorNoEncontradoException("No existe el autor: '" + nombre.trim() + "'"));
    }


    //Alta autor
    @PostMapping
    public ResponseEntity<AutorDTO> crearAutor(@RequestBody AutorDTO dto) {
        var autorCreado = autorDTOService.guardarAutor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(autorCreado);
    }

    //Body Autor - Para el Post del Postman
    //-- Alta del autor Miguel de Cervantes, sin libros asociados
    /*
    {
        "nombre": "Miguel de Cervantes",
        "librosIds": []
    }
    */

    //Actualizar un autor dado su nombre
    @PutMapping("/{nombreAutor}")
    public ResponseEntity<AutorDTO> actualizarAutor(@PathVariable String nombreAutor, @RequestBody AutorDTO dto) {
        var actualizado = autorDTOService.actualizarAutor(nombreAutor, dto);
        return ResponseEntity.ok(actualizado);
    }
    //Body Autor - Para el Put del Postman
    //-- Se actualiza el nombre del autor Paco Recio por Paco Recio Serrano, y tiene dos libros asociados
    /*
    {
        "nombre": "Paco Recio Serrano",
        "librosIds": ["6","17"]
    }
    */

    //Eliminar un Autor dado su nombre
    @DeleteMapping("/{nombre}")
    public ResponseEntity<String> eliminarAutor(@PathVariable String nombre) {
        autorDTOService.eliminarAutor(nombre);
        return ResponseEntity.ok("Autor '" + nombre + "' eliminado correctamente.");
    }

}

