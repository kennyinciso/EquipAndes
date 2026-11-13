# EquipAndes · Alquileres API

Adaptación del proyecto `InventarioBackend` (base PharmaBackend) al examen parcial Unidad 1, versión D: alquiler de equipos para eventos. Proyecto Maven, Spring Boot 4.0.7 y Java 21, preparado para IntelliJ IDEA.

## Abrir y ejecutar en IntelliJ IDEA

1. Descarga o clona la rama `feature/equipandes-inciso` y abre su archivo `pom.xml` como proyecto Maven.
2. En **File → Project Structure → Project SDK**, selecciona un **JDK 21**. También selecciona ese JDK en **Settings → Build Tools → Maven → Runner**.
3. Espera a que Maven descargue las dependencias. No necesitas instalar Maven: se incluye Maven Wrapper.
4. Inicia Oracle XE/Free y comprueba el servicio de tu instalación: `XEPDB1` en XE o, normalmente, `FREEPDB1` en Free.
5. En SQL Developer, conectado como administrador al PDB correspondiente, ejecuta `sql/crear_usuario_dev.sql` para crear un esquema exclusivo. No uses el esquema del inventario anterior: las tablas `categorias` y `clientes` tienen otro contrato.
6. Si tu configuración difiere de los valores siguientes, introduce las variables en **Run → Edit Configurations → Environment variables**:

   | Variable | Valor predeterminado en dev |
   | --- | --- |
   | `DB_URL` | `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
   | `DB_USERNAME` | `alquileresbd` |
   | `DB_PASSWORD` | `Alquileres2026` (solo desarrollo local) |
   | `SERVER_PORT` | `8081` |

7. Abre `AlquileresBackendApplication.java` y pulsa el botón verde junto a `main`. `dev` es el perfil predeterminado; también se incluye la configuración **EquipAndes dev** en `.run/`.
8. Después del primer arranque, Hibernate habrá creado las tablas. Conéctate como `alquileresbd` y ejecuta **una sola vez** `datos_semilla.sql`. Carga 3 categorías, 12 equipos y 6 clientes.
9. Abre **http://localhost:8081/swagger-ui.html**. El contrato JSON está en `/v3/api-docs` y la salud en `/api/v1/health`.

Por consola en Windows:

```bat
mvnw.cmd test
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

En Linux/macOS:

```bash
./mvnw test
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La cuenta de Oracle, el listener y el PDB deben existir y estar disponibles. Las credenciales de producción se proporcionan mediante variables de entorno.

## Arquitectura

Raíz: `src/main/java/pe/edu/upeu/AlquileresBackend/`.

| Paquete | Responsabilidad |
| --- | --- |
| `config` | CORS, OpenAPI y reloj de negocio de Perú |
| `controller` | HTTP, `@Valid`, `ResponseEntity` y delegación; solo recibe/devuelve DTO |
| `dto` | Records de entrada/salida y proyección `EquipoAlquiladoDTO` |
| `entity`, `enums` | Modelo JPA, relaciones LAZY y fechas automáticas |
| `repository` | Persistencia, bloqueos, Specification y consulta agregada JPQL |
| `service/generic` | `CrudService<REQ, RES, ID>` de la arquitectura base |
| `service/service`, `service/impl` | Contratos, reglas y límites transaccionales |
| `mapper` | Entidades a DTO planos dentro de la transacción |
| `exception` | Errores uniformes 400, 404, 409 y 500 |

Todos los recursos están bajo `/api/v1`. La entrega Unidad 1 no incorpora frontend, Spring Security, JWT, paginación, Spring Data REST, triggers ni procedimientos almacenados. El frontend original se conserva en su propio repositorio.

## Endpoints

| Método | Ruta | Comportamiento |
| --- | --- | --- |
| GET | `/health` | Consulta real `SELECT 1 FROM DUAL`; 200 UP o 503 DOWN |
| GET, POST | `/categorias` | Lista y registro |
| GET, PUT, DELETE | `/categorias/{id}` | Consulta, actualización y eliminación |
| GET | `/categorias/{id}/equipos` | Equipos de una categoría; 404 si no existe |
| GET, POST | `/equipos` | Lista y registro |
| GET, PUT, DELETE | `/equipos/{id}` | Consulta, actualización y eliminación |
| GET | `/equipos/buscar` | Filtros `nombre`, `categoriaId`, `tarifaMax`, `disponible`; `orden=nombre\|tarifa\|unidades`, `dir=asc\|desc` |
| GET, POST | `/clientes` | Lista y registro |
| GET, PUT | `/clientes/{id}` | Consulta y actualización |
| GET, POST | `/alquileres` | Lista con detalles y registro atómico |
| GET | `/alquileres/{id}` | Cabecera y detalles |
| PATCH | `/alquileres/{id}/devolver` | Devolución y reposición de unidades |
| GET | `/reportes/equipos-alquilados` | `periodo=AAAA-MM`, `categoriaId` opcional |

Creación: 201 y cabecera `Location`; consulta/actualización/devolución: 200; eliminación: 204. Validación: 400 con `validationErrors`; inexistencia: 404; regla o conflicto de integridad: 409; error inesperado: 500 con mensaje genérico.

Ejemplo de alquiler (sustituye la fecha por hoy + 7 días):

```json
{
  "clienteId": 1,
  "fechaInicio": "2026-11-20",
  "dias": 3,
  "lugarEvento": "Centro de convenciones Cerro Juli",
  "detalles": [
    {"equipoId": 1, "cantidad": 4},
    {"equipoId": 3, "cantidad": 6},
    {"equipoId": 9, "cantidad": 8}
  ]
}
```

## Reglas y decisiones para la defensa

- **RN-01:** `AlquilerServiceImpl.crear` comprueba cliente/equipos activos y fecha entre hoy y hoy + 60 días, inclusive. `Clock` permite probar fechas y define el día de negocio en `America/Lima`.
- **RN-02:** el mismo servicio valida y descuenta existencias. `devolver` solo acepta VIGENTE y repone exactamente las cantidades guardadas. Las dos operaciones usan `@Transactional`.
- **RN-03:** se bloquea el cliente y se ejecuta `countByClienteIdAndEstado` antes del alta. Esto protege el máximo de dos alquileres ante solicitudes simultáneas.
- **RN-04:** se detectan equipos repetidos antes de modificar existencias. Cada detalle guarda su tarifa histórica; importe = cantidad × tarifa × días. Se usa `BigDecimal`, escala 2 y `HALF_UP`; 7 o más días reciben 15 % de descuento.
- Los equipos se bloquean en orden de ID para proteger stock compartido y reducir bloqueos cruzados. Fallar cualquier línea revierte cabecera, detalles y stock.
- El reporte agrupa en JPQL y devuelve una proyección: usa **fecha de registro**, incluye VIGENTE y DEVUELTO, y suma importe bruto sin descontar. Ordena unidades descendente y código ascendente.
- El modelo usa `tarifa_diaria` y `unidades_disponibles`, coherentes con el script del Anexo A. Todas las entidades heredan `fecha_creacion` y `fecha_modificacion`.
- El nombre de categoría se compara sin mayúsculas ni espacios, tanto al crear como al actualizar. Se impide eliminar categorías con equipos y equipos con alquileres.
- CORS global solo permite `http://localhost:4200`. Un preflight desde `http://localhost:3000` responde 403.
- SLF4J: INFO en escrituras/reportes, WARN en reglas y ERROR en fallos inesperados.

