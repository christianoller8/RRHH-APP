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
