package com.example.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibroDTO {
    private int id;
    private String isbn;
    private int añoPublicacion;
    private String titulo;
    private List<String> nombresAutores;
    private int totales;
    private int disponibles;
}

