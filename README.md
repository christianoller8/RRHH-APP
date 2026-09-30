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
| **Nombres de archivo según el estilo 2025**, con `.service` conservado en los servicios | Es la guía oficial vigente. Conservar `.service` evita que el servicio `Empleado` choque con el modelo `Empleado`. | Los tutoriales antiguos usan `.component.ts`; hay que traducir los nombres. |
| **Frontend sin `zone.js` (`--zoneless`) y con SCSS** | Angular repinta según los `signal()`. SCSS es imprescindible para el tema de Material 3. | Alguna librería de terceros puede asumir `zone.js`. *(pendiente de verificar con Material)* |
| **Angular Material 3 en el frontend**, con `provideAnimationsAsync()` | Componentes accesibles y un sistema de temas basado en variables CSS. `mat-sidenav` y otros componentes necesitan un proveedor de animaciones o fallan con error `NG05105`. | Aprender su sistema de theming además de Angular. |
| **Pantallas de empleados con componentes de Material** (`mat-table`, `mat-form-field`, `mat-select`), no HTML plano | Consistente con el layout de la fase 1; se reutiliza en departamentos y vacaciones. | Más superficie de API de Material que aprender de una vez. |
| **Java 25 en vez de 21** | Es la LTS más reciente (soporte hasta 2033), y así no hace falta migrar más adelante. | Ninguno relevante para este proyecto: se verificó que Boot 4.1.1 e Hibernate 7.4.5 compilan y arrancan igual que en 21. |
| **Proyecto de aprendizaje** | Cada pieza de código se explica (qué es y por qué) antes de escribirla. | Más lento, pero es el objetivo. |

> **Dependencias entre features:** `empleado` depende de `departamento` (un empleado pertenece a un departamento), pero `departamento` no debe depender de `empleado`. Si dos features se necesitan mutuamente, es señal de que el diseño está mal cortado.

---

## 2. Requisitos previos

| Herramienta | Versión recomendada | Comprobar con |
|---|---|---|
| Java JDK | 25 | `java -version` |
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
    ├── styles.scss
    └── app/
        ├── app.ts                           ← componente raíz (clase App)
        ├── app.html
        ├── app.config.ts
        ├── app.routes.ts
        ├── core/                            ← global, se usa una vez
        │   ├── layout/main-layout/          ← toolbar + sidenav de Material
        │   ├── services/auth.service.ts
        │   ├── guards/auth-guard.ts
        │   └── interceptors/jwt-interceptor.ts
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
| Java | 25 |
| Dependencies | Spring Web, Spring Data JPA, H2 Database, Validation, Lombok |

Descarga el zip, descomprímelo dentro de `rrhh-app/` y renombra la carpeta a `backend`.

**Opción B — Terminal:**

```bash
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=4.1.1 \
  -d javaVersion=25 \
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
ng new frontend --routing --style=scss --ssr=false --skip-git --zoneless --ai-config=none --file-name-style-guide=2025
cd frontend
```

| Opción | Por qué |
|---|---|
| `--style=scss` | Angular Material 3 define su tema con Sass (`@use '@angular/material' as mat`). Con `css` no podrías usarlo. |
| `--skip-git` | Sin esto, `ng new` ejecuta `git init` dentro de `frontend/` y creas un repositorio dentro de otro. |
| `--zoneless` | La app no usa `zone.js` (la librería que vigila el navegador para saber cuándo repintar). Angular repinta según los `signal()`, que es lo que usan los ejemplos. |
| `--ai-config=none` | No genera archivos de configuración para asistentes de IA. |
| `--file-name-style-guide=2025` | Estilo vigente: `empleado-list.ts` y clase `EmpleadoList`, sin el sufijo `.component`. |
| `--ssr=false` | Sin renderizado en servidor. |

### 5.2 Crear la estructura con Angular CLI

