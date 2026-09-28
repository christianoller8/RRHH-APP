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
