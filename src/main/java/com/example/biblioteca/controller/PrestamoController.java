package com.example.biblioteca.controller;

import com.example.biblioteca.dto.PrestamoDTO;

import com.example.biblioteca.serviceDTO.PrestamoDTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/Prestamos")
public class PrestamoController {
    private PrestamoDTOService prestamoDTOService;

    public PrestamoController(PrestamoDTOService prestamoDTOService) {
        this.prestamoDTOService = prestamoDTOService;
    }

    //Alta prestamo
    @PostMapping
    public ResponseEntity<PrestamoDTO> crearPrestamo(@RequestBody PrestamoDTO dto) {
        var prestamoCreado = prestamoDTOService.guardarPrestamo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoCreado);
    }

    //Devolver un préstamo -> Actualizar un préstamo dado su titulo
    @PutMapping("/{titulo}")
    public ResponseEntity<PrestamoDTO> actualizarPrestamo(@PathVariable String titulo, @RequestBody PrestamoDTO dto) {
        var actualizado = prestamoDTOService.actualizarPrestamo(titulo,dto);
        return ResponseEntity.ok(actualizado);
    }


    //Body PrestamoDTO - En el PostMan para el Post o el Put
    /*
    {
            "añoPublicacion": 2023,
            "usuarioId": 9,
            "materialIds": [21]
    }
    */
}

