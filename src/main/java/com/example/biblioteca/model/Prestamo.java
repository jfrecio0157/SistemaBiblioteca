package com.example.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="Prestamo")
@Data //Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.

public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Para claves primaria autogeneradas
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "añoPublicacion", nullable = false)
    private int añoPublicacion;

    @ManyToOne
    @JoinColumn(name="usuario_id", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    /**
     * Para establecer una relación entra las tablas Libro y Autor como un libro puede tener varios autores
     * y un autor puede tener varios libros, es necesario usar la relación @ManyToMany
     */
    @ManyToMany
    @JoinTable(
            name = "PrestamoMaterial",
            joinColumns = @JoinColumn(name = "PrestamoId"),
            inverseJoinColumns = @JoinColumn(name = "MaterialId")
    )
    private List<MaterialBiblioteca> materiales;



    @Column(name="activo", nullable = false)
    private boolean activo;

}

