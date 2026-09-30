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
