import { Routes } from '@angular/router';
import { EmpleadoList } from './empleado-list/empleado-list';
import { EmpleadoForm } from './empleado-form/empleado-form';

export const EMPLEADOS_ROUTES: Routes = [
  { path: '', component: EmpleadoList },
  { path: 'nuevo', component: EmpleadoForm },
  { path: ':id/editar', component: EmpleadoForm },
];
