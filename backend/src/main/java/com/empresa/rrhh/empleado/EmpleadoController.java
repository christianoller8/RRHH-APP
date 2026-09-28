package com.empresa.rrhh.empleado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public List<EmpleadoDTO> listar() {
        return empleadoService.listarActivos();
    }

    @GetMapping("/{id}")
    public EmpleadoDTO obtener(@PathVariable Long id) {
        return empleadoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoDTO crear(@Valid @RequestBody EmpleadoDTO dto) {
        return empleadoService.crear(dto);
    }

    @PutMapping("/{id}")
    public EmpleadoDTO actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoDTO dto) {
        return empleadoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Long id) {
        empleadoService.darDeBaja(id);
    }
}
