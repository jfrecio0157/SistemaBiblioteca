package com.example.biblioteca.service;
import com.example.biblioteca.dto.LibroDTO;
import com.example.biblioteca.dto.UsuarioDTO;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.PrestamoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PrestamoRepository prestamoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository
                          ) {
        this.usuarioRepository=usuarioRepository;
    }

    public void insertarUsuario (Usuario usuario){
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario consultarUsuarioByNombre(String nombre) {
        Optional<Usuario> usuario = usuarioRepository.findByNombre(nombre);
        return usuario.orElse(null);
    }

    public void eliminarUsuarioById (int id){
        usuarioRepository.deleteById(id);
    }

    //******************************************
    //Métodos para APIs Rest
    //******************************************
    /**
     * Convierte una entidad {@link Usuario} en un objeto {@link UsuarioDTO} para su uso en la capa de presentación.
     *
     * Este método extrae los datos relevantes del Usuario, incluyendo su ID, nombre, eamil,
     * membresía asociada y prestamos asociados, y los empaqueta en un DTO que puede ser
     * fácilmente serializado como JSON en una API REST.
     *
     * @param usuario la entidad {@link Usuario} que se desea convertir.
     * @return un objeto {@link UsuarioDTO} que representa los datos del usuario de forma simplificada.
     */

    public UsuarioDTO convertirAUsuarioDTO (Usuario usuario){
        Objects.requireNonNull(usuario, "El usuario no puede ser nulo");

        var listaPrestamosId = (usuario.getPrestamos() == null ? List.<Prestamo>of() : usuario.getPrestamos())
                .stream()
                .filter(Objects::nonNull)
                .map(Prestamo::getId)
                .toList();

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                listaPrestamosId
        );
    }

    public Usuario convertirAUsuario(UsuarioDTO dto){
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPrestamos(prestamoRepository.findAllById(dto.getPrestamosIds()));

        return usuario;

    }

    //Para hacer el Get desde Postman - Consultar usuario por nombre
    @Transactional
    public Optional<UsuarioDTO> consultarUsuarioDTO(String nombre){
        Objects.requireNonNull(nombre, "El nombre del usuario a consultar no puede ser nulo o vacío");

        String nombreValido = validarNombre(nombre);

        return usuarioRepository.findByNombre(nombreValido).stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(this::convertirAUsuarioDTO)
                ;
    }

    private String validarNombre (String nombre){
        if (nombre == null || nombre.isEmpty()){
            return null;
        }
        return nombre.trim();
    }

    //Para hacer el Post desde Postman. - Guardar usuario
    public UsuarioDTO guardarUsuario(UsuarioDTO dto){
        var usuario = convertirAUsuario(dto);
        var usuarioGuardado = usuarioRepository.save(usuario);
        return convertirAUsuarioDTO(usuarioGuardado);
    }
}

