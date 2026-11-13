package pe.edu.upeu.AlquileresBackend;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * H2 se limita al perfil de pruebas. Dev y prod siempre usan Oracle. Las llamadas HTTP no comparten
 * transacción con el test: un rollback fallido deja evidencia real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EquipAndesApiTest {
  @LocalServerPort int puerto;
  @Autowired JdbcTemplate jdbc;
  @Autowired DataSource dataSource;
  private final HttpClient http = HttpClient.newHttpClient();
  private final JsonMapper json = JsonMapper.builder().build();

  private String f() {
    return LocalDate.now(ZoneId.of("America/Lima")).plusDays(7).toString();
  }

  @BeforeEach
  void semilla() throws Exception {
    for (String tabla :
        List.of("detalle_alquileres", "alquileres", "equipos", "clientes", "categorias")) {
      jdbc.execute("DELETE FROM " + tabla);
      jdbc.execute("ALTER TABLE " + tabla + " ALTER COLUMN id RESTART WITH 1");
    }
    try (var c = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(c, new FileSystemResource("datos_semilla.sql"));
    }
  }

  record Res(int status, JsonNode body, HttpHeaders headers) {}

  private Res call(String method, String path, String body) throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/v1" + path))
            .header("Content-Type", "application/json");
    builder.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));
    var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    return new Res(
        response.statusCode(),
        response.body().isBlank() ? null : json.readTree(response.body()),
        response.headers());
  }

  private Res rent(int cliente, int dias, int... pares) throws Exception {
    return rentFecha(cliente, dias, f(), pares);
  }

  private Res rentFecha(int cliente, int dias, String fecha, int... pares) throws Exception {
    var detalles = new ArrayList<Map<String, Object>>();
    for (int i = 0; i < pares.length; i += 2)
      detalles.add(Map.of("equipoId", pares[i], "cantidad", pares[i + 1]));
    return call(
        "POST",
        "/alquileres",
        json.writeValueAsString(
            Map.of(
                "clienteId", cliente, "fechaInicio", fecha, "dias", dias, "detalles", detalles)));
  }

  private int stock(int id) {
    return jdbc.queryForObject(
        "SELECT unidades_disponibles FROM equipos WHERE id=?", Integer.class, id);
  }

  private int count(String tabla) {
    return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
  }

  private void money(JsonNode node, String field, String expected) {
    assertEquals(0, node.get(field).decimalValue().compareTo(new java.math.BigDecimal(expected)));
  }

  @Test
  void anexoB_completo_con_stock_rollback_reporte_y_devolucion() throws Exception {
    Res health = call("GET", "/health", null);
    assertEquals(200, health.status);
    assertEquals("UP", health.body.get("database").asText());
    assertEquals(
        409, call("POST", "/categorias", "{\"nombre\":\"  sonido  \",\"estado\":true}").status);
    Res invalid =
        call(
            "POST",
            "/equipos",
            "{\"codigo\":\"eqp1\",\"nombre\":\"TV\",\"tarifaDiaria\":0,\"unidadesDisponibles\":-1,\"estado\":true,\"categoriaId\":1}");
    assertEquals(400, invalid.status);
    for (String k : List.of("codigo", "nombre", "tarifaDiaria", "unidadesDisponibles"))
      assertTrue(invalid.body.get("validationErrors").has(k));
    assertEquals(
        404,
        call(
                "POST",
                "/equipos",
                "{\"codigo\":\"EQP-099\",\"nombre\":\"Equipo de"
                    + " prueba\",\"tarifaDiaria\":25,\"unidadesDisponibles\":1,\"estado\":true,\"categoriaId\":99}")
            .status);
    Res filtro =
        call("GET", "/equipos/buscar?categoriaId=2&disponible=true&orden=tarifa&dir=asc", null);
    assertEquals(200, filtro.status);
    assertEquals(2, filtro.body.size());
    assertEquals("EQP-005", filtro.body.get(0).get("codigo").asText());
    assertEquals("EQP-006", filtro.body.get(1).get("codigo").asText());
    Res a = rent(1, 3, 1, 4, 3, 6, 9, 8);
    assertEquals(201, a.status);
    assertEquals(18, a.body.get("totalUnidades").asInt());
    money(a.body, "subtotal", "2190.00");
    money(a.body, "descuento", "0.00");
    money(a.body, "montoTotal", "2190.00");
    assertEquals(6, stock(1));
    assertEquals(14, stock(3));
    assertEquals(8, stock(9));
    assertEquals(409, rent(4, 1, 1, 1).status);
    assertEquals(409, rent(2, 2, 7, 2, 4, 1).status);
    assertEquals(8, stock(7));
    assertEquals(1, count("alquileres"));
    assertEquals(3, count("detalle_alquileres"));
    assertEquals(
        409,
        rentFecha(3, 1, LocalDate.now(ZoneId.of("America/Lima")).minusDays(1).toString(), 1, 1)
            .status);
    Res semanal = rent(2, 7, 2, 2);
    assertEquals(201, semanal.status);
    money(semanal.body, "subtotal", "1680.00");
    money(semanal.body, "descuento", "252.00");
    money(semanal.body, "montoTotal", "1428.00");
    assertEquals(2, stock(2));
    Res segundo = rent(1, 1, 5, 2);
    assertEquals(201, segundo.status);
    money(segundo.body, "montoTotal", "80.00");
    assertEquals(4, stock(5));
    assertEquals(409, rent(1, 1, 6, 1).status);
    assertEquals(3, stock(6));
    assertEquals(409, rent(3, 2, 7, 1, 7, 1).status);
    assertEquals(8, stock(7));
    String periodo = YearMonth.now(ZoneId.of("America/Lima")).toString();
    Res reporte = call("GET", "/reportes/equipos-alquilados?periodo=" + periodo, null);
    assertEquals(200, reporte.status);
    assertEquals(5, reporte.body.size());
    String[] codigos = {"EQP-009", "EQP-003", "EQP-001", "EQP-002", "EQP-005"};
    int[] uds = {8, 6, 4, 2, 2};
    String[] bruto = {"600.00", "630.00", "960.00", "1680.00", "80.00"};
    for (int i = 0; i < 5; i++) {
      var row = reporte.body.get(i);
      assertEquals(codigos[i], row.get("codigo").asText());
      assertEquals(uds[i], row.get("unidadesAlquiladas").asInt());
      money(row, "importeBruto", bruto[i]);
    }
    long id = a.body.get("id").asLong();
    Res dev = call("PATCH", "/alquileres/" + id + "/devolver", null);
    assertEquals(200, dev.status);
    assertEquals("DEVUELTO", dev.body.get("estado").asText());
    assertFalse(dev.body.get("fechaDevolucion").isNull());
    assertEquals(10, stock(1));
    assertEquals(20, stock(3));
    assertEquals(16, stock(9));
    assertEquals(409, call("PATCH", "/alquileres/" + id + "/devolver", null).status);
    assertEquals(
        reporte.body, call("GET", "/reportes/equipos-alquilados?periodo=" + periodo, null).body);
    for (int origen : List.of(4200, 3000)) {
      var preflight =
          HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/v1/equipos"))
              .header("Origin", "http://localhost:" + origen)
              .header("Access-Control-Request-Method", "POST")
              .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
              .build();
      var response = http.send(preflight, HttpResponse.BodyHandlers.ofString());
      assertEquals(origen == 4200 ? 200 : 403, response.statusCode());
      assertEquals(
          origen == 4200 ? Optional.of("http://localhost:4200") : Optional.empty(),
          response.headers().firstValue("Access-Control-Allow-Origin"));
    }
  }

  @Test
  void rollback_despues_de_descontar_la_primera_linea() throws Exception {
    // EQP001 se procesa y descuenta antes de que falle EQP004; el rollback debe restaurarlo.
    assertEquals(409, rent(2, 2, 1, 2, 4, 1).status);
    assertEquals(10, stock(1));
    assertEquals(0, count("alquileres"));
    assertEquals(0, count("detalle_alquileres"));
  }

  @Test
  void fecha_limite_e_inactividad() throws Exception {
    assertEquals(
        409,
        rentFecha(1, 1, LocalDate.now(ZoneId.of("America/Lima")).plusDays(61).toString(), 1, 1)
            .status);
    assertEquals(409, rent(1, 1, 12, 1).status);
    assertEquals(
        201,
        rentFecha(1, 1, LocalDate.now(ZoneId.of("America/Lima")).plusDays(60).toString(), 1, 1)
            .status);
  }

  @Test
  void contrato_crud_errores_y_referencias() throws Exception {
    Res categoria = call("POST", "/categorias", "{\"nombre\":\"Audiovisuales\",\"estado\":true}");
    assertEquals(201, categoria.status);
    assertTrue(categoria.headers.firstValue("Location").isPresent());
    long id = categoria.body.get("id").asLong();
    assertEquals(
        409,
        call("PUT", "/categorias/" + id, "{\"nombre\":\"  S o n i d o \",\"estado\":true}").status);
    assertEquals(409, call("DELETE", "/categorias/1", null).status);
    assertEquals(204, call("DELETE", "/categorias/" + id, null).status);
    assertEquals(404, call("GET", "/categorias/" + id, null).status);
    assertEquals(404, call("GET", "/categorias/99/equipos", null).status);
    assertEquals(400, call("GET", "/equipos/buscar?orden=sql", null).status);
    assertEquals(400, call("GET", "/equipos/buscar?dir=otro", null).status);
    assertEquals(400, call("GET", "/reportes/equipos-alquilados?periodo=2026-99", null).status);
    assertEquals(400, call("GET", "/reportes/equipos-alquilados?periodo=2026-1", null).status);
    assertEquals(
        400,
        call(
                "POST",
                "/alquileres",
                "{\"clienteId\":1,\"fechaInicio\":\"" + f() + "\",\"dias\":1,\"detalles\":[null]}")
            .status);
    assertEquals(400, rent(1, 31, 1, 1).status);
    assertEquals(404, rent(99, 1, 1, 1).status);
    assertEquals(404, rent(1, 1, 99, 1).status);
    assertEquals(201, rent(1, 1, 1, 1).status);
    assertEquals(409, call("DELETE", "/equipos/1", null).status);
  }

  @Test
  void tarifa_historica_y_reporte_por_fecha_de_registro() throws Exception {
    Res a = rent(1, 7, 1, 1);
    assertEquals(201, a.status);
    money(a.body, "subtotal", "560.00");
    money(a.body, "descuento", "84.00");
    assertEquals(
        200,
        call(
                "PUT",
                "/equipos/1",
                "{\"codigo\":\"EQP-001\",\"nombre\":\"Parlante activo de 15"
                    + " pulgadas\",\"tarifaDiaria\":999,\"unidadesDisponibles\":9,\"estado\":true,\"categoriaId\":1}")
            .status);
    Res leido = call("GET", "/alquileres/" + a.body.get("id").asLong(), null);
    money(leido.body.get("detalles").get(0), "tarifaDiaria", "80.00");
    money(leido.body, "subtotal", "560.00");
    Res reporte =
        call(
            "GET",
            "/reportes/equipos-alquilados?periodo="
                + YearMonth.now(ZoneId.of("America/Lima"))
                + "&categoriaId=1",
            null);
    money(reporte.body.get(0), "importeBruto", "560.00");
    assertEquals(0, call("GET", "/reportes/equipos-alquilados?periodo=2000-01", null).body.size());
  }

  @Test
  void cliente_codigo_documento_unicos_y_tipo_validado() throws Exception {
    String plantilla =
        "{\"codigo\":\"%s\",\"documento\":\"%s\",\"nombre\":\"Cliente"
            + " prueba\",\"email\":\"prueba@example.com\",\"tipo\":\"%s\",\"estado\":true}";
    assertEquals(
        409,
        call("POST", "/clientes", plantilla.formatted("CLI-001", "12345678", "PERSONA")).status);
    assertEquals(
        409,
        call("POST", "/clientes", plantilla.formatted("CLI-099", "42315678", "PERSONA")).status);
    assertEquals(
        400, call("POST", "/clientes", plantilla.formatted("CLI-099", "12345678", "OTRO")).status);
    assertEquals(
        201,
        call("POST", "/clientes", plantilla.formatted("CLI-099", "12345678", "PERSONA")).status);
  }

  @Test
  void peticionesSimultaneasRespetanLimiteStockYDevolucionUnica() throws Exception {
    try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      assertEquals(201, rent(1, 1, 1, 1).status());
      var limite1 = pool.submit(() -> rent(1, 1, 3, 1).status());
      var limite2 = pool.submit(() -> rent(1, 1, 5, 1).status());
      assertEquals(
          List.of(201, 409),
          java.util.stream.Stream.of(limite1.get(), limite2.get()).sorted().toList());
      assertEquals(2, count("alquileres"));
      var stock1 = pool.submit(() -> rent(2, 1, 7, 5).status());
      var stock2 = pool.submit(() -> rent(3, 1, 7, 5).status());
      assertEquals(
          List.of(201, 409),
          java.util.stream.Stream.of(stock1.get(), stock2.get()).sorted().toList());
      assertEquals(3, stock(7));
      var dev1 = pool.submit(() -> call("PATCH", "/alquileres/1/devolver", null).status());
      var dev2 = pool.submit(() -> call("PATCH", "/alquileres/1/devolver", null).status());
      assertEquals(
          List.of(200, 409), java.util.stream.Stream.of(dev1.get(), dev2.get()).sorted().toList());
      assertEquals(10, stock(1));
    }
  }
}
