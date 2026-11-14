package com.escrims.domain.builder;

import com.escrims.domain.entities.*;
import com.escrims.domain.state.BuscandoState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitarios para {@link ScrimBuilder} con mensajes de depuración en consola.
 * Permiten verificar visualmente cada regla de negocio e invariante durante la ejecución.
 */
class ScrimBuilderTest {

    private Usuario organizadorValido;

    @BeforeEach
    void setUp() {
        // Configuramos un organizador apto según RN-01 (email verificado) y RN-06 (sin cooldowns)
        organizadorValido = new Usuario("organizador", 1500, 30, Region.LAS);
        organizadorValido.setEmailVerificado(true);
    }

    @Test
    @DisplayName("Camino feliz: Construye un Scrim válido con todas sus invariantes cumplidas")
    void deberiaConstruirScrimValidoCorrectamente() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaConstruirScrimValidoCorrectamente (Camino Feliz)");
        System.out.println("[TEST DEBUG] 1. Configurando organizador válido con email verificado: " + organizadorValido.getUsername());

        LocalDateTime fechaFutura = LocalDateTime.now().plusHours(3);

        System.out.println("[TEST DEBUG] 2. Invocando ScrimBuilder con parámetros reglamentarios:");
        System.out.println("             - Juego: VALORANT");
        System.out.println("             - Formato: CINCO_VS_CINCO (debe calcular 10 cupos)");
        System.out.println("             - Región: LAS (Buenos Aires)");
        System.out.println("             - Rango: ORO a PLATINO (1200 a 1800 MMR)");
        System.out.println("             - Latencia máxima: 80 ms");
        System.out.println("             - Fecha/Hora: " + fechaFutura + " (posterior a now())");
        System.out.println("             - Duración: 60 minutos (válida entre 15 y 360)");
        System.out.println("             - Roles pedidos: 2 Duelistas + 2 Controladores (4 <= 10 cupos)");

        Scrim scrim = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .rango(Rango.ORO, Rango.PLATINO)
                .latenciaMax(80)
                .fechaHora(fechaFutura)
                .duracion(60)
                .modalidad(Modalidad.RANKED_LIKE)
                .agregarReglaRol(RolJuego.DUELISTA, 2)
                .agregarReglaRol(RolJuego.CONTROLADOR, 2)
                .build();

        System.out.println("[TEST DEBUG] 3. Scrim construido con éxito. Verificando estado interno:");
        System.out.println("             - UUID generado: " + scrim.getId());
        System.out.println("             - Cupos calculados: " + scrim.getCupos());
        System.out.println("             - Estado inicial: " + scrim.getEstado().getClass().getSimpleName());
        System.out.println("             - Postulaciones iniciales: " + scrim.getPostulaciones().size());

        assertNotNull(scrim.getId(), "El Scrim debe generarse con un UUID.");
        assertEquals(organizadorValido, scrim.getOrganizador());
        assertEquals(Juego.VALORANT, scrim.getJuego());
        assertEquals(10, scrim.getCupos(), "Un formato 5v5 debe calcular 10 cupos totales.");
        assertEquals(80, scrim.getLatenciaMax());
        assertEquals(Modalidad.RANKED_LIKE, scrim.getModalidad());
        assertNotNull(scrim.getPostulaciones(), "El Set de postulaciones debe inicializarse vacío.");
        assertTrue(scrim.getPostulaciones().isEmpty());
        assertInstanceOf(BuscandoState.class, scrim.getEstado(), "Todo Scrim debe nacer en estado BuscandoState.");

