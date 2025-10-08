package com.example.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevistaDTO {
    private int id;
    private String titulo;
    private int numeroEdicion;
    private String periodicidad;
    private int totales;
    private int disponibles;
}

