package com.empresa.rrhh.departamento;

import jakarta.persistence.*;
import lombok.*;

@Entity                                    // esta clase se guarda como tabla
@Table(name = "departamentos")             // nombre de la tabla en la BD
@Getter @Setter                            // Lombok genera get/set en tiempo de compilación
@NoArgsConstructor @AllArgsConstructor     // constructor vacío (lo exige JPA) y con todos los campos
public class Departamento {

    @Id                                                 // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // la BD autoincrementa el id
    private Long id;

    @Column(nullable = false, unique = true)   // no puede ser null ni repetirse
    private String nombre;
}
