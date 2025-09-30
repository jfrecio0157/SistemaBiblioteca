package com.example.biblioteca.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "autor")
@Data //Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.

public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Para claves primaria autogeneradas
    @Column(name = "id", nullable = false)
    private int id;

    @Column (name = "nombre", nullable = false, unique = false)
    private String nombre;

    @ManyToMany(mappedBy = "autores", cascade = CascadeType.ALL)
    private List<Libro> libro;

    //Constructor
    public Autor(String nombre) {
        this.nombre = nombre;
    }
}
