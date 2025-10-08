package com.example.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO plano para transferir datos de {@code Usuario} sin cargar relaciones completas.
 *
 * Representa las relaciones mediante identificadores (IDs) para evitar payloads pesados y
 * problemas de serialización recursiva.
 *
 */

public class UsuarioDTO {
    private int id;

    /** Nombre del usuario. No puede estar en blanco. */
    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    /** Email del usuario. Debe tener formato válido. */
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email no tiene un formato válido.")
    private String email;

    /**
     * Identificadores de préstamos asociados al usuario (opcional).
     * Suele omitirse en altas/ediciones y utilizarse en lecturas/resúmenes.
     */
    @JsonIgnore
    private List<Integer> prestamosIds;

}