## Perfiles y pruebas

| Perfil | Base | Esquema | Swagger |
| --- | --- | --- | --- |
| `dev` | Oracle local; valores configurables | `update` | Habilitado |
| `prod` | Oracle, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` obligatorias | `validate` | Deshabilitado |
| `test` | H2 en modo Oracle, dependencia exclusiva de test | `create-drop` | Solo contexto de pruebas |

`mvnw.cmd test` ejecuta pruebas HTTP contra el servidor real de Spring y pruebas unitarias. Incluyen los CP01–CP16, rollback después de descontar una línea, tarifa histórica, validación, fechas y conflictos. El perfil test comprueba lógica/JPA/HTTP pero **no sustituye** la demostración contra Oracle exigida por el examen.

Importa `postman/EquipAndes_Anexo_B.postman_collection.json`. Ejecuta en orden sobre un esquema recién cargado; la colección genera F=hoy+7 y el periodo actual en hora peruana. Comprueba códigos, montos, unidades, rollback, reporte, devolución y CORS. La variante CP01 DOWN requiere detener Oracle después de arrancar la API y hacer GET `/health`: debe devolver 503. No ejecutes la colección completa dos veces sin restaurar los datos; modifica existencias y crea alquileres.

## Git y alcance de la entrega

Rama de trabajo: `feature/equipandes-inciso`, basada en la rama proporcionada `feature/seguridad-pruebas-inciso`. El repositorio no tenía `develop` al comenzar. Esta adaptación conserva el historial de origen y no modifica `main` ni la rama de seguridad.

El PDF exige trabajo real de dos integrantes, revisiones cruzadas, aportes en distintas sesiones, PR y una etiqueta final `v1.0-unidad1`. Los commits de esta adaptación están identificados como Codex; no acreditan autoría individual de estudiantes ni participación del compañero. No se fabrican fechas, autores, revisiones, capturas ni pruebas Oracle.

Antes de entregar:

1. Validar el arranque con Oracle y ejecutar la colección del Anexo B desde Postman/Swagger.
2. Registrar capturas reales de Swagger, pruebas, gráfico Git y `git shortlog -sne` en el PDF de evidencias.
3. Acordar la rama de integración `develop` y completar revisión real por el compañero antes de fusionar a `main`.
4. Crear `v1.0-unidad1` solo en el commit finalmente evaluado.
5. Implementar la solicitud **SC-A, SC-B, SC-C o SC-D que asigne el docente**, en su rama `sc-<letra>-inciso`. Son alternativas de la Parte II y no forman parte de esta implementación principal.

No se afirma que se haya conectado a tu Oracle ni actualizado la carpeta de IntelliJ de tu PC.
