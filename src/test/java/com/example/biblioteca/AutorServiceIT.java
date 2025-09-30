package com.example.biblioteca;

import com.example.biblioteca.model.Autor;
import com.example.biblioteca.repository.AutorRepository;
import com.example.biblioteca.service.AutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AutorService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
class AutorServiceIT {

    @Autowired AutorService autorService;
    @Autowired AutorRepository autorRepository;

    @Test
    @Timeout(10)
    void insertarAutor_persiste_en_mariadb_local() {
        autorService.insertarAutor("Ada Lovelace");
        assertThat(autorRepository.findByNombre("Ada Lovelace")).isPresent();
    }

    @Test
    @Timeout(10)
    void buscarAutorByNombre_cuandoAutorExiste_devolverAutor() {
        //Arrange
        //Insertar un autor
        autorService.insertarAutor("Federico Garcia Lorca");
        //Act
        //LLamar al proceso
        Autor resultado = autorService.buscarAutorByNombre("Federico Garcia Lorca");

        //Asset
        //Verificar salida
        assertNotNull(resultado);
        assertEquals("Federico Garcia Lorca",resultado.getNombre());

    }

    @Test
    @Timeout(10)
    void buscarAutorByNombre_cuandoAutorNoExiste_devolverNull() {
        //Arrange
        //No se inserta un autor

        //Act
        //LLamar al proceso
        Autor resultado = autorService.buscarAutorByNombre("Autor inexistente");

        //Asset
        //Verificar salida
        assertNull(resultado);

    }

    @Test
    @Timeout(10)
    void eliminarAutorById_cuandoIdExiste_seElimina () {
        //Arrange: Insertar el autor y buscar su id.
        autorService.insertarAutor("Pepe Grillo");
        //Se llama a buscarAutorByNombre y se recupera el id del autor.
        int id = autorRepository.findByNombre("Pepe Grillo").orElseThrow().getId();

        //Act: Eliminar by id
        autorService.eliminarAutorById(id);

        //Asset: ya no existe
        assertThat(autorRepository.findById(id)).isEmpty();
        assertThat(autorRepository.findByNombre("Pepe Grillo")).isEmpty();

    }

    @Test
    @Timeout(10)
    void eliminarAutorById_cuandoIdNoExiste_deberiaEjecutaNingunaAccion () {
        // Arrange: contar antes
        long before = autorRepository.count();

        // Act & Assert: no lanza y el conteo no cambia
        assertThatCode(() -> autorService.eliminarAutorById(999_999))
                .doesNotThrowAnyException();

        assertThat(autorRepository.count()).isEqualTo(before);

    }

    @Test
    @Timeout(10)
    void eliminarAutorById_cuandoSeLanzaDosVeces_noHaceNadaLaSegundaVez () {
        //Arrange: Insertar el autor y buscar su id.
        autorService.insertarAutor("Pepe Grillo");
        //Se llama a buscarAutorByNombre y se recupera el id del autor.
        int id = autorRepository.findByNombre("Pepe Grillo").orElseThrow().getId();

        //Act: Eliminar by id
        autorService.eliminarAutorById(id);

        //Se intenta eliminar de nuevo
        assertThatCode(() -> autorService.eliminarAutorById(id))
                .doesNotThrowAnyException();

        //Asset: ya no existe
        assertThat(autorRepository.findById(id)).isEmpty();
        assertThat(autorRepository.findByNombre("Pepe Grillo")).isEmpty();

    }
}


