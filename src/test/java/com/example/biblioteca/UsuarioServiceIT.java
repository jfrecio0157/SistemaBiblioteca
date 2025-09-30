package com.example.biblioteca;

import com.example.biblioteca.model.Revista;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.RevistaRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import com.example.biblioteca.service.RevistaService;
import com.example.biblioteca.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UsuarioService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
class UsuarioServiceIT {

    @Autowired
    UsuarioService usuarioService;
    @Autowired
    UsuarioRepository usuarioRepository;


    @Test
    @Timeout(10)
    void insertarUsuario() {
        //Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("FRANCISCO GARCIA");
        usuario.setEmail("FRANCISCOGAR@GMAIL.COM");

        //Act
       usuarioService.insertarUsuario(usuario);

        //Assert
        assertThat(usuarioRepository.findByNombre("FRANCISCO GARCIA").isPresent());
    }

    @Test
    @Timeout(10)
    void consultarUsuarioByNombre_cuandoUsuarioExiste() {
        //Datos de entrada
        String nombre = "RODRIGO GARCIA";
        //Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("RODRIGO GARCIA");
        usuario.setEmail("RODRIGOGAR@GMAIL.COM");
        usuarioRepository.save(usuario);

        //Act
        Usuario resultado = usuarioService.consultarUsuarioByNombre(nombre);

        //Assert
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByNombre(nombre);
        assertEquals(usuarioEncontrado, Optional.of(resultado));
    }

    @Test
    @Timeout(10)
    void consultarUsuarioByNombre_cuandoUsuarioNoExiste_deberiaDevolverNull() {
        //Datos de entrada
        String nombre = "LUCIA BENITEZ";
        //Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("LUCIA SERRANO");
        usuario.setEmail("SERRANO@GMAIL.COM");
        usuarioRepository.save(usuario);

        //Act
        Usuario resultado = usuarioService.consultarUsuarioByNombre(nombre);

        //Assert
        assertNull(resultado); //Busco a Lucia Benitez cuando la que he insertado es Lucia Serrano

    }

    @Test
    @Timeout(10)
    void eliminarUsuarioById() {
        //Datos de entrada

        //Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("RODRIGO PEREZ");
        usuario.setEmail("RODRIGOPEREZ@GMAIL.COM");
        usuarioRepository.save(usuario);

        //Act
        usuarioService.eliminarUsuarioById(usuario.getId());

        //Assert
        assertNull(usuarioService.consultarUsuarioByNombre(usuario.getNombre())); //La consulta es null porque se ha eliminado

    }

}


