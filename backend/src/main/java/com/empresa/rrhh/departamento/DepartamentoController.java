package com.empresa.rrhh.departamento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @GetMapping
    public List<DepartamentoDTO> listar() {
        return departamentoService.listar();
    }

    @GetMapping("/{id}")
    public DepartamentoDTO obtener(@PathVariable Long id) {
        return departamentoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartamentoDTO crear(@Valid @RequestBody DepartamentoDTO dto) {
        return departamentoService.crear(dto);
    }

    @PutMapping("/{id}")
    public DepartamentoDTO actualizar(@PathVariable Long id, @Valid @RequestBody DepartamentoDTO dto) {
        return departamentoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        departamentoService.eliminar(id);
    }
}
