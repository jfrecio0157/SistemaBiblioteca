package com.example.biblioteca.controller;

import com.example.biblioteca.dto.RevistaDTO;
import com.example.biblioteca.serviceDTO.RevistaDTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Revistas")
public class RevistaController {
    private RevistaDTOService revistaDTOService;

    public RevistaController(RevistaDTOService revistaDTOService) {
        this.revistaDTOService = revistaDTOService;
    }

    //Listar revistas
    @GetMapping
    public List<RevistaDTO> listarRevista () {
        return revistaDTOService.listarRevistasDTO();
    }

    //Alta revista
    @PostMapping
    public ResponseEntity<RevistaDTO> crearRevista(@RequestBody RevistaDTO dto) {
        var revistaCreada = revistaDTOService.guardarRevista(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(revistaCreada);
    }

    //Actualizar uns revista dado su titulo
    //-- Se puede actualizar el numeroEdicion, periodicidad, totales y disponibles.
    @PutMapping("/{titulo}")
    public ResponseEntity<RevistaDTO> actualizarRevista(@PathVariable String titulo, @RequestBody RevistaDTO dto) {
        var actualizado = revistaDTOService.actualizarRevista(titulo, dto);
        return ResponseEntity.ok(actualizado);
    }

    //Eliminar una revista dado su titulo
    @DeleteMapping("/{titulo}")
    public ResponseEntity<String> eliminarRevista(@PathVariable String titulo) {
        revistaDTOService.eliminarRevista(titulo);

        return ResponseEntity.ok("La revista ha sido eliminada correctamente");
    }

}

