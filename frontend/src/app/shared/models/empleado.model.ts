// forma de un empleado tal como lo devuelve/espera la API (EmpleadoDTO del backend)
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
