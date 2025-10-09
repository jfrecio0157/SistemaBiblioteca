package com.example.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoDTO {
    private int id;
    private LocalDate fechaPrestamo;
    private String nombreUsuario;
    private List<String> tituloMateriales;
    private boolean activos;

}