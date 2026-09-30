import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { EmpleadoService } from '../empleado.service';
import { Empleado } from '../../../shared/models/empleado.model';

@Component({
  selector: 'app-empleado-detail',
  imports: [RouterLink, DatePipe, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './empleado-detail.html',
  styleUrl: './empleado-detail.scss',
})
export class EmpleadoDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private empleadoService = inject(EmpleadoService);

  empleado = signal<Empleado | null>(null);
  cargando = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.empleadoService.obtener(id).subscribe({
      next: (emp) => {
        this.empleado.set(emp);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se ha podido cargar el empleado');
        this.cargando.set(false);
      },
    });
  }
}
