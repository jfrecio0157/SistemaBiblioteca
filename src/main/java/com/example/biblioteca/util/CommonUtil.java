package com.example.biblioteca.util;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
//Se define la clase como final y el constructor como private para evitar que la clase CommonUtil se pueda instanciar
public final class CommonUtil {
    public CommonUtil() {
    }

    public int leerEntero() {
        final Scanner scanner = new Scanner(System.in);

        while (!scanner.hasNextInt()) {
            System.out.println("Por favor, introduce un número válido:");
            scanner.next();
        }
        int valor = scanner.nextInt();
        scanner.nextLine(); // limpiar salto de línea
        return valor;
    }

    public String leerEntrada(){
        final Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim().toUpperCase();
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarMensajeError (String contexto, Exception e){
        System.out.printf("Error inesperado en %s: %s%n ",contexto,e.getMessage());
    }


}
