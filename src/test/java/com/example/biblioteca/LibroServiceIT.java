package com.example.biblioteca;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.model.Libro;
import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.repository.LibroRepository;
import com.example.biblioteca.service.AutorService;
import com.example.biblioteca.service.LibroService;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(LibroService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
class LibroServiceIT {

    @Autowired LibroService libroService;
    @Autowired LibroRepository libroRepository;
    @Autowired AutorRepository autorRepository;
    //@Autowired Validator validator;

    @MockBean
    Validator validator;

    @Test
    @Timeout(10)
    void insertarLibro_cuandoNoHayViolaciones() {
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Rudyard Kipling");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("El libro de la selva");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1234");
        libro.setAñoPublicacion(1964);
        libro.setTotales(10);
        libro.setDisponibles(10);

        //Act
        libroService.insertarLibro(libro);

        //Assert
        assertThat(libroRepository.findByTitulo("El libro de la selva")).isPresent();
    }

    @Test
    @Timeout(10)
    @Rollback(value = true) //Se permite el rollback tras la violación.
    void insertarLibro_cuandoHayViolaciones() {
        //Arrange

        //El autor es obligatorio. Salta violacion
        Libro libro = new Libro();
        libro.setAutores(List.of()); //Lista vacia de autores
        libro.setTitulo("Libro desconocido");
        libro.setIsbn("1246");
        libro.setAñoPublicacion(2000);
        libro.setTotales(7);
        libro.setDisponibles(7);

        long before = autorRepository.count();

        //Act
        assertThatThrownBy(() -> libroService.insertarLibro(libro))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Un libro debe tener al menos un autor");

        //Assert
        //assertThat(autorRepository.count()).isEqualTo(before);
        //assertThat(libroRepository.findByTitulo("Libro desconocido")).isEmpty();

    }

    @Test
    @Timeout(10)
    void listarLibro_cuandoHayLibros () {
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Julio Verne");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("La vuelta al mundo en 80 días");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1578");
        libro.setAñoPublicacion(1972);
        libro.setTotales(10);
        libro.setDisponibles(10);
        libroRepository.save(libro);

        //Act
        List<Libro> resultado = libroService.listarLibros();
        //Assert
        assertNotNull(resultado);
        assertEquals(1,resultado.size());
        assertEquals("La vuelta al mundo en 80 días",resultado.get(0).getTitulo());
        assertEquals("Julio Verne",resultado.get(0).getAutores().get(0).getNombre());
        assertEquals("1578",resultado.get(0).getIsbn());
        assertEquals(1972,resultado.get(0).getAñoPublicacion());
        assertEquals(10,resultado.get(0).getTotales());
        assertEquals(10,resultado.get(0).getDisponibles());

    }

    @Test
    @Timeout(10)
    void buscarLibroByIsbn_cuandoExisteLibro () {
        //Datos de entrada
        String isbn = "1361";
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Julio Verne");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("Viaje al centro de la tierra");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1361");
        libro.setAñoPublicacion(1972);
        libro.setTotales(10);
        libro.setDisponibles(10);
        libroRepository.save(libro);

        //Act
        Libro resultado = libroService.buscarLibroByIsbn(isbn);

        //Assert
        assertNotNull(resultado);
        assertEquals("Viaje al centro de la tierra",resultado.getTitulo());
        assertEquals("Julio Verne", resultado.getAutores().get(0).getNombre());
        assertEquals("1361",resultado.getIsbn());
        assertEquals(1972,resultado.getAñoPublicacion());
        assertEquals(10,resultado.getTotales());
        assertEquals(10,resultado.getDisponibles());
    }

    @Test
    @Timeout(10)
    void buscarLibroByIsbn_cuandoNoExisteLibro_deberiaDevolverNull () {
        //Datos de entrada
        String isbn = "1234";
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Julio Verne");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("Viaje a Marte");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1578");
        libro.setAñoPublicacion(1972);
        libro.setTotales(10);
        libro.setDisponibles(10);
        libroRepository.save(libro);

        //Act
        Libro resultado = libroService.buscarLibroByIsbn(isbn);

        //Assert
        assertNull(resultado); //Busco un isbn 1234 y el libro tiene 1578
    }

    @Test
    @Timeout(10)
    void buscarLibroByTitulo_cuandoExisteLibro () {
        //Datos de entrada
        String titulo = "La vuelta al mundo en 80 días";
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Julio Verne");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("La vuelta al mundo en 80 días");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1578");
        libro.setAñoPublicacion(1972);
        libro.setTotales(10);
        libro.setDisponibles(10);
        libroRepository.save(libro);

        //Act
        Libro resultado = libroService.buscarLibroByTitulo(titulo);

        //Assert
        assertNotNull(resultado);
        assertEquals("La vuelta al mundo en 80 días",resultado.getTitulo());
        assertEquals("Julio Verne", resultado.getAutores().get(0).getNombre());
        assertEquals("1578",resultado.getIsbn());
        assertEquals(1972,resultado.getAñoPublicacion());
        assertEquals(10,resultado.getTotales());
        assertEquals(10,resultado.getDisponibles());
    }

    @Test
    @Timeout(10)
    void buscarLibroByTitulo_cuandoNoExisteLibro_deberiaDevolverNull () {
        //Datos de entrada
        String titulo = "El pollo Pepe";
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Julio Verne");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("La vuelta al mundo en 80 días");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1578");
        libro.setAñoPublicacion(1972);
        libro.setTotales(10);
        libro.setDisponibles(10);
        libroRepository.save(libro);

        //Act
        Libro resultado = libroService.buscarLibroByTitulo(titulo);

        //Assert
        assertNull(resultado);

    }

    @Test
    @Timeout(10)
    void eliminarLibroById_cuandoExisteLibro () {
        //Datos de entrada
        String isbn = "1578";
        //Arrange
        Autor autor = new Autor();
        autor.setNombre("Julio Verne");
        autorRepository.save(autor);

        Libro libro = new Libro();
        libro.setTitulo("La vuelta al mundo en 80 días");
        libro.setAutores(List.of(autor));
        libro.setIsbn("1578");
        libro.setAñoPublicacion(1972);
        libro.setTotales(10);
        libro.setDisponibles(10);
        libroRepository.save(libro);

        //Act
        libroService.eliminarLibroById(isbn);

        //Assert
        Libro libroBuscado  = libroService.buscarLibroByIsbn("1578");
        assertNull(libroBuscado);
        assertFalse(libroRepository.existsById("1578")); //El id es el Isbn
    }
}