```bash
# Core
ng generate service core/services/auth --type=service
ng generate guard core/guards/auth --functional
ng generate interceptor core/interceptors/jwt --functional
ng generate component core/layout/main-layout

# Features
ng generate component features/auth/login
ng generate component features/dashboard
ng generate component features/empleados/empleado-list
ng generate component features/empleados/empleado-form
ng generate component features/empleados/empleado-detail
ng generate service features/empleados/empleado --type=service
ng generate component features/departamentos/departamento-list
ng generate service features/departamentos/departamento --type=service
ng generate component features/vacaciones/solicitar-vacaciones
ng generate component features/vacaciones/mis-solicitudes
ng generate component features/vacaciones/aprobar-solicitudes
ng generate service features/vacaciones/vacaciones --type=service
```

Cómo se nombra cada cosa con el estilo 2025 (comprobado con `ng generate --dry-run` en Angular CLI 22):

| Tipo | Archivo | Clase |
|---|---|---|
| Componente | `empleado-list.ts` | `EmpleadoList` |
| Servicio | `empleado.service.ts` | `EmpleadoService` |
| Guard | `auth-guard.ts` | lo que genere el CLI |
| Interceptor | `jwt-interceptor.ts` | lo que genere el CLI |
| Modelo (interfaz) | `empleado.model.ts` | `Empleado` |

> Por defecto, el estilo 2025 también quita el sufijo a los servicios (`empleado.ts`, clase `Empleado`), lo que chocaría con el modelo `Empleado`. Por eso los servicios se generan con `--type=service`, que conserva `.service`.

Crea a mano la carpeta de modelos:

```bash
mkdir -p src/app/shared/models
```

### 5.3 Proxy para hablar con el backend

**El problema (CORS).** Un *origen* es protocolo + host + puerto. Angular vive en `localhost:4200` y Spring en `localhost:8080`: son orígenes distintos, y el navegador bloquea que el JavaScript de uno lea las respuestas del otro salvo que el servidor lo autorice con cabeceras CORS.

**La solución en desarrollo.** `ng serve` actúa de intermediario: recibe `localhost:4200/api/...` y lo reenvía a `localhost:8080/api/...`. Para el navegador todo viene del mismo origen. En producción lo hará un servidor como nginx.

Crea `frontend/proxy.conf.json` (JSON no admite comentarios, por eso la explicación está aquí):

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

- `"/api"`: la regla se aplica a toda URL que empiece por `/api`.
- `"target"`: adónde se reenvía (el backend).
- `"secure": false`: no valida certificados TLS del destino. Con un `target` en `http://` no tiene efecto; solo importaría si fuera `https://` con un certificado autofirmado.

Y en `angular.json`, dentro de `projects → frontend → architect → serve`, añade `options` justo debajo de `builder` (en Angular 22 el bloque `serve` solo trae `configurations`):

```json
"serve": {
  "builder": "@angular/build:dev-server",
  "options": {
    "proxyConfig": "proxy.conf.json"
  },
  "configurations": { ... }
}
```

**Cómo comprobar que funciona.** Con backend y `ng serve` arrancados, pide la misma URL a los dos puertos. Si el proxy actúa, ambas devuelven el mismo JSON de Spring (todavía sin endpoint, un 404 con `"path":"/api/empleados"`):

```bash
curl http://localhost:8080/api/empleados   # directo a Spring
curl http://localhost:4200/api/empleados   # a través del proxy de Angular
```

Una ruta que no empieza por `/api` (`curl http://localhost:4200/empleados`) la sigue sirviendo Angular con su `index.html`.

### 5.4 Activar HttpClient: `src/app/app.config.ts`

`provideHttpClient()` registra el servicio `HttpClient` en la inyección de dependencias. Sin él, cualquier servicio que haga `inject(HttpClient)` falla con `NullInjectorError`.

```typescript
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient() // registra HttpClient para poder inyectarlo en los servicios
  ]
};
```

> Al crear el proyecto con `--zoneless`, el CLI no añadió ningún proveedor de detección de cambios ni `zone.js` (ni como dependencia, ni en `angular.json`). En Angular 22 no hace falta declarar nada para trabajar sin `zone.js`.

