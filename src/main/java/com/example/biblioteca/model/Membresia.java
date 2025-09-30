package com.example.biblioteca.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Table(name="Membresia")
@Data //Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.

public class Membresia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Para claves primaria autogeneradas
    @Column(name = "id", nullable = false)
    private int id;

    @Temporal(TemporalType.DATE)
    @Column(name="fechaInicio")
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    @Column(name="fechaExpiracion")
    private Date fechaExpiracion;

    @Enumerated(EnumType.STRING)
    @Column(name="tipoMembresia")
    private TipoMembresia tipoMembresia;

    @Column(name="activa")
    private boolean activa;

}

