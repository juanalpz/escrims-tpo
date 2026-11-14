package com.escrims.infrastructure.notification;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.EstadoNotificacion;
import com.escrims.domain.notification.Notificacion;
import com.escrims.domain.notification.Notifier;
import com.escrims.domain.notification.NotifierFactory;
import com.escrims.infrastructure.adapter.DiscordAdapter;
import com.escrims.infrastructure.adapter.FirebasePushAdapter;
import com.escrims.infrastructure.adapter.SendGridAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotifierFactoryTest {

    private static final ProdNotifierConfig CONFIG = new ProdNotifierConfig(
            "https://discord.test/webhook", "sg-key-de-prueba", "no-reply@escrims.test",
            "escrims-test", () -> "token-de-prueba");

    @Test
    @DisplayName("DevNotifierFactory crea un LogNotifier por canal")
    void devCreaFakesPorCanal() {
        NotifierFactory factory = new DevNotifierFactory();

        assertInstanceOf(LogNotifier.class, factory.createPush());
        assertInstanceOf(LogNotifier.class, factory.createEmail());
        assertInstanceOf(LogNotifier.class, factory.createChat());
        assertEquals(Canal.PUSH, factory.createPush().canal());
        assertEquals(Canal.EMAIL, factory.createEmail().canal());
        assertEquals(Canal.CHAT, factory.createChat().canal());
    }

    @Test
    @DisplayName("Los fakes de dev registran lo enviado y marcan la notificación como ENVIADA")
    void devRegistraLoEnviado() {
        DevNotifierFactory factory = new DevNotifierFactory();
        Notificacion n = new Notificacion("LOBBY_ARMADO", Canal.EMAIL, "jugador@test.com",
                "Lobby completo", "Tu scrim ya tiene todos los cupos.");

        factory.createEmail().send(n);

        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        assertEquals(List.of(n), factory.createEmail().getEnviadas());
        assertTrue(factory.createPush().getEnviadas().isEmpty());
    }

    @Test
    @DisplayName("Un notifier rechaza notificaciones de otro canal")
    void rechazaCanalIncorrecto() {
        Notifier push = new DevNotifierFactory().createPush();
        Notificacion email = new Notificacion("X", Canal.EMAIL, "a@test.com", "t", "p");

        assertThrows(IllegalArgumentException.class, () -> push.send(email));
        assertEquals(EstadoNotificacion.PENDIENTE, email.getEstado());
    }

    @Test
    @DisplayName("ProdNotifierFactory crea los adaptadores de los proveedores reales")
    void prodCreaAdaptadoresReales() {
        NotifierFactory factory = new ProdNotifierFactory(CONFIG, (url, headers, body) -> 200);

        assertInstanceOf(FirebasePushAdapter.class, factory.createPush());
        assertInstanceOf(SendGridAdapter.class, factory.createEmail());
        assertInstanceOf(DiscordAdapter.class, factory.createChat());
    }

    @Test
    @DisplayName("create(canal) devuelve el notifier del canal pedido en ambos entornos")
    void createPorCanal() {
        List<NotifierFactory> factories = List.of(
                new DevNotifierFactory(),
                new ProdNotifierFactory(CONFIG, (url, headers, body) -> 200));

        for (NotifierFactory factory : factories) {
            for (Canal canal : Canal.values()) {
                assertEquals(canal, factory.create(canal).canal());
            }
            assertThrows(IllegalArgumentException.class, () -> factory.create(null));
        }
    }

    @Test
    @DisplayName("El código cliente funciona igual con cualquiera de las dos factories")
    void clienteIndependienteDelEntorno() {
        List<String> urls = new ArrayList<>();
        List<NotifierFactory> factories = List.of(
                new DevNotifierFactory(),
                new ProdNotifierFactory(CONFIG, (url, headers, body) -> {
                    urls.add(url);
                    return 200;
                }));

        for (NotifierFactory factory : factories) {
            Notificacion n = new Notificacion("CONFIRMADO", Canal.CHAT, null, "Scrim confirmado", "A jugar");
            factory.create(n.getCanal()).send(n);
            assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        }
        assertEquals(List.of("https://discord.test/webhook"), urls);
    }

    @Test
    @DisplayName("ProdNotifierFactory falla si falta la configuración de un proveedor")
    void prodFallaSinConfiguracion() {
        ProdNotifierConfig incompleta = new ProdNotifierConfig(null, " ", null, null, () -> "t");
        NotifierFactory factory = new ProdNotifierFactory(incompleta, (url, headers, body) -> 200);

        assertThrows(IllegalArgumentException.class, factory::createChat);
        assertThrows(IllegalArgumentException.class, factory::createEmail);
        assertThrows(IllegalArgumentException.class, factory::createPush);
    }

    @Test
    @DisplayName("La configuración de producción no expone credenciales en toString")
    void configNoExponeCredenciales() {
        String texto = CONFIG.toString();

        assertFalse(texto.contains("sg-key-de-prueba"));
        assertFalse(texto.contains("webhook"));
    }
}
