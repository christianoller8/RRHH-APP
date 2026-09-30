package com.empresa.rrhh.departamento;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    boolean existsByNombre(String nombre);

    // igual que existsByNombre, pero ignora al propio departamento: sirve para editar
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
