package com.empresa.rrhh.departamento;

import jakarta.validation.constraints.NotBlank;

// totalEmpleados es un campo calculado: no se guarda en la tabla,
// el service lo rellena al listar contando empleados activos.
// Long (no "long"): si el cliente no lo manda, Jackson necesita poder
// ponerle null; un primitivo nunca admite null y el parseo fallaría.
public record DepartamentoDTO(
        Long id,
        @NotBlank String nombre,
        Long totalEmpleados
) {}
