package com.example.biblioteca.component;

import org.springframework.stereotype.Component;

@Component
public class Consola {

    String menuConsola () {
        return """
                \nBienvenido a la Biblioteca
                1.- Usuario
                2.- Libros
                3.- Revistas
                4.- Préstamos
                5.- Autor
                S.- Salir
                Elige una opción:""";
    }
    public void mostrarMenuPrincipal() {
        System.out.println(menuConsola());
    }
}
