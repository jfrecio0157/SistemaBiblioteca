package com.example.biblioteca;

import com.example.biblioteca.model.Revista;
import com.example.biblioteca.repository.RevistaRepository;
import com.example.biblioteca.service.RevistaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(RevistaService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
class RevistaServiceIT {

    @Autowired RevistaService revistaService;
    @Autowired RevistaRepository revistaRepository;


    @Test
    @Timeout(10)
    void insertarRevista() {
        //Arrange
        Revista revista = new Revista();
        revista.setPeriodicidad("Mensual");
        revista.setTitulo("Federados");
        revista.setNumeroEdicion(1);
        revista.setTotales(10);
        revista.setDisponibles(10);

        //Act
        revistaService.insertarRevista(revista);

        //Assert
        assertThat(revistaRepository.findByTitulo("Federados")).isPresent();
    }

    @Test
    @Timeout(10)
    void buscarRevistaByTitulo_cuandoRevistaExiste() {
        //Datos de entrada
        String titulo = "NBA";
        //Arrange
        Revista revista = new Revista();
        revista.setPeriodicidad("Mensual");
        revista.setTitulo("NBA");
        revista.setNumeroEdicion(1);
        revista.setTotales(10);
        revista.setDisponibles(10);
        revistaRepository.save(revista);

        //Act
        Revista resultado = revistaService.buscarRevistaByTitulo(titulo);

        //Assert
        assertNotNull(resultado);
        assertThat(revistaRepository.findByTitulo("NBA")).isPresent();
        assertEquals("NBA", resultado.getTitulo());
        assertEquals("Mensual", resultado.getPeriodicidad());
        assertEquals(1,resultado.getNumeroEdicion());
        assertEquals(10, resultado.getTotales());
        assertEquals(10, resultado.getDisponibles());
    }

    @Test
    @Timeout(10)
    void buscarRevistaByTitulo_cuandoRevistaNoExiste_deberiaDevolverNull() {
        //Datos de entrada
        String titulo = "Cosas de jardín";
        //Arrange
        Revista revista = new Revista();
        revista.setPeriodicidad("Mensual");
        revista.setTitulo("NBA");
        revista.setNumeroEdicion(1);
        revista.setTotales(10);
        revista.setDisponibles(10);
        revistaRepository.save(revista);

        //Act
        Revista resultado = revistaService.buscarRevistaByTitulo(titulo);

        //Assert
        assertNull(resultado);
    }

    @Test
    @Timeout(10)
    void eliminarRevistaById_cuandoRevistaExiste() {
        //Datos de entrada
        String titulo = "ACB";
        //Arrange
        Revista revista = new Revista();
        revista.setPeriodicidad("Mensual");
        revista.setTitulo("ACB");
        revista.setNumeroEdicion(1);
        revista.setTotales(10);
        revista.setDisponibles(10);
        revistaRepository.save(revista);

        //Act
        revistaService.eliminarRevistaById(revista.getId());

        //Assert
        assertThat(revistaRepository.findByTitulo(titulo)).isEmpty();

    }

    @Test
    @Timeout(10)
    void listaRevistas_cuandoExistenDosRevista () {
        //Arrange
        Revista revista1 = new Revista();
        revista1.setPeriodicidad("Mensual");
        revista1.setTitulo("Baloncesto");
        revista1.setNumeroEdicion(1);
        revista1.setTotales(10);
        revista1.setDisponibles(10);
        revistaRepository.save(revista1);

        Revista revista2 = new Revista();
        revista2.setPeriodicidad("Mensual");
        revista2.setTitulo("Cosas de aquí");
        revista2.setNumeroEdicion(1);
        revista2.setTotales(10);
        revista2.setDisponibles(10);
        revistaRepository.save(revista2);

        //Act
        List<Revista> resultado = revistaService.listarRevistas();

        //Assert
        assertEquals(2, resultado.size());
        assertEquals("Baloncesto",resultado.get(0).getTitulo());
        assertEquals("Cosas de aquí",resultado.get(1).getTitulo());

    }

}


