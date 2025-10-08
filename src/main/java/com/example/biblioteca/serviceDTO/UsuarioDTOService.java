package com.example.biblioteca.serviceDTO;

import com.example.biblioteca.dto.UsuarioDTO;
import com.example.biblioteca.expection.OperacionNoPermitidaException;
import com.example.biblioteca.expection.UsuarioNoEncontradoException;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.PrestamoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UsuarioDTOService {
    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PrestamoRepository prestamoRepository;

    public UsuarioDTOService(UsuarioRepository usuarioRepository
                          ) {
        this.usuarioRepository=usuarioRepository;
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
                usuario.getNombre().toUpperCase(),
                usuario.getEmail().toUpperCase(),
                listaPrestamosId
        );
    }

    public Usuario convertirAUsuario(UsuarioDTO dto){
        Usuario usuario = new Usuario();
        //El id es la clave autogenerada
        //usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre().toUpperCase());
        usuario.setEmail(dto.getEmail().toUpperCase());
        usuario.setPrestamos(prestamoRepository.findAllById(dto.getPrestamosIds()));

        return usuario;

    }

    //Para hacer el Get desde Postman - Consultar usuario por nombre
    @Transactional
    public Optional<UsuarioDTO> consultarUsuarioDTO(String nombre){
        String nombreValido = validarNombre(nombre);

        return usuarioRepository.findByNombre(nombreValido).stream()
                .findFirst()
                .map(this::convertirAUsuarioDTO)
                ;
    }

    //Se comprueba que el nombre no esta a nulos o vacío.
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

    //Para hacer el Put desde Postman. Actualizar un Usuario
    public UsuarioDTO actualizarUsuario(String nombre, UsuarioDTO dto) {

        //Se busca el usuario
        Usuario usuario = usuarioRepository.findByNombre(nombre.toUpperCase())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con nombre: " + nombre));

        //El parámetro nombre se usa para buscar el usuario en la BBDD
        //El nombre que se mete en el body se usa para actualizar
        usuario.setNombre(dto.getNombre().toUpperCase());
        usuario.setEmail(dto.getEmail().toUpperCase());

        List<Prestamo> prestamoList = new ArrayList<>();

        if (!dto.getPrestamosIds().isEmpty()) {
            for (Integer prestamoId : dto.getPrestamosIds()) {
                Prestamo prestamo = prestamoRepository.getReferenceById(prestamoId);
                prestamoList.add(prestamo);
            }
        }
        usuario.setPrestamos(prestamoList);

        return convertirAUsuarioDTO(usuarioRepository.save(usuario));
    }

    //Para hacer el Delete desde Postman. Borrar un usuario dado su nombbre
    public void eliminarUsuario(String nombre) {
        Optional<Usuario> usuario = usuarioRepository.findByNombre(nombre.toUpperCase());

        //Spring detecta que esta excepción no fue capturada en el controlador.
        //Busca un manejador en las clases anotadas con @ControllerAdvice.-> que esta en ManejadorGlobalDeErrores
        if (usuario.isEmpty()){
            throw new UsuarioNoEncontradoException("Usuario no encontrado con nombre: '" + nombre + "'");
        }

        //Comprobar que no tiene préstamos activos
        List<Prestamo> prestamoList = prestamoRepository.findByUsuario_idAndActivo(usuario.get().getId());
        if (!(prestamoList == null || prestamoList.isEmpty())){
            throw new OperacionNoPermitidaException("Usuario '" + nombre + "' no se puede dar de baja. Tiene préstamos activos");
        }
        usuarioRepository.deleteById(usuario.get().getId());

    }
}

