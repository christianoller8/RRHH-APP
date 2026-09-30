import { Routes } from '@angular/router';
import { EmpleadoList } from './empleado-list/empleado-list';
import { EmpleadoForm } from './empleado-form/empleado-form';
import { EmpleadoDetail } from './empleado-detail/empleado-detail';

// el router prueba las rutas en orden: "nuevo" (literal) debe ir ANTES que ":id"
// (dinámica), si no, "/empleados/nuevo" se interpretaría como id="nuevo"
export const EMPLEADOS_ROUTES: Routes = [
  { path: '', component: EmpleadoList },
  { path: 'nuevo', component: EmpleadoForm },
  { path: ':id/editar', component: EmpleadoForm },
  { path: ':id', component: EmpleadoDetail },
];
