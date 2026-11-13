-- EquipAndes. Ejecutar UNA vez sobre un esquema nuevo después del primer arranque dev.
-- Hibernate crea las cinco tablas e identidades; no se introducen IDs manuales.
-- Los casos CP asumen IDs por orden de inserción en un esquema limpio.
INSERT INTO categorias (nombre, descripcion, estado, fecha_creacion) VALUES ('Sonido', 'Parlantes, consolas y micrófonos', 1, CURRENT_TIMESTAMP);
INSERT INTO categorias (nombre, descripcion, estado, fecha_creacion) VALUES ('Proyección', 'Proyectores, ecran y pantallas', 1, CURRENT_TIMESTAMP);
INSERT INTO categorias (nombre, descripcion, estado, fecha_creacion) VALUES ('Iluminación', 'Luces y efectos', 1, CURRENT_TIMESTAMP);
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-001', 'Parlante activo de 15 pulgadas', 80.00, 10, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Sonido';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-002', 'Consola de mezcla de 12 canales', 120.00, 4, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Sonido';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-003', 'Micrófono inalámbrico', 35.00, 20, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Sonido';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-004', 'Proyector de 5000 lúmenes', 150.00, 0, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Proyección';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-005', 'Ecran trípode de 100 pulgadas', 40.00, 6, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Proyección';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-006', 'Pantalla LED de 55 pulgadas', 110.00, 3, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Proyección';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-007', 'Cabeza móvil LED', 90.00, 8, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Iluminación';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-008', 'Máquina de humo', 50.00, 0, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Iluminación';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-009', 'Reflector PAR LED RGB', 25.00, 16, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Iluminación';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-010', 'Subwoofer activo de 18 pulgadas', 100.00, 5, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Sonido';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-011', 'Barra LED de escenario', 45.00, 12, 1, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Iluminación';
INSERT INTO equipos (codigo,nombre,tarifa_diaria,unidades_disponibles,estado,categoria_id,fecha_creacion) SELECT 'EQP-012', 'Consola analógica de respaldo', 60.00, 2, 0, id, CURRENT_TIMESTAMP FROM categorias WHERE nombre='Sonido';
INSERT INTO clientes (codigo,documento,nombre,email,telefono,tipo,estado,fecha_creacion) VALUES ('CLI-001','20601112223','Eventos Misti SAC','contacto@eventosmisti.pe','959000000','EMPRESA',1,CURRENT_TIMESTAMP);
INSERT INTO clientes (codigo,documento,nombre,email,telefono,tipo,estado,fecha_creacion) VALUES ('CLI-002','20455566677','Hotel Plaza Sur','eventos@plazasur.pe','959000000','EMPRESA',1,CURRENT_TIMESTAMP);
INSERT INTO clientes (codigo,documento,nombre,email,telefono,tipo,estado,fecha_creacion) VALUES ('CLI-003','42315678','Carlos Huanca Rojas','carlos.huanca@gmail.com','959000000','PERSONA',1,CURRENT_TIMESTAMP);
INSERT INTO clientes (codigo,documento,nombre,email,telefono,tipo,estado,fecha_creacion) VALUES ('CLI-004','20509988776','Producciones Sur EIRL','ventas@prodsur.pe','959000000','EMPRESA',0,CURRENT_TIMESTAMP);
INSERT INTO clientes (codigo,documento,nombre,email,telefono,tipo,estado,fecha_creacion) VALUES ('CLI-005','45678901','Ana Quispe Mamani','ana.quispe@example.com','959000000','PERSONA',1,CURRENT_TIMESTAMP);
INSERT INTO clientes (codigo,documento,nombre,email,telefono,tipo,estado,fecha_creacion) VALUES ('CLI-006','20609999888','Convenciones Andes SAC','contacto@convandes.example','959000000','EMPRESA',1,CURRENT_TIMESTAMP);
COMMIT;
