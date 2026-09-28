package com.empresa.rrhh.empleado;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record EmpleadoDTO(
        Long id,
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        String puesto,
        LocalDate fechaAlta,
        Long departamentoId,
        String departamentoNombre
) {}
