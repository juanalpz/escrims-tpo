package com.escrims.domain.moderation;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.EstadoReporte;
import com.escrims.domain.entities.Formato;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Sancion;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.TipoInfraccion;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.ScrimContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del sistema de strikes ({@link PoliticaSanciones}) y de la cadena
 * de moderación AutoResolver → Bot → Moderador humano (CU11).
 */
class ModeracionChainTest {

    private final PoliticaSanciones politica = new PoliticaSanciones();
    private final Map<UUID, Integer> reportesPrevios = new HashMap<>();

    private ReportHandler cadena;
    private Scrim scrim;
    private Usuario reportante;
    private Usuario reportado;

    @BeforeEach
    void setUp() {
        AutoResolverHandler auto = new AutoResolverHandler(u -> reportesPrevios.getOrDefault(u.getId(), 0));
        BotModeratorHandler bot = new BotModeratorHandler(politica);
        HumanModeratorHandler human = new HumanModeratorHandler();
        auto.setSiguiente(bot);
        bot.setSiguiente(human);
        cadena = auto;

        reportante = jugador("reportante");
        reportado = jugador("reportado");
        scrim = new ScrimBuilder()
                .organizador(jugador("organizador"))
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();
        ScrimContext ctx = new ScrimContext(scrim);
        ctx.postular(reportante, RolJuego.DUELISTA);
        ctx.postular(reportado, RolJuego.CENTINELA);
    }

    private Usuario jugador(String username) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmailVerificado(true);
        u.setRangoActual(Rango.PLATINO);
        return u;
    }

    private ReporteConducta reporte() {
        return new ReporteConducta(scrim, reportante, reportado, "Insultos por chat de voz");
    }

    @Test
    @DisplayName("Política: el cooldown escala 0h -> 24h -> 72h -> 7 días con la reincidencia")
    void cooldownEscalaConLaReincidencia() {
        assertEquals(0, politica.horasDeCooldown(1));
        assertEquals(24, politica.horasDeCooldown(2));
        assertEquals(72, politica.horasDeCooldown(3));
        assertEquals(168, politica.horasDeCooldown(4));
        assertEquals(168, politica.horasDeCooldown(9));
        assertThrows(IllegalArgumentException.class, () -> politica.horasDeCooldown(0));
    }

    @Test
    @DisplayName("Política: el primer strike es una advertencia y el segundo bloquea al jugador (RN-06)")
    void primerStrikeAdvierteYSegundoBloquea() {
        LocalDateTime ahora = LocalDateTime.now();

        Sancion primera = politica.aplicar(reportado, TipoInfraccion.NO_SHOW, "no se presentó", ahora);
        assertEquals(1, reportado.getStrikes());
        assertNull(primera.getCooldownHasta());
        assertFalse(reportado.tieneCooldownActivo());

        Sancion segunda = politica.aplicar(reportado, TipoInfraccion.ABANDONO, "dejó la partida", ahora);
        assertEquals(2, reportado.getStrikes());
        assertEquals(ahora.plusHours(24), segunda.getCooldownHasta());
        assertEquals(TipoInfraccion.ABANDONO, segunda.getTipo());
        assertTrue(reportado.tieneCooldownActivo());
        assertFalse(reportado.tieneSancionActiva(ahora.plusHours(25)));
    }

    @Test
    @DisplayName("AutoResolver: desestima el primer reporte contra un jugador sin antecedentes")
    void autoResolverDesestimaPrimerReporte() {
        ReporteConducta reporte = reporte();

        ResultadoModeracion resultado = cadena.manejar(reporte);

        assertTrue(resultado.isResuelto());
        assertNull(resultado.getSancion());
        assertEquals(EstadoReporte.RECHAZADO, reporte.getEstado());
        assertEquals(0, reportado.getStrikes());
    }

    @Test
    @DisplayName("AutoResolver: desestima reportes contra quien no participó del scrim")
    void autoResolverDesestimaReporteImprocedente() {
        Usuario ajeno = jugador("ajeno");
        ajeno.agregarSancion(new Sancion(2, null));
        ReporteConducta reporte = new ReporteConducta(scrim, reportante, ajeno, "Reporte sin relación con el scrim");

        ResultadoModeracion resultado = cadena.manejar(reporte);

        assertEquals(EstadoReporte.RECHAZADO, reporte.getEstado());
        assertTrue(resultado.getResolucion().contains("improcedente"));
        assertEquals(2, ajeno.getStrikes());
    }

    @Test
    @DisplayName("Bot: un jugador sin strikes pero ya reportado recibe su primer strike sin cooldown")
    void botSancionaAlJugadorYaReportado() {
        reportesPrevios.put(reportado.getId(), 1);
        ReporteConducta reporte = reporte();

        ResultadoModeracion resultado = cadena.manejar(reporte);

        assertEquals(EstadoReporte.RESUELTO, reporte.getEstado());
        assertNotNull(resultado.getSancion());
        assertEquals(TipoInfraccion.CONDUCTA, resultado.getSancion().getTipo());
        assertNull(resultado.getSancion().getCooldownHasta());
        assertEquals(1, reportado.getStrikes());
    }

    @Test
    @DisplayName("Bot: la reincidencia leve suma un strike con cooldown")
    void botAplicaCooldownAlReincidente() {
        reportado.agregarSancion(new Sancion(1, null));
        ReporteConducta reporte = reporte();

        ResultadoModeracion resultado = cadena.manejar(reporte);

        assertEquals(EstadoReporte.RESUELTO, reporte.getEstado());
        assertEquals(2, reportado.getStrikes());
        assertNotNull(resultado.getSancion().getCooldownHasta());
        assertTrue(reportado.tieneCooldownActivo());
    }

    @Test
    @DisplayName("Moderador humano: con 3 o más strikes el reporte se escala a revisión sin sanción automática")
    void reincidenteGraveSeEscalaARevisionHumana() {
        reportado.agregarSancion(new Sancion(3, null));
        ReporteConducta reporte = reporte();

        ResultadoModeracion resultado = cadena.manejar(reporte);

        assertTrue(resultado.isResuelto());
        assertNull(resultado.getSancion());
        assertEquals(EstadoReporte.EN_REVISION, reporte.getEstado());
        assertEquals(3, reportado.getStrikes());
    }
}
