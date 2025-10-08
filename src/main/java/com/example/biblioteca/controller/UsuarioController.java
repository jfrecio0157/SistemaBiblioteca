package com.example.biblioteca.controller;

import com.example.biblioteca.dto.UsuarioDTO;
import com.example.biblioteca.expection.OperacionNoPermitidaException;
import com.example.biblioteca.expection.UsuarioNoEncontradoException;
import com.example.biblioteca.serviceDTO.UsuarioDTOService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Usuarios")
public class UsuarioController {
    private UsuarioDTOService usuarioDTOService;

    public UsuarioController(UsuarioDTOService usuarioDTOService) {
        this.usuarioDTOService = usuarioDTOService;
    }

    //Consulta usuario sin pasarle el nombre
    @GetMapping("")
    public ResponseEntity<String> nombreNoProporcionado() {
        return ResponseEntity.badRequest().body("Debe proporcionar un nombre del usuario en la URL");

    }

    //Consutar usuario por nombre
    @GetMapping("/{nombre}")
    public UsuarioDTO consultarUsuario(@PathVariable String nombre) {
        return usuarioDTOService.consultarUsuarioDTO(nombre)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No existe el usuario: '" + nombre.trim() + "'"));
    }


    //Alta usuario
    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody UsuarioDTO dto) {
        var usuarioCreado = usuarioDTOService.guardarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    //Actualizar un usuario dado su nombre
    @PutMapping("/{nombre}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable String nombre, @RequestBody UsuarioDTO dto) {
        var actualizado = usuarioDTOService.actualizarUsuario(nombre, dto);
        return ResponseEntity.ok(actualizado);
    }

    //Eliminar un libro dado su id
    @DeleteMapping("/{nombre}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable String nombre) {
        usuarioDTOService.eliminarUsuario(nombre);
        return ResponseEntity.ok("Usuario '" + nombre + "' eliminado correctamente.");

    }

    //Body para el Postman en el post y put
    /*
    {
        "nombre": "Antonio Fernandez",
        "email": "antonio123@gmail.com",
        "prestamosIds": []
    }
    */
}

