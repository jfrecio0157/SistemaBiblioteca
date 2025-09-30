package com.example.biblioteca.service;


import com.example.biblioteca.model.MaterialBiblioteca;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.repository.MaterialBibliotecaRepository;
import com.example.biblioteca.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class MaterialBibliotecaService {

    @Autowired
    private MaterialBibliotecaRepository materialBibliotecaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    public MaterialBibliotecaService(MaterialBibliotecaRepository materialBibliotecaRepository, PrestamoRepository prestamoRepository) {
        this.materialBibliotecaRepository = materialBibliotecaRepository;
        this.prestamoRepository = prestamoRepository;
    }


    public List<MaterialBiblioteca> obtenerMaterialesDelPrestamoById(int prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);
        if (prestamo == null) {
            return Collections.emptyList();
        }

        // Forzar la carga de la colección
        prestamo.getMateriales().size(); // accede para inicializar

        return prestamo.getMateriales(); // ahora sí se puede acceder
    }

    public MaterialBiblioteca obtenerMaterialDelPrestamoByTitulo (String titulo){
        return materialBibliotecaRepository.findByTitulo(titulo);
    }

    public void actualizarDisponible(MaterialBiblioteca materialBiblioteca, int disponible){
        materialBiblioteca.setDisponibles(disponible);
        materialBibliotecaRepository.save(materialBiblioteca);
    }
}