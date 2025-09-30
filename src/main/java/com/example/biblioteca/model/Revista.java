package com.example.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="Revista")
@Data //Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.

public class Revista extends MaterialBiblioteca{

    // Para seguir exigiendo el valor cuando sea una Revista, usa Bean Validation:
    @NotNull(message = "La revista debe tener número de edición")
    @Column(name = "numeroEdicion")
    private int numeroEdicion;

    @NotNull(message = "La revista debe tener una periodicidad")
    @Column(name="periodicidad")
    private String periodicidad;

}
