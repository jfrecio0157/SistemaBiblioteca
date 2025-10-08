package com.example.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO plano para transferir datos de {@code Autor} sin cargar relaciones completas.
 *
 * Representa las relaciones mediante identificadores (IDs) para evitar payloads pesados y
 * problemas de serialización recursiva.
 *
 */

public class AutorDTO {
    private int id;

    /** Nombre del autor. No puede estar en blanco. */
    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    /**
     * Identificadores de libros asociados al autor (opcional).
     * Suele omitirse en altas/ediciones y utilizarse en lecturas/resúmenes.
     */
    private List<Integer> librosIds;

}

