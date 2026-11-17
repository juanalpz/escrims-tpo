package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.Canal;
import com.escrims.domain.entities.ResultadoEnvio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotifierFactoryTest {

    @Test
    @DisplayName("DevNotifierFactory crea un LogNotifier para cualquier canal")
    void devCreaLogNotifierPorCanal() {
        NotifierFactory factory = new DevNotifierFactory();

        for (Canal canal : Canal.values()) {
            assertInstanceOf(LogNotifier.class, factory.crearNotifier(canal));
        }
    }

    @Test
    @DisplayName("LogNotifier registra el último envío y devuelve un resultado exitoso")
    void devRegistraLoEnviado() {
        LogNotifier notifier = (LogNotifier) new DevNotifierFactory().crearNotifier(Canal.EMAIL);

        ResultadoEnvio resultado = notifier.enviar("Tu scrim ya tiene todos los cupos.", "jugador@test.com");

        assertTrue(resultado.isExitoso());
        assertEquals(1, resultado.getIntentos());
        assertEquals("Tu scrim ya tiene todos los cupos.", notifier.getUltimoMensaje());
        assertEquals("jugador@test.com", notifier.getUltimoDestinatario());
    }

    @Test
    @DisplayName("LogNotifier no registra nada antes del primer envío")
    void devSinEnviosPrevios() {
        LogNotifier notifier = new LogNotifier();

        assertNull(notifier.getUltimoMensaje());
        assertNull(notifier.getUltimoDestinatario());
        assertTrue(notifier.reintentar(3).isExitoso());
    }

    @Test
    @DisplayName("ProdNotifierFactory crea el adaptador del proveedor real de cada canal")
    void prodCreaAdaptadoresReales() {
        NotifierFactory factory = new ProdNotifierFactory();

        assertInstanceOf(FirebaseAdapter.class, factory.crearNotifier(Canal.PUSH));
        assertInstanceOf(SendGridAdapter.class, factory.crearNotifier(Canal.EMAIL));
        assertInstanceOf(DiscordAdapter.class, factory.crearNotifier(Canal.DISCORD));
        assertInstanceOf(ICalAdapter.class, factory.crearNotifier(Canal.ICAL));
    }

    @Test
    @DisplayName("ProdNotifierFactory rechaza un canal nulo")
    void prodRechazaCanalNulo() {
        assertThrows(NullPointerException.class, () -> new ProdNotifierFactory().crearNotifier(null));
    }
}
