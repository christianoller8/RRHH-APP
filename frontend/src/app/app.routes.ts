import { Routes } from '@angular/router';
import { MainLayout } from './core/layout/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout, // el layout es el "marco" de las rutas hijas
    children: [
      { path: '', redirectTo: 'empleados', pathMatch: 'full' },
      {
        path: 'empleados',
        // loadChildren: el código de empleados se descarga solo al navegar aquí,
        // no en el bundle inicial (lazy loading)
        loadChildren: () =>
          import('./features/empleados/empleados.routes').then((m) => m.EMPLEADOS_ROUTES),
      },
    ],
  },
  // { path: 'login', ... }  → irá aquí, FUERA del layout (fase 4)
  { path: '**', redirectTo: '' },
];
