import { Component, OnInit, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DepartamentoService } from '../departamento.service';
import { Departamento } from '../../../shared/models/departamento.model';

@Component({
  selector: 'app-departamento-list',
  imports: [MatTableModule, MatProgressSpinnerModule],
  templateUrl: './departamento-list.html',
  styleUrl: './departamento-list.scss',
})
export class DepartamentoList implements OnInit {
  private departamentoService = inject(DepartamentoService);

  departamentos = signal<Departamento[]>([]);
  cargando = signal(true);
  error = signal<string | null>(null);

  columnas = ['nombre', 'totalEmpleados'];

  ngOnInit(): void {
    this.departamentoService.listar().subscribe({
      next: (datos) => {
        this.departamentos.set(datos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se han podido cargar los departamentos');
        this.cargando.set(false);
      },
    });
  }
}
