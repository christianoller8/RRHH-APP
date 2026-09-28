INSERT INTO departamentos (nombre) VALUES ('Recursos Humanos');
INSERT INTO departamentos (nombre) VALUES ('Desarrollo');
INSERT INTO departamentos (nombre) VALUES ('Administración');

INSERT INTO empleados (nombre, apellidos, email, puesto, fecha_alta, salario, activo, departamento_id)
VALUES ('Lucía', 'García López', 'lucia.garcia@empresa.com', 'Técnica de RRHH', '2022-03-01', 32000, true, 1);

INSERT INTO empleados (nombre, apellidos, email, puesto, fecha_alta, salario, activo, departamento_id)
VALUES ('Javier', 'Martín Ruiz', 'javier.martin@empresa.com', 'Desarrollador Java', '2021-09-15', 38000, true, 2);

INSERT INTO empleados (nombre, apellidos, email, puesto, fecha_alta, salario, activo, departamento_id)
VALUES ('Marta', 'Sánchez Pérez', 'marta.sanchez@empresa.com', 'Administrativa', '2023-01-10', 26000, true, 3);
