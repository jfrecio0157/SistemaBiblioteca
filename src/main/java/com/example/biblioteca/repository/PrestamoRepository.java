package com.example.biblioteca.repository;

import com.example.biblioteca.model.Prestamo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository <Prestamo, Integer> {
    List<Prestamo> findByUsuario_Id (Integer usuario_id);

    @Query("SELECT p FROM Prestamo p JOIN p.materiales m WHERE m.id = :materialId AND p.activo = true")
    List<Prestamo> findMaterialById(@Param("materialId") int materialId);

    @Query("SELECT p FROM Prestamo p JOIN p.materiales m WHERE p.usuario.id = :usuarioId AND p.activo = true")
    Prestamo findMaterialByUsuarioId(@Param("usuarioId") int usuarioId);

    @Query("SELECT p FROM Prestamo p WHERE p.usuario.id = :usuario_id and p.activo = true")
    List<Prestamo> findByUsuario_idAndActivo (@Param("usuario_id") Integer usuario_id);

    @Query(value = "SELECT material_id FROM PrestamoMaterial WHERE prestamo_id = :prestamoId", nativeQuery = true)
    Integer findMaterialIdsByPrestamoIds(@Param("prestamoId") Integer prestamoId);

    @Query("SELECT DISTINCT p FROM Prestamo p JOIN FETCH p.materiales WHERE p.id IN :ids")
    List<Prestamo> findAllWithMaterialesById(@Param("ids") List<Integer> ids);



}
