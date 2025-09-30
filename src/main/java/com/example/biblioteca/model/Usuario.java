package com.example.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Usuario")
@Data //Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Para claves primaria autogeneradas
    @Column(name = "id", nullable = false)
    private int id;

    @Column (name = "nombre", nullable = false, unique = false)
    private String nombre;

    @Column (name = "email", nullable = false, unique = false)
    private String email;

    //Un usuario tiene una unica membresia
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "membresia_id", referencedColumnName = "id")
    private Membresia membresia;

    //Un usuario puede tener uno o varios prestamos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Prestamo> prestamos;

}