### 5.5 Instalar Angular Material 3

```bash
ng add @angular/material --skip-confirmation --defaults
```

`ng add` no es un `npm install` normal: es un *schematic*, un script que además de instalar el paquete **modifica tu proyecto**. Con `--defaults` toma las opciones por defecto sin preguntar: tema *Azure/Blue*, tipografía Roboto y densidad estándar. Tocó tres archivos:

- **`package.json`**: añade `@angular/material` y `@angular/cdk` (el *Component Dev Kit*, la capa de accesibilidad y comportamiento sobre la que se construye Material).
- **`src/index.html`**: añade la fuente Roboto y los iconos de Material Symbols desde Google Fonts.
- **`src/styles.scss`**: añade el tema con el mixin `mat.theme`, que genera variables CSS (`--mat-sys-primary`, `--mat-sys-surface`...) usadas por todos los componentes.

```scss
@use '@angular/material' as mat;

html {
  height: 100%;
  @include mat.theme(
    (
      color: (
        primary: mat.$azure-palette,
        tertiary: mat.$blue-palette,
      ),
      typography: Roboto,
      density: 0,
    )
  );
}

body {
  color-scheme: light;
  background-color: var(--mat-sys-surface);
  color: var(--mat-sys-on-surface);
  font: var(--mat-sys-body-medium);
  margin: 0;
  height: 100%;
}
```

> **`--defaults` no instala `@angular/animations`.** Varios componentes de Material (por ejemplo `mat-sidenav`, que usaremos en el layout) llevan animaciones con "escucha sintética" y **fallan con el error `NG05105`** si no hay un proveedor de animaciones configurado; no es solo un problema estético. Instálalo y regístralo:
>
> ```bash
> npm install @angular/animations
> ```
>
> ```typescript
> import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
>
> export const appConfig: ApplicationConfig = {
>   providers: [
>     provideBrowserGlobalErrorListeners(),
>     provideRouter(routes),
>     provideHttpClient(),
>     provideAnimationsAsync() // varios componentes de Material (ej. mat-sidenav) fallan sin esto
>   ]
> };
> ```
>
> `provideAnimationsAsync()` carga el módulo de animaciones de forma perezosa (aparece como *lazy chunk* en `ng build`, no en el paquete principal), en vez de `provideAnimations()`, que lo carga siempre de entrada.

### 5.6 Layout base: toolbar y sidenav

```bash
ng generate component core/layout/main-layout
```

Piezas de Material que usa el layout, cada una en su propio módulo (por eso hay que importarlas todas en `imports`):

| Módulo | Componente | Para qué |
|---|---|---|
| `MatToolbarModule` | `<mat-toolbar>` | La barra superior. |
| `MatSidenavModule` | `<mat-sidenav-container>`, `<mat-sidenav>`, `<mat-sidenav-content>` | El esqueleto del layout: contenedor, menú lateral y el resto del contenido. |
| `MatListModule` | `<mat-nav-list>`, `a[mat-list-item]` | La lista de enlaces de navegación dentro del menú. |
| `MatIconModule` | `<mat-icon>` | Iconos de Material Symbols (instalados en el paso anterior). |
| `MatButtonModule` | `button[mat-icon-button]` | El botón que abre y cierra el menú. |

`main-layout.ts`:

```typescript
import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-main-layout',
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
})
export class MainLayout {
  // controla si el menú lateral está abierto o cerrado
  menuAbierto = signal(true);

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }
}
```

`main-layout.html`:

```html
<mat-sidenav-container class="contenedor">
  <!-- menú lateral: mode="side" lo empuja el contenido en vez de taparlo -->
  <mat-sidenav [opened]="menuAbierto()" mode="side" class="menu">
    <mat-nav-list>
      <a mat-list-item routerLink="/empleados" routerLinkActive="activo">
        <mat-icon matListItemIcon>badge</mat-icon>
        <span matListItemTitle>Empleados</span>
      </a>
    </mat-nav-list>
  </mat-sidenav>

  <mat-sidenav-content>
    <mat-toolbar color="primary">
      <button mat-icon-button (click)="alternarMenu()" aria-label="Abrir o cerrar el menú">
        <mat-icon>menu</mat-icon>
      </button>
      <span>RRHH App</span>
    </mat-toolbar>

    <!-- aquí se pintan las rutas hijas: empleados, departamentos... -->
    <main class="contenido">
      <router-outlet />
    </main>
  </mat-sidenav-content>
</mat-sidenav-container>
```

