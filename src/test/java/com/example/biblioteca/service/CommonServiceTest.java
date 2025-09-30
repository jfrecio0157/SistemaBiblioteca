package com.example.biblioteca.service;

import com.example.biblioteca.util.CommonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CommonServiceTest {
    private CommonUtil commonUtil;
    private CommonService commonService;

    @BeforeEach
    void setUp () {
        commonUtil = mock(CommonUtil.class);
        commonService = new CommonService(commonUtil);
    }

    @Test
    void preguntarSiBorrar_Afirmativo() {
        String respuesta = "S";

        when(commonUtil.leerEntrada()).thenReturn(respuesta);

        //Ejecutamos el proceso
        boolean resultado = commonService.preguntarSiBorrar();

        //Assert
        assertTrue(resultado);

        // Assert: interacciones y orden
        InOrder inOrder = inOrder(commonUtil);
        inOrder.verify(commonUtil).mostrarMensaje("¿Estas seguro (S/N): ?");
        inOrder.verify(commonUtil).leerEntrada();
    }

    @Test
    void preguntarSiBorrar_Negativo() {
        String respuesta = "N";

        when(commonUtil.leerEntrada()).thenReturn(respuesta);

        //Ejecutamos el proceso
        boolean resultado = commonService.preguntarSiBorrar();

        //Assert
        assertFalse(resultado);

        // Assert: interacciones y orden
        InOrder inOrder = inOrder(commonUtil);
        inOrder.verify(commonUtil).mostrarMensaje("¿Estas seguro (S/N): ?");
        inOrder.verify(commonUtil).leerEntrada();
    }

    @Test
    void mostrarError () {
        //Ejecutamos el proceso
        commonService.mostrarError();

        //Assert
        verify(commonUtil).mostrarMensaje("Opción no permitida. Intenta de nuevo");
    }

    @Test
    void volverMenuPrincipal () {
        //Ejecutamos el proceso
        commonService.volverMenuPrincipal();

        //Assert
        verify(commonUtil).mostrarMensaje("Volviendo al menú principal...");
    }
}
