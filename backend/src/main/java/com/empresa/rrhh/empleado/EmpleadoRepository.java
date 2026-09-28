package com.empresa.rrhh.empleado;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Spring genera la consulta a partir del nombre del método
    List<Empleado> findByActivoTrue();

    boolean existsByEmail(String email);

    // Igual que existsByEmail, pero ignora al propio empleado: sirve para editar
    boolean existsByEmailAndIdNot(String email, Long id);
}
