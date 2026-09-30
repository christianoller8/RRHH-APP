package com.empresa.rrhh.departamento;

import com.empresa.rrhh.common.exception.RecursoNoEncontradoException;
import com.empresa.rrhh.empleado.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final EmpleadoRepository empleadoRepository;

    public List<DepartamentoDTO> listar() {
        return departamentoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public DepartamentoDTO obtener(Long id) {
        return toDTO(buscar(id));
    }

    @Transactional
    public DepartamentoDTO crear(DepartamentoDTO dto) {
        if (departamentoRepository.existsByNombre(dto.nombre())) {
            throw new IllegalArgumentException("Ya existe un departamento con ese nombre");
        }
        Departamento departamento = new Departamento();
        departamento.setNombre(dto.nombre());
        return toDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public DepartamentoDTO actualizar(Long id, DepartamentoDTO dto) {
        Departamento departamento = buscar(id);
        if (departamentoRepository.existsByNombreAndIdNot(dto.nombre(), id)) {
            throw new IllegalArgumentException("Ya existe un departamento con ese nombre");
        }
        departamento.setNombre(dto.nombre());
        return toDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public void eliminar(Long id) {
        buscar(id); // lanza 404 si no existe

        // cuenta TODOS los empleados (activos o no): la FK de la BD no
        // distingue por "activo", así que la comprobación tiene que ser igual de estricta
        long empleados = empleadoRepository.countByDepartamentoId(id);
        if (empleados > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: tiene " + empleados + " empleado(s) asignado(s)");
        }
        departamentoRepository.deleteById(id);
    }

    // ---- métodos auxiliares ----

    private Departamento buscar(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Departamento " + id + " no encontrado"));
    }

    private DepartamentoDTO toDTO(Departamento d) {
        long total = empleadoRepository.countByDepartamentoIdAndActivoTrue(d.getId());
        return new DepartamentoDTO(d.getId(), d.getNombre(), total);
    }
}