        System.out.println("[TEST DEBUG] RESULTADO: OK - Todas las aserciones del camino feliz se cumplieron.");
    }

    @Test
    @DisplayName("Valores por defecto: Asigna Modalidad.CASUAL y duración de 60 minutos si no se especifican")
    void deberiaAplicarValoresPorDefecto() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaAplicarValoresPorDefecto (Defaults)");
        System.out.println("[TEST DEBUG] Creando Scrim sin invocar .modalidad(...) ni .duracion(...)...");

        Scrim scrim = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();

        System.out.println("[TEST DEBUG] Verificando valores asignados por defecto:");
        System.out.println("             - Modalidad por defecto: " + scrim.getModalidad());
        System.out.println("             - Duración por defecto: " + scrim.getDuracion() + " minutos");

        assertEquals(Modalidad.CASUAL, scrim.getModalidad(), "Por defecto la modalidad debe ser CASUAL.");
        assertEquals(60, scrim.getDuracion(), "Por defecto la duración debe ser de 60 minutos.");
        System.out.println("[TEST DEBUG] RESULTADO: OK - Valores por defecto aplicados correctamente.");
    }

    @Test
    @DisplayName("Permite crear un Scrim abierto sin restricción de rangos (ambos null)")
    void deberiaPermitirScrimSinLimiteDeRangos() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaPermitirScrimSinLimiteDeRangos");
        System.out.println("[TEST DEBUG] Verificando que si no se define rango, el Scrim admita cualquier MMR (scrim abierto).");

        Scrim scrim = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .latenciaMax(100)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .build();

        assertNull(scrim.getRangoMin());
        assertNull(scrim.getRangoMax());
        assertTrue(scrim.mmrDentroDeRango(100), "Debe admitir MMR bajo (Hierro).");
        assertTrue(scrim.mmrDentroDeRango(3000), "Debe admitir MMR alto (Radiante).");

        System.out.println("[TEST DEBUG] RESULTADO: OK - Scrim abierto admite tanto MMR=100 como MMR=3000.");
    }

    @Test
    @DisplayName("Permite crear un Scrim cerrado a un único rango (rangoMin == rangoMax)")
    void deberiaPermitirRangoMinimoIgualARangoMaximo() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaPermitirRangoMinimoIgualARangoMaximo");
        System.out.println("[TEST DEBUG] Configurando .rango(Rango.ORO, Rango.ORO) (solo jugadores Oro: 1200 a 1499 MMR)...");

        Scrim scrim = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .rango(Rango.ORO, Rango.ORO)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();

        assertEquals(Rango.ORO, scrim.getRangoMin());
        assertEquals(Rango.ORO, scrim.getRangoMax());
        assertEquals(Rango.ORO, scrim.getRango());

        // Dentro de Oro (1200..1499)
        assertTrue(scrim.mmrDentroDeRango(1200), "Debe admitir MMR 1200 (mínimo de Oro).");
        assertTrue(scrim.mmrDentroDeRango(1350), "Debe admitir MMR 1350 (dentro de Oro).");
        assertTrue(scrim.mmrDentroDeRango(1499), "Debe admitir MMR 1499 (máximo de Oro).");

        // Fuera de Oro
        assertFalse(scrim.mmrDentroDeRango(1199), "Debe rechazar MMR 1199 (Plata).");
        assertFalse(scrim.mmrDentroDeRango(1500), "Debe rechazar MMR 1500 (Platino).");

        System.out.println("[TEST DEBUG] RESULTADO: OK - Scrim cerrado a un único rango validado exitosamente.");
    }

    @Test
    @DisplayName("RN-01: Falla si el organizador no tiene el email verificado")
    void deberiaFallarSiOrganizadorNoTieneEmailVerificado() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiOrganizadorNoTieneEmailVerificado (RN-01)");

        Usuario noVerificado = new Usuario("sinVerificar", 1200, 40, Region.SA);
        noVerificado.setEmailVerificado(false);
        System.out.println("[TEST DEBUG] Intentando build con organizador no verificado...");

        ScrimBuilder builder = new ScrimBuilder()
                .organizador(noVerificado)
                .juego(Juego.CS2)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.SA)
                .latenciaMax(60)
                .fechaHora(LocalDateTime.now().plusHours(2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("email verificado"));
        System.out.println("[TEST DEBUG] RESULTADO: OK - Se bloqueó la creación por falta de verificación.");
    }

    @Test
    @DisplayName("RN-06: Falla si el organizador tiene un cooldown o sanción activa")
    void deberiaFallarSiOrganizadorTieneSancionActiva() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiOrganizadorTieneSancionActiva (RN-06)");

        Usuario sancionado = new Usuario("sancionado", 1500, 30, Region.LAS);
        sancionado.setEmailVerificado(true);
        // Cooldown activo por 24hs
        sancionado.agregarSancion(new Sancion(1, LocalDateTime.now().plusDays(1)));
        System.out.println("[TEST DEBUG] Intentando build con organizador con cooldown activo hasta: " + sancionado.getSanciones().get(0).getCooldownHasta());

        ScrimBuilder builder = new ScrimBuilder()
                .organizador(sancionado)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("sanción activa"));
        System.out.println("[TEST DEBUG] RESULTADO: OK - Se bloqueó la creación por sanción activa.");
    }

    @Test
    @DisplayName("RN-03: Falla si el rango mínimo es superior al rango máximo")
    void deberiaFallarSiRangoMinEsMayorARangoMax() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiRangoMinEsMayorARangoMax (RN-03)");
        System.out.println("[TEST DEBUG] Intentando rango ilógico: min=DIAMANTE (" + Rango.DIAMANTE.ordinal() +
                           ") > max=PLATA (" + Rango.PLATA.ordinal() + ")");

        // Invalido: Diamante > Plata
        ScrimBuilder builder = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .rango(Rango.DIAMANTE, Rango.PLATA)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        System.out.println("[TEST DEBUG] RESULTADO: OK - Invariante de coherencia de rango validada.");
    }

    @Test
    @DisplayName("Falla si se define solo rango mínimo sin rango máximo")
    void deberiaFallarSiSoloSeDefineUnExtremoDeRango() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiSoloSeDefineUnExtremoDeRango");
        System.out.println("[TEST DEBUG] Intentando definir min=ORO y max=null...");

        // Invalido: solo rango mínimo
        ScrimBuilder builder = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .rango(Rango.ORO, null)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        System.out.println("[TEST DEBUG] RESULTADO: OK - Se exige definir ambos extremos o dejar ambos abiertos.");
    }

    @Test
    @DisplayName("RN-04: Falla si la latencia máxima es cero, negativa o mayor a 500ms")
    void deberiaFallarSiLatenciaEsInvalida() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiLatenciaEsInvalida (RN-04)");

        // 1. Latencia 0 ms
        System.out.println("[TEST DEBUG] Probando latencia = 0 ms (debe ser > 0)...");
        ScrimBuilder builderCero = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .latenciaMax(0)
                .fechaHora(LocalDateTime.now().plusHours(2));
        assertThrows(IllegalArgumentException.class, builderCero::build);

        // 2. Latencia negativa
        System.out.println("[TEST DEBUG] Probando latencia negativa = -10 ms...");
        ScrimBuilder builderNegativo = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .latenciaMax(-10)
                .fechaHora(LocalDateTime.now().plusHours(2));
        assertThrows(IllegalArgumentException.class, builderNegativo::build);

        // 3. Latencia excesiva (> 500ms)
        System.out.println("[TEST DEBUG] Probando latencia excesiva = 600 ms (máximo permitido 500 ms)...");
        ScrimBuilder builderExcesivo = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .latenciaMax(600)
                .fechaHora(LocalDateTime.now().plusHours(2));
        assertThrows(IllegalArgumentException.class, builderExcesivo::build);

        System.out.println("[TEST DEBUG] RESULTADO: OK - Los tres casos de latencia inválida fueron rechazados.");
    }

    @Test
    @DisplayName("Invariante temporal: Falla si la fecha/hora es pasada")
    void deberiaFallarSiFechaEsPasada() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiFechaEsPasada");
        LocalDateTime fechaPasada = LocalDateTime.now().minusMinutes(5);
        System.out.println("[TEST DEBUG] Intentando fecha en el pasado: " + fechaPasada);

        ScrimBuilder builder = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.CS2)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.SA)
                .latenciaMax(50)
                .fechaHora(fechaPasada);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        System.out.println("[TEST DEBUG] RESULTADO: OK - El builder rechaza partidas en fechas pasadas.");
    }

    @Test
    @DisplayName("Falla si la duración en minutos es menor a 15 o mayor a 360")
    void deberiaFallarSiDuracionEsInvalida() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiDuracionEsInvalida");

        // Duración menor a 15 min
        System.out.println("[TEST DEBUG] Probando duración muy corta = 10 minutos (mínimo 15)...");
        ScrimBuilder builderMuyCorta = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.CS2)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.SA)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .duracion(10);
        assertThrows(IllegalArgumentException.class, builderMuyCorta::build);

        // Duración mayor a 360 min
        System.out.println("[TEST DEBUG] Probando duración excesiva = 400 minutos (máximo 360)...");
        ScrimBuilder builderMuyLarga = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.CS2)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.SA)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .duracion(400);
        assertThrows(IllegalArgumentException.class, builderMuyLarga::build);

        System.out.println("[TEST DEBUG] RESULTADO: OK - Límites de duración verificados exitosamente.");
    }

    @Test
    @DisplayName("RN-05: Falla si la suma de roles obligatorios supera el cupo total")
    void deberiaFallarSiSumaDeRolesSuperaCuposTotales() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiSumaDeRolesSuperaCuposTotales (RN-05)");
        System.out.println("[TEST DEBUG] Configurando formato 1v1 (2 cupos totales) pero pidiendo 3 Duelistas...");

        // Formato 1v1 = 2 cupos totales. Pide 3 roles, debe fallar.
        ScrimBuilder builder = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .latenciaMax(70)
                .fechaHora(LocalDateTime.now().plusHours(4))
                .agregarReglaRol(RolJuego.DUELISTA, 3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        System.out.println("[TEST DEBUG] RESULTADO: OK - El builder no permite exigir más roles que cupos.");
    }

    @Test
    @DisplayName("Falla al agregar regla de rol con rol nulo o cantidad menor o igual a cero")
    void deberiaFallarAlAgregarReglaRolInvalida() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarAlAgregarReglaRolInvalida");

        ScrimBuilder builder = new ScrimBuilder();

        // Rol nulo
        System.out.println("[TEST DEBUG] 1. Probando agregarReglaRol con rol = null...");
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class,
                () -> builder.agregarReglaRol(null, 2));
        System.out.println("             Capturada: " + exNull.getMessage());

        // Cantidad cero
        System.out.println("[TEST DEBUG] 2. Probando agregarReglaRol con cantidad = 0...");
        IllegalArgumentException exCero = assertThrows(IllegalArgumentException.class,
                () -> builder.agregarReglaRol(RolJuego.DUELISTA, 0));
        System.out.println("             Capturada: " + exCero.getMessage());

        // Cantidad negativa
        System.out.println("[TEST DEBUG] 3. Probando agregarReglaRol con cantidad = -1...");
        IllegalArgumentException exNegativo = assertThrows(IllegalArgumentException.class,
                () -> builder.agregarReglaRol(RolJuego.DUELISTA, -1));
        System.out.println("             Capturada: " + exNegativo.getMessage());

        System.out.println("[TEST DEBUG] RESULTADO: OK - Validaciones de agregarReglaRol verificadas correctamente.");
    }

    @Test
    @DisplayName("Falla si falta algún campo obligatorio")
    void deberiaFallarSiFaltaCampoObligatorio() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiFaltaCampoObligatorio");

        // Falta juego
        System.out.println("[TEST DEBUG] 1. Probando omisión de juego...");
        ScrimBuilder sinJuego = new ScrimBuilder()
                .organizador(organizadorValido)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2));
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, sinJuego::build);
        System.out.println("             Capturada: " + ex1.getMessage());

        // Falta organizador
        System.out.println("[TEST DEBUG] 2. Probando omisión de organizador...");
        ScrimBuilder sinOrg = new ScrimBuilder()
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2));
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, sinOrg::build);
        System.out.println("             Capturada: " + ex2.getMessage());

        // Falta formato
        System.out.println("[TEST DEBUG] 3. Probando omisión de formato...");
        ScrimBuilder sinFormato = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2));
        IllegalStateException ex3 = assertThrows(IllegalStateException.class, sinFormato::build);
        System.out.println("             Capturada: " + ex3.getMessage());

        // Falta región
        System.out.println("[TEST DEBUG] 4. Probando omisión de región...");
        ScrimBuilder sinRegion = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .fechaHora(LocalDateTime.now().plusHours(2));
        IllegalStateException ex4 = assertThrows(IllegalStateException.class, sinRegion::build);
        System.out.println("             Capturada: " + ex4.getMessage());

        // Falta fechaHora
        System.out.println("[TEST DEBUG] 5. Probando omisión de fecha y hora...");
        ScrimBuilder sinFecha = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.LOL)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS);
        IllegalStateException ex5 = assertThrows(IllegalStateException.class, sinFecha::build);
        System.out.println("             Capturada: " + ex5.getMessage());

        System.out.println("[TEST DEBUG] RESULTADO: OK - Se exige la totalidad de campos obligatorios.");
    }

    @Test
    @DisplayName("Falla si se fuerza una cantidad de cupos inconsistente con el Formato")
    void deberiaFallarSiCuposInconsistentesConFormato() {
        System.out.println("\n[TEST DEBUG] ========================================================");
        System.out.println("[TEST DEBUG] Ejecutando: deberiaFallarSiCuposInconsistentesConFormato");
        System.out.println("[TEST DEBUG] Formato 5v5 espera 10 cupos, pero se forza manualmente cupos=8...");

        // Formato 5v5 espera 10 cupos, forzamos 8 (inconsistente)
        ScrimBuilder builder = new ScrimBuilder()
                .organizador(organizadorValido)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .cupos(8)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        System.out.println("[TEST DEBUG] Excepción capturada correctamente: " + ex.getMessage());
        System.out.println("[TEST DEBUG] RESULTADO: OK - El builder rechaza cupos inconsistentes con el Formato.");
    }
}
