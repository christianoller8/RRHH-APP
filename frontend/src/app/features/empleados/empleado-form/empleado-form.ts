import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { EmpleadoService } from '../empleado.service';
import { Empleado } from '../../../shared/models/empleado.model';
import { DepartamentoService } from '../../departamentos/departamento.service';
import { Departamento } from '../../../shared/models/departamento.model';

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
  private departamentoService = inject(DepartamentoService);

  id: number | null = null; // null = creando; con valor = editando ese id
  departamentos = signal<Departamento[]>([]);

  form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    apellidos: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    puesto: [''],
    // sin id fijo por defecto: ya no podemos asumir que el departamento 1 existe
    departamentoId: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.departamentoService.listar().subscribe((datos) => this.departamentos.set(datos));

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
    const valores = this.form.getRawValue();
    // el "!" es seguro aquí: this.form.invalid ya comprobó arriba que
    // departamentoId cumple Validators.required, así que no puede ser null
    const datos: Empleado = { ...valores, departamentoId: valores.departamentoId! };
    const peticion = this.id
      ? this.empleadoService.actualizar(this.id, datos)
      : this.empleadoService.crear(datos);

    peticion.subscribe(() => this.router.navigate(['/empleados']));
  }
}
