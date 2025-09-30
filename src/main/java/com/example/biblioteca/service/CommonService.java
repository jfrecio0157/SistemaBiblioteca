package com.example.biblioteca.service;

import com.example.biblioteca.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Scanner;

@Service
@Transactional
public class CommonService {

    @Autowired
    private final CommonUtil commonUtil;

    public CommonService(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
    }

    public boolean preguntarSiBorrar(){
        commonUtil.mostrarMensaje("¿Estas seguro (S/N): ?");
        String respuesta = commonUtil.leerEntrada();

        return ("S").equals(respuesta);
    }

    public void mostrarError(){
        commonUtil.mostrarMensaje("Opción no permitida. Intenta de nuevo");
    }

    public void volverMenuPrincipal() {
        commonUtil.mostrarMensaje("Volviendo al menú principal...");
    }

}