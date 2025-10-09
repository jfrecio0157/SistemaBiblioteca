package com.example.biblioteca;

import com.example.biblioteca.model.*;
import com.example.biblioteca.repository.*;
import com.example.biblioteca.service.AutorService;
import com.example.biblioteca.service.PrestamoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(PrestamoService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
class PrestamoServiceIT {

    @Autowired PrestamoService prestamoService;
    @Autowired PrestamoRepository prestamoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired
    LibroRepository libroRepository;
    @Autowired
    AutorRepository autorRepository;

    @Test
    @Timeout(10)
    void insertarPrestamo() {
        Usuario usuario = new Usuario();
        usuario.setNombre("LUIS FERNANDEZ");
        usuario.setEmail("LUISFER@GMAIL.COM");
        usuarioRepository.save(usuario);

        Autor autor = new Autor("Eduardo el Pescador");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("Hoy de pesca");
        libro.setIsbn("4567");
        libro.setAutores(List.of(autor));
        libroRepository.save(libro);


        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setActivo(true);
        prestamo.setUsuario(usuario);
        prestamo.setMateriales(List.of(libro));

        //Act
        prestamoService.insertarPrestamo(prestamo);

        //Assert
        List<Prestamo> prestamoList = prestamoRepository.findByUsuario_idAndActivo(usuario.getId());
        assertTrue(prestamoList.contains(prestamo));
    }

    @Test
    @Timeout(10)
    void buscarPrestamoByMaterialId () {


        Usuario usuario = new Usuario();
        usuario.setNombre("LUIS FERNANDEZ");
        usuario.setEmail("LUISFER@GMAIL.COM");
        usuarioRepository.save(usuario);

        Autor autor = new Autor("Eduardo el Pescador");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("Hoy de pesca");
        libro.setIsbn("4567");
        libro.setAutores(List.of(autor));
        libroRepository.save(libro);


        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setActivo(true);
        prestamo.setUsuario(usuario);
        prestamo.setMateriales(List.of(libro));
        prestamoRepository.save(prestamo);

        //ACt
        List<Prestamo> resultado = prestamoService.buscarPrestamoByMaterialId(libro.getId());

        //Assert
        assertNotNull(resultado);
        List<Prestamo> prestamoList = prestamoRepository.findMaterialById(libro.getId());
        assertEquals(prestamoList, resultado);
    }

    @Test
    @Timeout(10)
    void buscarPrestamoByUsuarioIdAndActivo () {

        Usuario usuario = new Usuario();
        usuario.setNombre("RAQUEL FERNANDEZ");
        usuario.setEmail("RAQUELFER@GMAIL.COM");
        usuarioRepository.save(usuario);

        Autor autor = new Autor("Joan Manuel Serrat");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("HOY PUEDE SER UN GRAN DIA");
        libro.setIsbn("7264");
        libro.setAutores(List.of(autor));
        libroRepository.save(libro);


        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setActivo(true);
        prestamo.setUsuario(usuario);
        prestamo.setMateriales(List.of(libro));
        prestamoRepository.save(prestamo);

        //ACt
        List<Prestamo> resultado = prestamoService.buscarPrestamoByUsuarioIdAndActivo(usuario.getId());

        //Assert
        assertNotNull(resultado);
        List<Prestamo> prestamoList = prestamoRepository.findByUsuario_idAndActivo(usuario.getId());
        assertEquals(prestamoList, resultado);
    }

    @Test
    @Timeout(10)
    void obtenerMaterialIdsPorPrestamos () {

        Usuario usuario = new Usuario();
        usuario.setNombre("RAQUEL FERNANDEZ");
        usuario.setEmail("RAQUELFER@GMAIL.COM");
        usuarioRepository.save(usuario);

        Autor autor = new Autor("Joan Manuel Serrat");
        autorRepository.save(autor);

        Libro libro1 = new Libro();
        libro1.setTitulo("HOY PUEDE SER UN GRAN DIA");
        libro1.setIsbn("7264");
        libro1.setAutores(List.of(autor));
        libroRepository.save(libro1);


        Libro libro2 = new Libro();
        libro2.setTitulo("NO HAGO OTRA COSA QUE PENSAR EN TI");
        libro2.setIsbn("8426");
        libro2.setAutores(List.of(autor));
        libroRepository.save(libro2);

        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setActivo(true);
        prestamo.setUsuario(usuario);
        prestamo.setMateriales(List.of(libro1, libro2));
        prestamoRepository.save(prestamo);

        //ACt
        List<Integer> resultado = prestamoService.obtenerMaterialIdsPorPrestamos(List.of(prestamo.getId()));

        //Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(libro1.getId()));
        assertTrue(resultado.contains(libro2.getId()));

    }

    @Test
    @Timeout(10)
    void desactivarPrestamoConMaterial () {

        Usuario usuario = new Usuario();
        usuario.setNombre("RAQUEL FERNANDEZ");
        usuario.setEmail("RAQUELFER@GMAIL.COM");
        usuarioRepository.save(usuario);

        Autor autor = new Autor("Joan Manuel Serrat");
        autorRepository.save(autor);

        Libro libro1 = new Libro();
        libro1.setTitulo("HOY PUEDE SER UN GRAN DIA");
        libro1.setIsbn("7264");
        libro1.setAutores(List.of(autor));
        libroRepository.save(libro1);


        Prestamo prestamo = new Prestamo();
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setActivo(true);
        prestamo.setUsuario(usuario);
        prestamo.setMateriales(new ArrayList<>(List.of(libro1))); //Para que la lista sea mutable. Si no
        //podemos un new ArrayList se crearia como inmutable y no se podria cambiar el campo ACtivo de
        //true a false
        prestamoRepository.save(prestamo);

        //ACt
        prestamoService.desactivarPrestamoConMaterial(List.of(prestamo.getId()), libro1.getId());

        //Assert
        assertFalse(prestamo.isActivo());

    }


}


