package com.empresa.rrhh.empleado;

import com.empresa.rrhh.departamento.Departamento;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String email;

    private String puesto;

    private LocalDate fechaAlta;

    private BigDecimal salario;          // no se expone en el DTO

    private boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;
}
