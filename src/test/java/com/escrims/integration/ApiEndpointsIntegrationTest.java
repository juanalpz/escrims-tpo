package com.escrims.integration;

import com.escrims.domain.entities.Sancion;
import com.escrims.domain.repository.UsuarioRepository;
import com.escrims.infrastructure.scheduler.ScrimScheduler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Prueba de integración de la API REST: recorre auth, ciclo de vida del scrim,
 * comandos, penalidades y el pipeline de moderación a través de HTTP.
 */
@SpringBootTest(properties = "escrims.auth.moderadores=mod@escrims.gg")
@AutoConfigureMockMvc
class ApiEndpointsIntegrationTest {

    private record Respuesta(int status, JsonNode body) {
    }

    private record Cuenta(String id, String token) {
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private ScrimScheduler scheduler;
    @Autowired
    private UsuarioRepository usuarios;

    private Respuesta enviar(MockHttpServletRequestBuilder peticion, String token, Object cuerpo) throws Exception {
        if (token != null) {
            peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(cuerpo));
        }
        var resultado = mvc.perform(peticion).andReturn().getResponse();
        String texto = resultado.getContentAsString();
        return new Respuesta(resultado.getStatus(), texto.isBlank() ? json.nullNode() : json.readTree(texto));
    }

    private Respuesta postear(String ruta, String token, Object cuerpo) throws Exception {
        return enviar(post(ruta), token, cuerpo);
    }