`main-layout.scss`:

```scss
.contenedor {
  height: 100vh;
}

.menu {
  width: 220px;
}

.contenido {
  padding: 1.5rem;
}
```

`app.routes.ts` conecta el layout como ruta padre (la decisión de rutas anidadas del punto 6.2):

```typescript
import { Routes } from '@angular/router';
import { MainLayout } from './core/layout/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout, // el layout es el "marco" de las rutas hijas
    // sin children todavía: el <router-outlet> del layout queda vacío
    // hasta que la fase 2 añada la ruta real de "empleados"
    children: [],
  },
  // { path: 'login', ... }  → irá aquí, FUERA del layout (fase 4)
  { path: '**', redirectTo: '' },
];
```

Y `app.html` queda reducido a:

```html
<router-outlet />
```

> **Verificado:** con esto arrancado, el botón del menú alterna de verdad la clase `mat-drawer-opened` y desplaza el `<mat-sidenav>` con una animación (`transform: translateX(-220px)`), sin el error `NG05105`. La ruta `/empleados` todavía no pinta nada dentro del `<main>`: eso llega en la Fase 2.

---

## 6. Ejemplo completo: módulo de empleados

Este es el patrón que se repite para cada funcionalidad. Una vez hecho empleados, departamentos y vacaciones se construyen igual.

### 6.1 Backend

#### `departamento/Departamento.java`

```java
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

    @ManyToOne                                     // muchos empleados, un departamento
    @JoinColumn(name = "departamento_id")          // columna en "empleados" que guarda la relación
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

// extender JpaRepository<Departamento, Long> ya da save/findById/findAll/deleteById...
// (Long = tipo de la clave primaria). Vacía porque todavía no necesitamos consultas propias.
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
}
```

#### `empleado/EmpleadoDTO.java`

```java
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
```

#### `common/exception/RecursoNoEncontradoException.java`

```java
package com.empresa.rrhh.common.exception;

// excepción propia (en vez de una genérica de Java) para que GlobalExceptionHandler
// pueda distinguirla de cualquier otro error y traducirla siempre a 404
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

> **`curl -d '...'` con tildes falla en Windows con `Invalid UTF-8 middle byte` (verificado).** Git Bash pasa el argumento al `curl.exe` nativo de Windows, que lo reinterpreta con la codificación de la consola y corrompe los caracteres multibyte (í, ñ, á...) antes de que lleguen al backend. El código Java no tiene ningún problema: es un artefacto del terminal. Para probar con tildes desde `curl`, manda el JSON desde un archivo UTF-8 en vez de como argumento:
>
> ```bash
> printf '{"nombre":"Lucía","apellidos":"García López","email":"lucia.garcia@empresa.com","departamentoId":1}' > body.json
> curl -X PUT http://localhost:8080/api/empleados/1 -H "Content-Type: application/json" --data-binary @body.json
> ```
>
> El navegador y Angular no tienen este problema: mandan UTF-8 correctamente. Solo afecta a pruebas manuales con `curl` en Windows.

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

#### `features/empleados/empleado-list/empleado-list.ts`

Piezas de Material que usa esta pantalla: `MatTableModule` (la tabla declarativa), `MatButtonModule`/`MatIconModule` (los botones e iconos de acciones) y `MatProgressSpinnerModule` (el indicador de carga).

```typescript
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmpleadoService } from '../empleado.service';
import { Empleado } from '../../../shared/models/empleado.model';

