# RRHH App · Angular + Spring Boot

Aplicación de Recursos Humanos con **frontend en Angular** y **backend en Java con Spring Boot**. Gestiona empleados, departamentos, solicitudes de vacaciones y autenticación de usuarios.

Esta guía explica cómo crear el proyecto desde cero, cómo se organiza y el orden recomendado para desarrollarlo, con el código completo del módulo de **empleados** como ejemplo de punta a punta.

---

## Índice

1. [Arquitectura](#1-arquitectura)
2. [Requisitos previos](#2-requisitos-previos)
3. [Estructura del repositorio](#3-estructura-del-repositorio)
4. [Crear el backend (Spring Boot)](#4-crear-el-backend-spring-boot)
5. [Crear el frontend (Angular)](#5-crear-el-frontend-angular)
6. [Ejemplo completo: módulo de empleados](#6-ejemplo-completo-módulo-de-empleados)
7. [Arrancar la aplicación](#7-arrancar-la-aplicación)
8. [Hoja de ruta del desarrollo](#8-hoja-de-ruta-del-desarrollo)
9. [API REST](#9-api-rest)
10. [Comandos útiles](#10-comandos-útiles)

---

## 1. Arquitectura

```
┌──────────────────────┐   HTTP / JSON    ┌──────────────────────┐    SQL    ┌──────────────┐
│  Frontend (Angular)  │ ───────────────▶ │ Backend (Spring Boot)│ ────────▶ │ Base de datos│
│  localhost:4200      │ ◀─────────────── │ localhost:8080       │ ◀──────── │ H2 / Postgres│
└──────────────────────┘                  └──────────────────────┘           └──────────────┘
```

- **Angular** pinta las pantallas y hace peticiones a la API.
- **Spring Boot** expone una API REST en `/api/...`, aplica la lógica de negocio y accede a la base de datos.
- En desarrollo usamos **H2** (base de datos en memoria, no hay que instalar nada). Más adelante se cambia a PostgreSQL o MySQL.

Flujo de una petición en el backend:

```
Controller  →  Service  →  Repository  →  Base de datos
(recibe HTTP)  (lógica)    (consultas)
     ▲
     └── devuelve DTOs en JSON
```

### Decisiones de diseño

| Decisión | Por qué | Trade-off |
|---|---|---|
| **Backend organizado por feature** (`empleado/`, `departamento/`...) | Todo lo de una funcionalidad vive junto. Es coherente con `features/` del frontend y escala mejor. | Menos habitual en tutoriales, que agrupan por capa. |
| **Archivos planos dentro de cada feature** | Con ~5 archivos por feature, subcarpetas serían ruido. El nombre de la clase ya indica su rol. | Si una feature crece mucho, habrá que subdividirla. |
| **`common/` para lo compartido** | Lo que usan todas las features (excepciones globales) no pertenece a ninguna. | Riesgo de que `common` se convierta en cajón de sastre: solo entra lo realmente transversal. |
| **Layout como componente de ruta** (`core/layout/main-layout`) | Las pantallas de la app cuelgan como rutas hijas del layout; el login queda fuera y no muestra toolbar ni sidenav. | Hay que entender las rutas anidadas y los `router-outlet` múltiples. |
| **Angular Material 3 en el frontend** | Componentes accesibles y un sistema de temas basado en variables CSS. | Aprender su sistema de theming además de Angular. *(pendiente de configurar)* |
| **Proyecto de aprendizaje** | Cada pieza de código se explica (qué es y por qué) antes de escribirla. | Más lento, pero es el objetivo. |

> **Dependencias entre features:** `empleado` depende de `departamento` (un empleado pertenece a un departamento), pero `departamento` no debe depender de `empleado`. Si dos features se necesitan mutuamente, es señal de que el diseño está mal cortado.

---

## 2. Requisitos previos

| Herramienta | Versión recomendada | Comprobar con |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Node.js | 20 o superior (LTS) | `node -v` |
| Angular CLI | la última | `ng version` |
| Git | cualquiera | `git --version` |
| IDE | IntelliJ IDEA (backend) y VS Code (frontend) | — |

Instalar Angular CLI:

```bash
npm install -g @angular/cli
```

> No hace falta instalar Maven: el proyecto de Spring Boot incluye `mvnw`, un script que lo descarga automáticamente.

---

## 3. Estructura del repositorio

```
rrhh-app/
├── backend/          ← Spring Boot (Java)
├── frontend/         ← Angular
├── .gitignore
└── README.md
```

### Backend

```
backend/
├── pom.xml                                  ← dependencias (como package.json)
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/empresa/rrhh/
    │   │   ├── RrhhApplication.java         ← punto de arranque
    │   │   │
    │   │   │   # Un paquete por feature. Dentro, los archivos van planos.
    │   │   ├── empleado/
    │   │   │   ├── Empleado.java            ← entidad (tabla)
    │   │   │   ├── EmpleadoRepository.java  ← acceso a BD
    │   │   │   ├── EmpleadoService.java     ← lógica de negocio
    │   │   │   ├── EmpleadoController.java  ← endpoints HTTP
    │   │   │   └── EmpleadoDTO.java         ← lo que viaja en JSON
    │   │   ├── departamento/
    │   │   │   ├── Departamento.java
    │   │   │   ├── DepartamentoRepository.java
    │   │   │   ├── DepartamentoService.java
    │   │   │   ├── DepartamentoController.java
    │   │   │   └── DepartamentoDTO.java
    │   │   ├── vacaciones/
    │   │   │   ├── SolicitudVacaciones.java
    │   │   │   ├── SolicitudVacacionesRepository.java
    │   │   │   ├── VacacionesService.java
    │   │   │   ├── VacacionesController.java
    │   │   │   └── SolicitudVacacionesDTO.java
    │   │   ├── auth/                        ← login (fase 4)
    │   │   │   ├── Usuario.java
    │   │   │   ├── UsuarioRepository.java
    │   │   │   ├── AuthService.java
    │   │   │   ├── AuthController.java
    │   │   │   └── LoginRequest.java
    │   │   ├── security/                    ← configuración transversal: JWT y permisos
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── JwtFilter.java
    │   │   └── common/                      ← lo que usan todas las features
    │   │       └── exception/
    │   │           ├── RecursoNoEncontradoException.java
    │   │           └── GlobalExceptionHandler.java
    │   └── resources/
    │       ├── application.properties       ← configuración
    │       └── data.sql                     ← datos de prueba
    └── test/java/com/empresa/rrhh/
```

### Frontend

```
frontend/
├── package.json
├── angular.json
├── proxy.conf.json                          ← redirige /api al backend
└── src/
    ├── main.ts
    ├── index.html
    ├── styles.css
    └── app/
        ├── app.component.ts
        ├── app.config.ts
        ├── app.routes.ts
        ├── core/                            ← global, se usa una vez
        │   ├── layout/main-layout/          ← toolbar + sidenav de Material
        │   ├── services/auth.service.ts
        │   ├── guards/auth.guard.ts
        │   └── interceptors/jwt.interceptor.ts
        ├── shared/                          ← reutilizable en varias features
        │   └── models/
        │       ├── empleado.model.ts
        │       ├── departamento.model.ts
        │       └── solicitud-vacaciones.model.ts
        └── features/                        ← una carpeta por área
            ├── auth/login/
            ├── dashboard/
            ├── empleados/
            │   ├── empleado-list/
            │   ├── empleado-form/
            │   ├── empleado-detail/
            │   ├── empleado.service.ts
            │   └── empleados.routes.ts
            ├── departamentos/
            └── vacaciones/
                ├── solicitar-vacaciones/
                ├── mis-solicitudes/
                └── aprobar-solicitudes/
```

---

## 4. Crear el backend (Spring Boot)

### 4.1 Generar el proyecto

Crea la carpeta raíz:

```bash
mkdir rrhh-app && cd rrhh-app
git init
```

**Opción A — Web:** entra en [start.spring.io](https://start.spring.io) y rellena:

| Campo | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | la última estable (4.1.1 al crear este proyecto; ya no se ofrece la 3.x) |
| Group | `com.empresa` |
| Artifact | `rrhh` |
| Package name | `com.empresa.rrhh` |
| Packaging | Jar |
| Java | 21 |
| Dependencies | Spring Web, Spring Data JPA, H2 Database, Validation, Lombok |

Descarga el zip, descomprímelo dentro de `rrhh-app/` y renombra la carpeta a `backend`.

**Opción B — Terminal:**

```bash
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=4.1.1 \
  -d javaVersion=21 \
  -d groupId=com.empresa \
  -d artifactId=rrhh \
  -d name=rrhh \
  -d packageName=com.empresa.rrhh \
  -d baseDir=backend \
  -d dependencies=web,data-jpa,h2,validation,lombok \
  -o backend.zip

unzip backend.zip && rm backend.zip
```

> **Windows (Git Bash):** el `tar` de Git Bash no lee `.zip` y `unzip` puede no estar instalado. Usa el `tar` de Windows: `/c/Windows/System32/tar.exe -xf backend.zip`.

> **Spring Security** se añade más adelante (fase 4). Si la incluyes desde el principio, bloquea todos los endpoints y complica las primeras pruebas.

### 4.2 Qué hace cada dependencia

| Dependencia | Para qué sirve |
|---|---|
| Spring Web | Crear la API REST y servidor Tomcat integrado |
| Spring Data JPA | Acceder a la BD con clases Java en vez de SQL a mano |
| H2 Database | Base de datos en memoria para desarrollo |
| Validation | Validar datos de entrada (`@NotBlank`, `@Email`...) |
| Lombok | Genera getters, setters y constructores automáticamente |

> **Spring Boot 4 renombró los starters.** En el `pom.xml` generado verás `spring-boot-starter-webmvc` (antes `spring-boot-starter-web`), un módulo separado `spring-boot-h2console` para la consola de H2, y un módulo de test por cada starter (`spring-boot-starter-webmvc-test`...). Es lo mismo con otro empaquetado: Boot 4 modularizó sus librerías.

> **IntelliJ:** activa el procesado de anotaciones para Lombok en *Settings → Build → Compiler → Annotation Processors → Enable*.

### 4.3 Configuración: `src/main/resources/application.properties`

```properties
spring.application.name=rrhh
server.port=8080

# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:rrhhdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true
spring.jpa.open-in-view=false
```

Qué hace cada bloque:

| Propiedad | Qué hace | Por qué |
|---|---|---|
| `spring.datasource.url=jdbc:h2:mem:rrhhdb` | Crea la BD H2 en memoria con el nombre fijo `rrhhdb`. | Sin nombre fijo, Boot genera uno aleatorio en cada arranque (`mem:8f58de5f-...`) y no podrías conectarte desde la consola. |
| `spring.h2.console.*` | Activa una consola web para ver las tablas. | Para inspeccionar los datos mientras aprendes. |
| `spring.jpa.hibernate.ddl-auto=update` | Hibernate crea o modifica las tablas a partir de las `@Entity`. | Cómodo en desarrollo. **En una BD real (fase 7) se cambia por migraciones** (Flyway/Liquibase), porque `update` puede dejar el esquema en un estado inesperado. |
| `spring.jpa.show-sql=true` | Imprime en consola el SQL que genera Hibernate. | Para ver qué hay detrás de `findByActivoTrue()`. |
| `spring.jpa.defer-datasource-initialization=true` | Ejecuta `data.sql` **después** de que Hibernate cree las tablas. | Por defecto `data.sql` corre antes y falla con "tabla no encontrada". |
| `spring.jpa.open-in-view=false` | Cierra la conexión a la BD al salir del service. | Evita consultas "perezosas" disparadas fuera del service (problema N+1) y apaga el aviso del arranque. |

Con esto puedes ver la base de datos en `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:rrhhdb`, usuario `sa`, sin contraseña).

### 4.4 Crear las carpetas (paquetes)

Un paquete por feature, más `common` para lo compartido y `security` para la configuración transversal:

```bash
cd backend/src/main/java/com/empresa/rrhh
mkdir empleado departamento vacaciones auth security
mkdir -p common/exception
cd -
```

> Solo creamos las carpetas de las features que vamos a construir ya (`empleado`, `departamento`, `common`). El resto se crea cuando toque su fase.

---

## 5. Crear el frontend (Angular)

### 5.1 Generar el proyecto

Desde la raíz `rrhh-app/`:

```bash
ng new frontend --routing --style=css --ssr=false
cd frontend
```

### 5.2 Crear la estructura con Angular CLI

```bash
# Core
ng generate service core/services/auth
ng generate guard core/guards/auth --functional
ng generate interceptor core/interceptors/jwt --functional
ng generate component core/layout/main-layout

# Features
ng generate component features/auth/login
ng generate component features/dashboard
ng generate component features/empleados/empleado-list
ng generate component features/empleados/empleado-form
ng generate component features/empleados/empleado-detail
ng generate service features/empleados/empleado
ng generate component features/departamentos/departamento-list
ng generate service features/departamentos/departamento
ng generate component features/vacaciones/solicitar-vacaciones
ng generate component features/vacaciones/mis-solicitudes
ng generate component features/vacaciones/aprobar-solicitudes
ng generate service features/vacaciones/vacaciones
```

> A partir de Angular 20, la CLI genera los ficheros sin el sufijo `.component` (por ejemplo `empleado-list.ts` en vez de `empleado-list.component.ts`). Funciona igual; adapta los nombres de los imports a lo que te genere.

Crea a mano la carpeta de modelos:

```bash
mkdir -p src/app/shared/models
```

### 5.3 Proxy para hablar con el backend

Para evitar problemas de CORS en desarrollo, Angular redirige las peticiones `/api` al backend.

Crea `frontend/proxy.conf.json`:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

Y en `angular.json`, dentro de `projects → frontend → architect → serve`, añade:

```json
"options": {
  "proxyConfig": "proxy.conf.json"
}
```

### 5.4 Activar HttpClient: `src/app/app.config.ts`

```typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

---

## 6. Ejemplo completo: módulo de empleados

Este es el patrón que se repite para cada funcionalidad. Una vez hecho empleados, departamentos y vacaciones se construyen igual.

### 6.1 Backend

#### `departamento/Departamento.java`

```java
package com.empresa.rrhh.departamento;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departamentos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;
}
```

#### `empleado/Empleado.java`

```java
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
```

#### `empleado/EmpleadoRepository.java`

```java
package com.empresa.rrhh.empleado;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Spring genera la consulta a partir del nombre del método
    List<Empleado> findByActivoTrue();

    boolean existsByEmail(String email);

    // Igual que existsByEmail, pero ignora al propio empleado: sirve para editar
    boolean existsByEmailAndIdNot(String email, Long id);
}
```

#### `departamento/DepartamentoRepository.java`

```java
package com.empresa.rrhh.departamento;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
}
```

#### `empleado/EmpleadoDTO.java`

```java
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
```

#### `common/exception/RecursoNoEncontradoException.java`

```java
package com.empresa.rrhh.common.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

#### `common/exception/GlobalExceptionHandler.java`

```java
package com.empresa.rrhh.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Datos no válidos"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }
}
```

#### `empleado/EmpleadoService.java`

```java
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
```

#### `empleado/EmpleadoController.java`

```java
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
```

#### `resources/data.sql` (datos de prueba)

```sql
INSERT INTO departamentos (nombre) VALUES ('Recursos Humanos');
INSERT INTO departamentos (nombre) VALUES ('Desarrollo');
INSERT INTO departamentos (nombre) VALUES ('Administración');

INSERT INTO empleados (nombre, apellidos, email, puesto, fecha_alta, salario, activo, departamento_id)
VALUES ('Lucía', 'García López', 'lucia.garcia@empresa.com', 'Técnica de RRHH', '2022-03-01', 32000, true, 1);

INSERT INTO empleados (nombre, apellidos, email, puesto, fecha_alta, salario, activo, departamento_id)
VALUES ('Javier', 'Martín Ruiz', 'javier.martin@empresa.com', 'Desarrollador Java', '2021-09-15', 38000, true, 2);

INSERT INTO empleados (nombre, apellidos, email, puesto, fecha_alta, salario, activo, departamento_id)
VALUES ('Marta', 'Sánchez Pérez', 'marta.sanchez@empresa.com', 'Administrativa', '2023-01-10', 26000, true, 3);
```

#### Probar el backend

Arranca con `./mvnw spring-boot:run` y abre en el navegador:

```
http://localhost:8080/api/empleados
```

Deberías ver los tres empleados en JSON.

### 6.2 Frontend

#### `shared/models/empleado.model.ts`

```typescript
export interface Empleado {
  id?: number;
  nombre: string;
  apellidos: string;
  email: string;
  puesto?: string;
  fechaAlta?: string;
  departamentoId?: number;
  departamentoNombre?: string;
}
```

#### `features/empleados/empleado.service.ts`

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Empleado } from '../../shared/models/empleado.model';

@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/empleados';   // el proxy lo redirige al backend

  listar(): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.apiUrl}/${id}`);
  }

  crear(empleado: Empleado): Observable<Empleado> {
    return this.http.post<Empleado>(this.apiUrl, empleado);
  }

  actualizar(id: number, empleado: Empleado): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.apiUrl}/${id}`, empleado);
  }

  darDeBaja(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

#### `features/empleados/empleado-list/empleado-list.component.ts`

```typescript
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EmpleadoService } from '../empleado.service';
import { Empleado } from '../../../shared/models/empleado.model';

@Component({
  selector: 'app-empleado-list',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './empleado-list.component.html',
  styleUrl: './empleado-list.component.css'
})
export class EmpleadoListComponent implements OnInit {
  private empleadoService = inject(EmpleadoService);

  empleados = signal<Empleado[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.empleadoService.listar().subscribe({
      next: (datos) => {
        this.empleados.set(datos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se han podido cargar los empleados');
        this.cargando.set(false);
      }
    });
  }

  darDeBaja(empleado: Empleado): void {
    if (!confirm(`¿Dar de baja a ${empleado.nombre} ${empleado.apellidos}?`)) return;
    this.empleadoService.darDeBaja(empleado.id!).subscribe(() => this.cargar());
  }
}
```

#### `features/empleados/empleado-list/empleado-list.component.html`

```html
<div class="cabecera">
  <h1>Empleados</h1>
  <a routerLink="/empleados/nuevo" class="boton">+ Nuevo empleado</a>
</div>

@if (cargando()) {
  <p>Cargando...</p>
} @else if (error()) {
  <p class="error">{{ error() }}</p>
} @else {
  <table>
    <thead>
      <tr>
        <th>Nombre</th>
        <th>Email</th>
        <th>Puesto</th>
        <th>Departamento</th>
        <th>Fecha de alta</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      @for (emp of empleados(); track emp.id) {
        <tr>
          <td>{{ emp.nombre }} {{ emp.apellidos }}</td>
          <td>{{ emp.email }}</td>
          <td>{{ emp.puesto }}</td>
          <td>{{ emp.departamentoNombre }}</td>
          <td>{{ emp.fechaAlta | date: 'dd/MM/yyyy' }}</td>
          <td>
            <a [routerLink]="['/empleados', emp.id, 'editar']">Editar</a>
            <button (click)="darDeBaja(emp)">Baja</button>
          </td>
        </tr>
      } @empty {
        <tr><td colspan="6">No hay empleados</td></tr>
      }
    </tbody>
  </table>
}
```

#### `features/empleados/empleado-list/empleado-list.component.css`

```css
.cabecera { display: flex; justify-content: space-between; align-items: center; }
table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
th, td { padding: 0.6rem; border-bottom: 1px solid #ddd; text-align: left; }
th { background: #f4f4f4; }
.boton { background: #1976d2; color: white; padding: 0.5rem 1rem; border-radius: 4px; text-decoration: none; }
.error { color: #c62828; }
td button { margin-left: 0.5rem; }
```

#### `features/empleados/empleado-form/empleado-form.component.ts`

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmpleadoService } from '../empleado.service';
import { Empleado } from '../../../shared/models/empleado.model';

@Component({
  selector: 'app-empleado-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './empleado-form.component.html'
})
export class EmpleadoFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private empleadoService = inject(EmpleadoService);

  id: number | null = null;

  form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    apellidos: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    puesto: [''],
    departamentoId: [1]
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.empleadoService.obtener(this.id).subscribe(emp => this.form.patchValue(emp));
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const datos: Empleado = this.form.getRawValue();
    const peticion = this.id
      ? this.empleadoService.actualizar(this.id, datos)
      : this.empleadoService.crear(datos);

    peticion.subscribe(() => this.router.navigate(['/empleados']));
  }
}
```

#### `features/empleados/empleado-form/empleado-form.component.html`

```html
<h1>{{ id ? 'Editar empleado' : 'Nuevo empleado' }}</h1>

<form [formGroup]="form" (ngSubmit)="guardar()">
  <label>Nombre <input formControlName="nombre" /></label>
  <label>Apellidos <input formControlName="apellidos" /></label>
  <label>Email <input formControlName="email" type="email" /></label>
  <label>Puesto <input formControlName="puesto" /></label>
  <label>
    Departamento
    <select formControlName="departamentoId">
      <option [value]="1">Recursos Humanos</option>
      <option [value]="2">Desarrollo</option>
      <option [value]="3">Administración</option>
    </select>
  </label>

  <button type="submit">Guardar</button>
  <a routerLink="/empleados">Cancelar</a>
</form>
```

> Más adelante, las opciones del desplegable de departamentos se cargarán desde `/api/departamentos` en vez de estar fijas.

#### `features/empleados/empleados.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { EmpleadoListComponent } from './empleado-list/empleado-list.component';
import { EmpleadoFormComponent } from './empleado-form/empleado-form.component';

export const EMPLEADOS_ROUTES: Routes = [
  { path: '', component: EmpleadoListComponent },
  { path: 'nuevo', component: EmpleadoFormComponent },
  { path: ':id/editar', component: EmpleadoFormComponent }
];
```

#### `app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,          // el layout es el "marco" de las rutas hijas
    children: [
      { path: '', redirectTo: 'empleados', pathMatch: 'full' },
      {
        path: 'empleados',
        loadChildren: () =>
          import('./features/empleados/empleados.routes').then(m => m.EMPLEADOS_ROUTES)
      }
    ]
  },
  // { path: 'login', ... }  → irá aquí, FUERA del layout (fase 4)
  { path: '**', redirectTo: '' }
];
```

Las rutas que cuelgan de `MainLayoutComponent` se pintan dentro de su `<router-outlet />`. Las que quedan fuera (el login) se pintan sin toolbar ni sidenav.

#### `app.component.html`

Sustituye todo el contenido generado por:

```html
<router-outlet />
```

Y en `app.component.ts` importa `RouterOutlet` en el array `imports`.

> El contenido de `main-layout` (toolbar y sidenav de Material, con su propio `<router-outlet />`) lo construimos en la Fase 1.

---

## 7. Arrancar la aplicación

Necesitas **dos terminales**:

**Terminal 1 — Backend**

```bash
cd backend
./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
```

> **Error `PKIX path building failed` al descargar dependencias (Windows + antivirus).** Algunos antivirus (por ejemplo Norton) descifran el HTTPS y lo vuelven a firmar con su propia CA. Windows confía en ella, pero Java usa su propio almacén (`cacerts`) y la rechaza. Solución sin tocar el JDK: decirle a Maven que use el almacén de Windows, solo para ese comando.
>
> ```bash
> MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT" ./mvnw spring-boot:run
> ```
>
> No lo pongas en `.mvn/jvm.config`: `Windows-ROOT` no existe en Linux/Mac y rompería el proyecto para otros. Solo hace falta cuando Maven descarga algo nuevo; con las dependencias ya en `~/.m2` no se necesita.

**Terminal 2 — Frontend**

```bash
cd frontend
npm install                   # solo la primera vez
ng serve
```

| Qué | URL |
|---|---|
| Aplicación Angular | http://localhost:4200 |
| API REST | http://localhost:8080/api/empleados |
| Consola de la BD | http://localhost:8080/h2-console |

---

## 8. Hoja de ruta del desarrollo

Orden recomendado. Cada fase deja algo funcionando antes de pasar a la siguiente.

### Fase 1 — Esqueleto *(código en esta guía)*
- [ ] Crear backend y frontend
- [ ] Configurar H2, proxy y HttpClient
- [ ] Instalar Angular Material 3 y configurar el tema con el mixin `mat.theme`
- [ ] Layout base: toolbar y sidenav vacíos con el `router-outlet` dentro (las pantallas de las demás fases se construyen ya sobre este layout)
- [ ] Comprobar que ambos arrancan

### Fase 2 — Empleados *(código en esta guía)*
- [ ] Entidades `Empleado` y `Departamento`
- [ ] CRUD completo en el backend
- [ ] Listado y formulario en Angular
- [ ] Pantalla de detalle (`empleado-detail`)

### Fase 3 — Departamentos
- [ ] `DepartamentoService` y `DepartamentoController` (`GET`, `POST`, `PUT`, `DELETE`)
- [ ] Listado de departamentos en Angular
- [ ] Cargar el desplegable del formulario de empleados desde la API
- [ ] Mostrar cuántos empleados tiene cada departamento

### Fase 4 — Autenticación y roles
- [ ] Añadir dependencia `spring-boot-starter-security` y una librería JWT (por ejemplo `jjwt`)
- [ ] Entidad `Usuario` con roles: `ADMIN`, `RRHH`, `EMPLEADO`
- [ ] `AuthController` con `POST /api/auth/login` que devuelve un token
- [ ] `SecurityConfig` y `JwtFilter` para proteger las rutas `/api/**`
- [ ] En Angular: pantalla de login, `AuthService` que guarda el token, `jwt.interceptor` que lo añade a cada petición y `auth.guard` que protege las rutas

### Fase 5 — Vacaciones
- [ ] Entidad `SolicitudVacaciones` (empleado, fecha inicio, fecha fin, estado: `PENDIENTE`, `APROBADA`, `RECHAZADA`)
- [ ] Reglas de negocio en el service: no solapar fechas, no superar los días disponibles
- [ ] Pantalla para solicitar vacaciones y ver las propias
- [ ] Pantalla para que RRHH apruebe o rechace

### Fase 6 — Dashboard
- [ ] Endpoint de estadísticas (empleados por departamento, vacaciones pendientes...)
- [ ] Pantalla de inicio con tarjetas y gráficas

### Fase 7 — Calidad y producción
- [ ] Tests del backend (JUnit + Mockito) y del frontend
- [ ] Cambiar H2 por PostgreSQL o MySQL
- [ ] Paginación y filtros en los listados
- [ ] Dockerizar backend y frontend
- [ ] Desplegar

---

## 9. API REST

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/empleados` | Lista empleados activos |
| `GET` | `/api/empleados/{id}` | Obtiene un empleado |
| `POST` | `/api/empleados` | Crea un empleado |
| `PUT` | `/api/empleados/{id}` | Actualiza un empleado |
| `DELETE` | `/api/empleados/{id}` | Da de baja (baja lógica) |
| `GET` | `/api/departamentos` | Lista departamentos *(fase 3)* |
| `POST` | `/api/auth/login` | Inicia sesión y devuelve JWT *(fase 4)* |
| `GET` | `/api/vacaciones/mias` | Solicitudes del usuario *(fase 5)* |
| `POST` | `/api/vacaciones` | Crea una solicitud *(fase 5)* |
| `PUT` | `/api/vacaciones/{id}/aprobar` | Aprueba una solicitud *(fase 5)* |
| `PUT` | `/api/vacaciones/{id}/rechazar` | Rechaza una solicitud *(fase 5)* |

---

## 10. Comandos útiles

### Backend

| Comando | Qué hace |
|---|---|
| `./mvnw spring-boot:run` | Arranca la aplicación |
| `./mvnw test` | Ejecuta los tests |
| `./mvnw package` | Genera el `.jar` en `target/` |
| `java -jar target/rrhh-0.0.1-SNAPSHOT.jar` | Ejecuta el `.jar` generado |
| `./mvnw clean` | Borra lo compilado |

### Frontend

| Comando | Qué hace |
|---|---|
| `ng serve` | Arranca en modo desarrollo |
| `ng generate component ruta/nombre` | Crea un componente |
| `ng generate service ruta/nombre` | Crea un servicio |
| `ng test` | Ejecuta los tests |
| `ng build` | Genera la versión de producción en `dist/` |

### `.gitignore` en la raíz

```gitignore
# Backend
backend/target/
backend/.idea/
*.iml

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.angular/

# Sistema
.DS_Store
.vscode/
```

---

## Equivalencias Angular ↔ Spring Boot

Para orientarse si vienes de uno de los dos lados:

| Angular | Spring Boot |
|---|---|
| `npm` | Maven (`mvnw`) |
| `package.json` | `pom.xml` |
| `node_modules/` | `~/.m2/repository` (global) |
| `ng new` | start.spring.io |
| `ng serve` (puerto 4200) | `./mvnw spring-boot:run` (puerto 8080) |
| `ng build` → `dist/` | `./mvnw package` → `target/*.jar` |
| `main.ts` | `RrhhApplication.java` |
| `environment.ts` | `application.properties` |
| `@Injectable` + `inject()` | `@Service` + inyección por constructor |
| Interfaz en `shared/models/` | `record` `*DTO` dentro de cada feature |
