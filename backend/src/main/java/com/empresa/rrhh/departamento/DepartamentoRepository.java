package com.empresa.rrhh.departamento;

import org.springframework.data.jpa.repository.JpaRepository;

// extender JpaRepository<Departamento, Long> ya da save/findById/findAll/deleteById...
// (Long = tipo de la clave primaria). Vacía porque todavía no necesitamos consultas propias.
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
}
