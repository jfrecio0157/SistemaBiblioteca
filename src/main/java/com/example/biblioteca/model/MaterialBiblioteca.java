package com.example.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="MaterialBiblioteca")
@Data //Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.

public abstract class MaterialBiblioteca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Para claves primaria autogeneradas
    @Column(name = "id", nullable = false)
    protected int id;

    @Column(name = "titulo", nullable = false)
    protected String titulo;

    //Definido como Integer en lugar de int para que admita valores nulos
    @Column(name = "totales")
    protected Integer totales;

    //Definido como Integer en lugar de int para que admita valores nulos
    @Column(name = "disponibles")
    protected Integer disponibles;

    @ManyToMany(mappedBy = "materiales", cascade = CascadeType.ALL)
    private List<Prestamo> prestamos;


}
