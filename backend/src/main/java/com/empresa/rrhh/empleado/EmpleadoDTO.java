package com.empresa.rrhh.empleado;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

// record = clase inmutable de solo datos; genera constructor, getters, equals/hashCode y toString.
// Un único DTO para leer y para crear/editar: al crear, el cliente manda "id" y
// "departamentoNombre" mismo cuando no los necesita, y el service los ignora.
// Las anotaciones (@NotBlank, @Email) las dispara @Valid en el controller.
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
