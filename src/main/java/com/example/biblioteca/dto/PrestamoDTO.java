package com.example.biblioteca.dto;

import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoDTO {
    private int id;
    private int añoPublicacion;
    private String nombreUsuario;
    private List<String> tituloMateriales;
    private boolean activos;

}