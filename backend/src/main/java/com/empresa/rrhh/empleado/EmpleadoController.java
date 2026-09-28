package com.empresa.rrhh.empleado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                        // @Controller + @ResponseBody: cada retorno se serializa a JSON
@RequestMapping("/api/empleados")      // prefijo común para todos los métodos de la clase
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping                        // GET /api/empleados
    public List<EmpleadoDTO> listar() {
        return empleadoService.listarActivos();
    }

    @GetMapping("/{id}")               // GET /api/empleados/5
    public EmpleadoDTO obtener(@PathVariable Long id) {    // {id} de la URL -> parámetro id
        return empleadoService.obtener(id);
    }

    @PostMapping                                   // POST /api/empleados
    @ResponseStatus(HttpStatus.CREATED)            // 201: se creó un recurso nuevo
    public EmpleadoDTO crear(@Valid @RequestBody EmpleadoDTO dto) {
        // @RequestBody: convierte el JSON del body en EmpleadoDTO
        // @Valid: dispara las anotaciones del DTO (@NotBlank, @Email); sin esto no harían nada
        return empleadoService.crear(dto);
    }

    @PutMapping("/{id}")               // PUT /api/empleados/5
    public EmpleadoDTO actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoDTO dto) {
        return empleadoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")                        // DELETE /api/empleados/5
    @ResponseStatus(HttpStatus.NO_CONTENT)         // 204: éxito, sin nada que devolver en el body
    public void darDeBaja(@PathVariable Long id) {
        empleadoService.darDeBaja(id);
    }
}