    private Map<String, Object> datosRegistro(String username) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("username", username);
        datos.put("email", username + "@escrims.gg");
        datos.put("password", "clave-de-prueba");
        datos.put("juegoPrincipal", "VALORANT");
        datos.put("rangoActual", "PLATINO");
        datos.put("region", "LAS");
        return datos;
    }

    /** Registra una cuenta, verifica su email e inicia sesión. */
    private Cuenta cuentaVerificada(String username) throws Exception {
        Respuesta registro = postear("/api/auth/register", null, datosRegistro(username));
        assertEquals(201, registro.status(), registro.body().toString());
        String email = registro.body().at("/usuario/email").asText();

        Respuesta verificacion = postear("/api/auth/verificar-email", null,
                Map.of("email", email, "codigo", registro.body().get("codigoVerificacion").asText()));
        assertEquals(200, verificacion.status());
        assertTrue(verificacion.body().get("emailVerificado").asBoolean());

        Respuesta login = postear("/api/auth/login", null, Map.of("email", email, "password", "clave-de-prueba"));
        assertEquals(200, login.status());
        return new Cuenta(login.body().at("/usuario/id").asText(), login.body().get("token").asText());
    }

    private Map<String, Object> datosScrim(String organizadorId, LocalDateTime fechaHora) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("organizadorId", organizadorId);
        datos.put("juego", "VALORANT");
        datos.put("formato", "UNO_VS_UNO");
        datos.put("region", "LAS");
        datos.put("rangoMin", "ORO");
        datos.put("rangoMax", "DIAMANTE");
        datos.put("latenciaMax", 80);
        datos.put("fechaHora", fechaHora.toString());
        datos.put("duracionMinutos", 45);
        return datos;
    }

    @Test
    @DisplayName("Auth: valida el registro, rechaza duplicados y credenciales inválidas, y exige token")
    void authValidaRegistroLoginYToken() throws Exception {
        Respuesta invalido = postear("/api/auth/register", null, Map.of("username", "x"));
        assertEquals(400, invalido.status());
        assertTrue(invalido.body().get("erroresValidacion").has("email"));

        Cuenta cuenta = cuentaVerificada("auth_user");
        assertEquals(409, postear("/api/auth/register", null, datosRegistro("auth_user")).status());
        assertEquals(401, postear("/api/auth/login", null,
                Map.of("email", "auth_user@escrims.gg", "password", "otra-clave")).status());

        Respuesta me = enviar(get("/api/auth/me"), cuenta.token(), null);
        assertEquals(200, me.status());
        assertEquals("auth_user", me.body().get("username").asText());
        assertEquals("USER", me.body().get("rol").asText());
        assertEquals(401, enviar(get("/api/auth/me"), null, null).status());
        assertEquals(401, enviar(get("/api/auth/me"), "token-inventado", null).status());
    }

    @Test
    @DisplayName("Scrims: una cuenta sin email verificado no puede crear y nadie opera en nombre de otro")
    void scrimsExigenIdentidadYEmailVerificado() throws Exception {
        Cuenta verificada = cuentaVerificada("identidad_ok");
        Respuesta registro = postear("/api/auth/register", null, datosRegistro("sin_verificar"));
        Respuesta login = postear("/api/auth/login", null,
                Map.of("email", "sin_verificar@escrims.gg", "password", "clave-de-prueba"));
        String idSinVerificar = registro.body().at("/usuario/id").asText();
        String tokenSinVerificar = login.body().get("token").asText();
        LocalDateTime fecha = LocalDateTime.now().plusHours(2).withNano(0);

        assertEquals(401, postear("/api/scrims", null, datosScrim(verificada.id(), fecha)).status());
        assertEquals(403, postear("/api/scrims", verificada.token(), datosScrim(idSinVerificar, fecha)).status());
        assertEquals(409, postear("/api/scrims", tokenSinVerificar, datosScrim(idSinVerificar, fecha)).status());
        assertEquals(400, postear("/api/scrims", verificada.token(),
                datosScrim(verificada.id(), LocalDateTime.now().minusDays(1))).status());
        assertEquals(404, enviar(get("/api/scrims/" + UUID.randomUUID()), null, null).status());
        assertEquals(400, enviar(get("/api/scrims/no-es-un-uuid"), null, null).status());
    }

    @Test
    @DisplayName("E2E: crear -> postular -> comandos -> confirmar -> jugar -> abandono -> finalizar -> reportes -> moderación")
    void flujoCompletoDeScrimPenalidadesYModeracion() throws Exception {
        Cuenta org = cuentaVerificada("e2e_org");
        Cuenta p1 = cuentaVerificada("e2e_p1");
        Cuenta p2 = cuentaVerificada("e2e_p2");
        Cuenta mod = cuentaVerificada("mod");
        LocalDateTime fecha = LocalDateTime.now().plusHours(2).withNano(0);

        // Crear y buscar
        Respuesta creado = postear("/api/scrims", org.token(), datosScrim(org.id(), fecha));
        assertEquals(201, creado.status(), creado.body().toString());
        assertEquals("Buscando jugadores", creado.body().get("estadoActual").asText());
        String scrimId = creado.body().get("id").asText();
        String base = "/api/scrims/" + scrimId;

        Respuesta busqueda = enviar(
                get("/api/scrims?juego=VALORANT&region=&rangoMin=PLATINO&rangoMax=&fecha=" + fecha.toLocalDate()
                        + "&latenciaMax=100"), null, null);
        assertEquals(200, busqueda.status());
        assertTrue(busqueda.body().toString().contains(scrimId));
        assertFalse(enviar(get("/api/scrims?juego=CS2"), null, null).body().toString().contains(scrimId));

        // Postulaciones: al completar los cupos se arma el lobby
        Respuesta post1 = postear(base + "/postulaciones", p1.token(),
                Map.of("usuarioId", p1.id(), "rolDeseado", "DUELISTA"));
        assertEquals(201, post1.status(), post1.body().toString());
        assertEquals("ACEPTADA", post1.body().get("estado").asText());
        assertEquals(409, postear(base + "/postulaciones", p1.token(),
                Map.of("usuarioId", p1.id(), "rolDeseado", "DUELISTA")).status());
        Respuesta post2 = postear(base + "/postulaciones", p2.token(),
                Map.of("usuarioId", p2.id(), "rolDeseado", "CENTINELA"));
        assertEquals("Lobby armado", post2.body().at("/scrim/estadoActual").asText());
        assertFalse(enviar(get("/api/scrims"), null, null).body().toString().contains(scrimId));

        // Primer reporte contra un jugador sin antecedentes: lo cierra el auto-resolver
        Respuesta reporteAuto = postear(base + "/reportes", p1.token(),
                Map.of("reportadoId", p2.id(), "motivo", "Lenguaje ofensivo en el lobby"));
        assertEquals(201, reporteAuto.status(), reporteAuto.body().toString());
        assertEquals("RECHAZADO", reporteAuto.body().get("estado").asText());
        assertTrue(reporteAuto.body().get("sancion").isNull());
        assertEquals(403, postear(base + "/reportes", mod.token(),
                Map.of("reportadoId", p2.id(), "motivo", "No participé del scrim")).status());

        // Acciones del organizador (Command)
        Map<String, Object> swap = Map.of("organizadorId", org.id(),
                "jugadorPrincipalId", p1.id(), "jugadorSecundarioId", p2.id());
        assertEquals(200, postear(base + "/acciones/swap", org.token(), swap).status());
        assertEquals(200, postear(base + "/acciones/deshacer", org.token(), Map.of("organizadorId", org.id())).status());
        assertEquals(409, postear(base + "/acciones/deshacer", org.token(), Map.of("organizadorId", org.id())).status());
        assertEquals(400, postear(base + "/acciones/teletransportar", org.token(),
                Map.of("organizadorId", org.id())).status());
        assertEquals(403, postear(base + "/acciones/swap", p1.token(), Map.of("organizadorId", p1.id(),
                "jugadorPrincipalId", p1.id(), "jugadorSecundarioId", p2.id())).status());

        // Confirmaciones y arranque automático por scheduler
        assertEquals(200, postear(base + "/confirmaciones", p1.token(), Map.of("usuarioId", p1.id())).status());
        Respuesta conf2 = postear(base + "/confirmaciones", p2.token(), Map.of("usuarioId", p2.id()));
        assertEquals("Confirmado", conf2.body().at("/scrim/estadoActual").asText());
        assertEquals(409, postear(base + "/abandonos", org.token(), Map.of("usuarioId", p1.id())).status());

        scheduler.procesarTransicionesProgramadas(fecha);
        assertEquals("En juego", enviar(get(base), null, null).body().get("estadoActual").asText());
        assertEquals(409, postear(base + "/cancelar", org.token(), null).status());

        // Abandono: primer strike, sin cooldown
        Respuesta abandono = postear(base + "/abandonos", org.token(),
                Map.of("usuarioId", p1.id(), "detalle", "Se fue en la ronda 5"));
        assertEquals(201, abandono.status(), abandono.body().toString());
        assertEquals(1, abandono.body().get("strikesTotales").asInt());
        assertTrue(abandono.body().at("/sancion/cooldownHasta").isNull());
        assertEquals(409, postear(base + "/no-shows", org.token(), Map.of("usuarioId", p1.id())).status());
        assertEquals(403, postear(base + "/no-shows", p1.token(), Map.of("usuarioId", p2.id())).status());

        // Finalizar y cargar estadísticas
        assertEquals(403, postear(base + "/finalizar", p1.token(), null).status());
        Respuesta finalizado = postear(base + "/finalizar", org.token(), Map.of("ganador", "EQUIPO_B", "marcador", "13-7"));
        assertEquals("Finalizado", finalizado.body().get("estadoActual").asText());
        Respuesta estadistica = postear(base + "/estadisticas", org.token(),
                Map.of("usuarioId", p2.id(), "mvp", true, "kda", "18/6/4"));
        assertEquals(201, estadistica.status(), estadistica.body().toString());
        assertEquals("18/6/4", estadistica.body().get("kda").asText());
        assertEquals(400, postear(base + "/estadisticas", org.token(),
                Map.of("usuarioId", p1.id(), "mvp", false, "kda", "muchas")).status());

        // Reporte contra un jugador con un strike: lo sanciona el bot (2.º strike, cooldown)
        Respuesta reporteBot = postear(base + "/reportes", p2.token(),
                Map.of("reportadoId", p1.id(), "motivo", "Abandonó e insultó al salir"));
        assertEquals("RESUELTO", reporteBot.body().get("estado").asText());
        assertFalse(reporteBot.body().at("/sancion/cooldownHasta").isNull());
        assertEquals(409, postear(base + "/reportes", p2.token(),
                Map.of("reportadoId", p1.id(), "motivo", "Reporte repetido")).status());

        // Con 3 strikes el siguiente reporte se escala al moderador humano
        usuarios.buscarPorId(UUID.fromString(p1.id())).orElseThrow().agregarSancion(new Sancion(1, null));
        Respuesta reporteHumano = postear(base + "/reportes", org.token(),
                Map.of("reportadoId", p1.id(), "motivo", "Reincidente, pido revisión"));
        assertEquals("EN_REVISION", reporteHumano.body().get("estado").asText());
        String reporteId = reporteHumano.body().get("id").asText();

        assertEquals(403, enviar(get("/api/moderacion/reportes"), p2.token(), null).status());
        Respuesta bandeja = enviar(get("/api/moderacion/reportes?estado=EN_REVISION"), mod.token(), null);
        assertEquals(200, bandeja.status());
        assertEquals(1, bandeja.body().size());
        assertEquals(reporteId, bandeja.body().get(0).get("id").asText());

        String rutaResolucion = "/api/moderacion/reportes/" + reporteId + "/resolucion";
        assertEquals(403, postear(rutaResolucion, org.token(), Map.of("sancionar", true)).status());
        assertEquals(404, postear("/api/moderacion/reportes/" + UUID.randomUUID() + "/resolucion", mod.token(),
                Map.of("sancionar", true)).status());
        Respuesta resolucion = postear(rutaResolucion, mod.token(),
                Map.of("sancionar", true, "nota", "Conducta reiterada"));
        assertEquals(200, resolucion.status(), resolucion.body().toString());
        assertEquals("RESUELTO", resolucion.body().get("estado").asText());
        assertEquals(409, postear(rutaResolucion, mod.token(), Map.of("sancionar", false)).status());

        // Historial disciplinario y efecto del cooldown
        assertEquals(403, enviar(get("/api/usuarios/" + p1.id() + "/sanciones"), p2.token(), null).status());
        Respuesta historial = enviar(get("/api/usuarios/" + p1.id() + "/sanciones"), p1.token(), null);
        assertEquals(200, historial.status());
        assertEquals(4, historial.body().get("strikes").asInt());
        assertTrue(historial.body().get("cooldownActivo").asBoolean());
        assertEquals(1, historial.body().get("abandonos").asInt());
        assertEquals(1, historial.body().get("scrimsJugados").asInt());
        assertEquals(1, historial.body().get("incidencias").size());

        Respuesta otroScrim = postear("/api/scrims", org.token(), datosScrim(org.id(), fecha.plusDays(1)));
        Respuesta bloqueado = postear("/api/scrims/" + otroScrim.body().get("id").asText() + "/postulaciones",
                p1.token(), Map.of("usuarioId", p1.id(), "rolDeseado", "DUELISTA"));
        assertEquals(409, bloqueado.status());
        assertTrue(bloqueado.body().get("mensaje").asText().contains("cooldown"));
    }
}
