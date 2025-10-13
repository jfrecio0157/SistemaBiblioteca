package com.example.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
//@Table(name = "libro") En el jdk 17 -> No se define como @Table porque MaterialBiblioteca ya está definida como @Table
@Getter
@Setter
@NoArgsConstructor //Genera un constructor sin argumentos.
@AllArgsConstructor //Genera un constructor con todos los campos.


public class Libro extends MaterialBiblioteca{
    @NotNull(message = "El libro debe tener un isbn")
    @Column(name = "isbn")
    private String isbn;

    @NotNull(message = "El libro debe tener un año de publicación")
    @Column(name = "añoPublicacion")
    private int añoPublicacion;

    /**
     * Para establecer una relación entra las tablas Libro y Autor como un libro puede tener varios autores
     * y un autor puede tener varios libros, es necesario usar la relación @ManyToMany
     */
    @ManyToMany
    @JoinTable(
            name = "LibroAutor",
            joinColumns = @JoinColumn(name = "libroIsbn"),
            inverseJoinColumns = @JoinColumn(name = "AutorId")
    )
    @NotEmpty(message="Un libro debe tener al menos un autor")
    private List<Autor> autores;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro)) return false;
        Libro libro = (Libro) o;
        return id != 0 && id == libro.id;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
