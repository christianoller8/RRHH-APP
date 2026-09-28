package com.empresa.rrhh.empleado;

import com.empresa.rrhh.common.exception.RecursoNoEncontradoException;
import com.empresa.rrhh.departamento.Departamento;
import com.empresa.rrhh.departamento.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor   // inyección de dependencias por constructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final DepartamentoRepository departamentoRepository;

    public List<EmpleadoDTO> listarActivos() {
        return empleadoRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public EmpleadoDTO obtener(Long id) {
        return toDTO(buscar(id));
    }

    @Transactional
    public EmpleadoDTO crear(EmpleadoDTO dto) {
        if (empleadoRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Ya existe un empleado con ese email");
        }
        Empleado empleado = new Empleado();
        copiarDatos(dto, empleado);
        empleado.setFechaAlta(dto.fechaAlta() != null ? dto.fechaAlta() : LocalDate.now());
        return toDTO(empleadoRepository.save(empleado));
    }

    @Transactional
    public EmpleadoDTO actualizar(Long id, EmpleadoDTO dto) {
        Empleado empleado = buscar(id);
        if (empleadoRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new IllegalArgumentException("Ya existe un empleado con ese email");
        }
        copiarDatos(dto, empleado);
        return toDTO(empleadoRepository.save(empleado));
    }

    @Transactional
    public void darDeBaja(Long id) {
        Empleado empleado = buscar(id);
        empleado.setActivo(false);     // baja lógica: no se borra el registro
    }

    // ---- métodos auxiliares ----

    private Empleado buscar(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado " + id + " no encontrado"));
    }

    private void copiarDatos(EmpleadoDTO dto, Empleado empleado) {
        empleado.setNombre(dto.nombre());
        empleado.setApellidos(dto.apellidos());
        empleado.setEmail(dto.email());
        empleado.setPuesto(dto.puesto());
        if (dto.departamentoId() != null) {
            Departamento dep = departamentoRepository.findById(dto.departamentoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Departamento no encontrado"));
            empleado.setDepartamento(dep);
        }
    }

    private EmpleadoDTO toDTO(Empleado e) {
        return new EmpleadoDTO(
                e.getId(),
                e.getNombre(),
                e.getApellidos(),
                e.getEmail(),
                e.getPuesto(),
                e.getFechaAlta(),
                e.getDepartamento() != null ? e.getDepartamento().getId() : null,
                e.getDepartamento() != null ? e.getDepartamento().getNombre() : null
        );
    }
}