@Component({
  selector: 'app-empleado-list',
  imports: [
    RouterLink,
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './empleado-list.html',
  styleUrl: './empleado-list.scss',
})
export class EmpleadoList implements OnInit {
  private empleadoService = inject(EmpleadoService);

  empleados = signal<Empleado[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);

  // columnas que pinta mat-table, en el orden en que se muestran
  columnas = ['nombre', 'email', 'puesto', 'departamento', 'fechaAlta', 'acciones'];

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
      },
    });
  }

  darDeBaja(empleado: Empleado): void {
    if (!confirm(`¿Dar de baja a ${empleado.nombre} ${empleado.apellidos}?`)) return;
    this.empleadoService.darDeBaja(empleado.id!).subscribe(() => this.cargar());
  }
}
```

#### `features/empleados/empleado-list/empleado-list.html`

```html
<div class="cabecera">
  <h1>Empleados</h1>
  <a mat-flat-button color="primary" routerLink="/empleados/nuevo">
    <mat-icon>add</mat-icon>
    Nuevo empleado
  </a>
</div>

@if (cargando()) {
  <mat-spinner />
} @else if (error()) {
  <p class="error">{{ error() }}</p>
} @else {
  <table mat-table [dataSource]="empleados()" class="tabla">
    <!-- cada matColumnDef agrupa la cabecera y la celda de UNA columna -->
    <ng-container matColumnDef="nombre">
      <th mat-header-cell *matHeaderCellDef>Nombre</th>
      <td mat-cell *matCellDef="let emp">{{ emp.nombre }} {{ emp.apellidos }}</td>
    </ng-container>

    <ng-container matColumnDef="email">
      <th mat-header-cell *matHeaderCellDef>Email</th>
      <td mat-cell *matCellDef="let emp">{{ emp.email }}</td>
    </ng-container>

    <ng-container matColumnDef="puesto">
      <th mat-header-cell *matHeaderCellDef>Puesto</th>
      <td mat-cell *matCellDef="let emp">{{ emp.puesto }}</td>
    </ng-container>

    <ng-container matColumnDef="departamento">
      <th mat-header-cell *matHeaderCellDef>Departamento</th>
      <td mat-cell *matCellDef="let emp">{{ emp.departamentoNombre }}</td>
    </ng-container>

    <ng-container matColumnDef="fechaAlta">
      <th mat-header-cell *matHeaderCellDef>Fecha de alta</th>
      <td mat-cell *matCellDef="let emp">{{ emp.fechaAlta | date: 'dd/MM/yyyy' }}</td>
    </ng-container>

    <ng-container matColumnDef="acciones">
      <th mat-header-cell *matHeaderCellDef></th>
      <td mat-cell *matCellDef="let emp">
        <a mat-icon-button [routerLink]="['/empleados', emp.id, 'editar']" aria-label="Editar">
          <mat-icon>edit</mat-icon>
        </a>
        <button mat-icon-button (click)="darDeBaja(emp)" aria-label="Dar de baja">
          <mat-icon>person_remove</mat-icon>
        </button>
      </td>
    </ng-container>

    <!-- ensamblan las filas reales a partir de "columnas": orden y contenido -->
    <tr mat-header-row *matHeaderRowDef="columnas"></tr>
    <tr mat-row *matRowDef="let row; columns: columnas"></tr>
  </table>

  @if (empleados().length === 0) {
    <p>No hay empleados</p>
  }
}
```

#### `features/empleados/empleado-list/empleado-list.scss`

```scss
.cabecera {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.tabla {
  width: 100%;
}

.error {
  color: var(--mat-sys-error); // variable del tema, no un color fijo
}
```

#### `features/empleados/empleado-form/empleado-form.ts`

Piezas de Material: `MatFormFieldModule` da el contenedor con la etiqueta flotante, `MatInputModule` conecta un `<input>` normal a ese contenedor (directiva `matInput`) y `MatSelectModule` es el equivalente a un `<select>`.

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { EmpleadoService } from '../empleado.service';
import { Empleado } from '../../../shared/models/empleado.model';

@Component({
  selector: 'app-empleado-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './empleado-form.html',
  styleUrl: './empleado-form.scss',
})
export class EmpleadoForm implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private empleadoService = inject(EmpleadoService);

  id: number | null = null; // null = creando; con valor = editando ese id

  form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    apellidos: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    puesto: [''],
    departamentoId: [1],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.empleadoService.obtener(this.id).subscribe((emp) => this.form.patchValue(emp));
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // fuerza a mostrar los errores de validación
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

#### `features/empleados/empleado-form/empleado-form.html`

```html
<h1>{{ id ? 'Editar empleado' : 'Nuevo empleado' }}</h1>

<form [formGroup]="form" (ngSubmit)="guardar()" class="formulario">
  <mat-form-field>
    <mat-label>Nombre</mat-label>
    <input matInput formControlName="nombre" />
  </mat-form-field>

  <mat-form-field>
    <mat-label>Apellidos</mat-label>
    <input matInput formControlName="apellidos" />
  </mat-form-field>

  <mat-form-field>
    <mat-label>Email</mat-label>
    <input matInput type="email" formControlName="email" />
  </mat-form-field>

  <mat-form-field>
    <mat-label>Puesto</mat-label>
    <input matInput formControlName="puesto" />
  </mat-form-field>

  <!-- fijo por ahora: en la fase 3 se carga desde /api/departamentos -->
  <mat-form-field>
    <mat-label>Departamento</mat-label>
    <mat-select formControlName="departamentoId">
      <mat-option [value]="1">Recursos Humanos</mat-option>
      <mat-option [value]="2">Desarrollo</mat-option>
      <mat-option [value]="3">Administración</mat-option>
    </mat-select>
  </mat-form-field>

  <div class="acciones">
    <button mat-flat-button color="primary" type="submit">Guardar</button>
    <a mat-button routerLink="/empleados">Cancelar</a>
  </div>
</form>
```

`empleado-form.scss`:

```scss
.formulario {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 400px;
}

.acciones {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}
```

> Más adelante, las opciones del desplegable de departamentos se cargarán desde `/api/departamentos` en vez de estar fijas.

#### `features/empleados/empleados.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { EmpleadoList } from './empleado-list/empleado-list';
import { EmpleadoForm } from './empleado-form/empleado-form';

export const EMPLEADOS_ROUTES: Routes = [
  { path: '', component: EmpleadoList },
  { path: 'nuevo', component: EmpleadoForm },
  { path: ':id/editar', component: EmpleadoForm }
];
```

#### `app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { MainLayout } from './core/layout/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,                   // el layout es el "marco" de las rutas hijas
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

Las rutas que cuelgan de `MainLayout` se pintan dentro de su `<router-outlet />`. Las que quedan fuera (el login) se pintan sin toolbar ni sidenav.

#### `app.html`

Sustituye todo el contenido generado por:

```html
<router-outlet />
```

El `app.ts` que genera el CLI ya importa `RouterOutlet` en su array `imports`, así que no hace falta tocarlo.

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

### Fase 1 — Esqueleto ✅
- [x] Crear backend y frontend
- [x] Configurar H2, proxy y HttpClient
- [x] Instalar Angular Material 3 y configurar el tema con el mixin `mat.theme`
- [x] Layout base: toolbar y sidenav con el `router-outlet` dentro (las pantallas de las demás fases se construyen ya sobre este layout)
- [x] Comprobar que ambos arrancan

### Fase 2 — Empleados *(código en esta guía)*
- [x] Entidades `Empleado` y `Departamento`
- [x] CRUD completo en el backend
- [x] Listado y formulario en Angular (con Angular Material) — probado en el navegador: crear, editar y dar de baja funcionan de punta a punta
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
- [ ] En Angular: pantalla de login, `AuthService` que guarda el token, `jwt-interceptor` que lo añade a cada petición y `auth-guard` que protege las rutas

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
- [ ] Revisar el presupuesto de bundle inicial en `angular.json` (superado por Material en `MainLayout` desde la fase 2; hoy es solo un warning, no un error)
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
