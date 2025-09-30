package com.example.biblioteca.service;

import com.example.biblioteca.model.*;
import com.example.biblioteca.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    public PrestamoService(PrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }


    public void insertarPrestamo (Prestamo prestamo){
        prestamoRepository.save(prestamo);
    }

    public List<Prestamo> buscarPrestamoByMaterialId (int id){
        return prestamoRepository.findMaterialById(id);
    }

    public List<Prestamo> buscarPrestamoByUsuarioIdAndActivo (int usuario_id){
        return prestamoRepository.findByUsuario_idAndActivo(usuario_id);
    }

    public List<Integer> obtenerMaterialIdsPorPrestamos(List<Integer> prestamoIds) {
        List<Prestamo> prestamos = prestamoRepository.findAllById(prestamoIds);

        return prestamos.stream()
                .flatMap(prestamo -> prestamo.getMateriales().stream())
                .map(MaterialBiblioteca::getId)
                .distinct()
                .collect(Collectors.toList());
    }


    public void desactivarPrestamoConMaterial(List<Integer> prestamoIds, int materialId) {
        List<Prestamo> prestamos = prestamoRepository.findAllWithMaterialesById(prestamoIds);

        prestamos.stream()
                .filter(prestamo -> prestamo.getMateriales().stream()
                        .anyMatch(material -> material.getId() == materialId))
                .findFirst()
                .ifPresent(prestamo -> {
                    prestamo.setActivo(false);
                    prestamoRepository.save(prestamo);
                });
    }

}