package com.escrims.infrastructure.adapter;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.EstadoNotificacion;
import com.escrims.domain.notification.Notificacion;
import com.escrims.domain.notification.NotificacionException;
import com.escrims.infrastructure.http.HttpTransport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotifierAdaptersTest {

    /** Transporte falso: guarda el último request y responde el status configurado. */
    private static class FakeTransport implements HttpTransport {
        int status = 200;
        boolean sinConexion;
        int llamadas;
        String url;
        Map<String, String> headers;
        String body;

        @Override
        public int postJson(String url, Map<String, String> headers, String body) {
            llamadas++;
            if (sinConexion) {
                throw new UncheckedIOException(new IOException("sin red"));
            }
            this.url = url;
            this.headers = headers;
            this.body = body;
            return status;
        }
    }

    private FakeTransport transport;

    @BeforeEach
    void setUp() {
        transport = new FakeTransport();
    }

    @Test
    @DisplayName("DiscordAdapter publica el mensaje en el webhook mencionando al destinatario")
    void discordTraduceAWebhook() {
        transport.status = 204;
        DiscordAdapter adapter = new DiscordAdapter(transport, "https://discord.test/webhook");
        Notificacion n = new Notificacion("LOBBY_ARMADO", Canal.CHAT, "<@123>",
                "Lobby completo", "Confirmá tu \"asistencia\"\nantes de las 20hs");

        adapter.send(n);

        assertEquals("https://discord.test/webhook", transport.url);
        assertEquals(
                "{\"content\":\"<@123> **Lobby completo**\\nConfirmá tu \\\"asistencia\\\"\\nantes de las 20hs\"}",
                transport.body);
        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
    }

    @Test
    @DisplayName("DiscordAdapter no requiere destinatario")
    void discordSinDestinatario() {
        new DiscordAdapter(transport, "https://discord.test/webhook")
                .send(new Notificacion("X", Canal.CHAT, null, "Título", "Texto"));

        assertEquals("{\"content\":\"**Título**\\nTexto\"}", transport.body);
    }

    @Test
    @DisplayName("SendGridAdapter arma el mail con remitente, destinatario, asunto y API key")
    void sendGridTraduceAMailSend() {
        transport.status = 202;
        SendGridAdapter adapter = new SendGridAdapter(transport, "sg-key", "no-reply@escrims.test");
        Notificacion n = new Notificacion("CONFIRMADO", Canal.EMAIL, "jugador@test.com",
                "Scrim confirmado", "Todos confirmaron.");

        adapter.send(n);

        assertEquals("https://api.sendgrid.com/v3/mail/send", transport.url);
        assertEquals("Bearer sg-key", transport.headers.get("Authorization"));
        assertEquals("{\"personalizations\":[{\"to\":[{\"email\":\"jugador@test.com\"}]}],"
                + "\"from\":{\"email\":\"no-reply@escrims.test\"},"
                + "\"subject\":\"Scrim confirmado\","
                + "\"content\":[{\"type\":\"text/plain\",\"value\":\"Todos confirmaron.\"}]}",
                transport.body);
        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
    }

    @Test
    @DisplayName("FirebasePushAdapter envía al token del dispositivo con el token OAuth vigente")
    void firebaseTraduceAFcm() {
        String[] token = {"token-1"};
        FirebasePushAdapter adapter = new FirebasePushAdapter(transport, "escrims-test", () -> token[0]);
        Notificacion n = new Notificacion("EN_JUEGO", Canal.PUSH, "device-abc", "Empezó tu scrim", "Entrá al lobby");

        adapter.send(n);

        assertEquals("https://fcm.googleapis.com/v1/projects/escrims-test/messages:send", transport.url);
        assertEquals("Bearer token-1", transport.headers.get("Authorization"));
        assertEquals("{\"message\":{\"token\":\"device-abc\","
                + "\"notification\":{\"title\":\"Empezó tu scrim\",\"body\":\"Entrá al lobby\"},"
                + "\"data\":{\"tipo\":\"EN_JUEGO\"}}}",
                transport.body);

        token[0] = "token-2";
        adapter.send(new Notificacion("EN_JUEGO", Canal.PUSH, "device-abc", "t", "p"));
        assertEquals("Bearer token-2", transport.headers.get("Authorization"));
    }

    @Test
    @DisplayName("Si el proveedor responde con error, la notificación queda FALLIDA")
    void errorDelProveedor() {
        transport.status = 500;
        SendGridAdapter adapter = new SendGridAdapter(transport, "sg-key", "no-reply@escrims.test");
        Notificacion n = new Notificacion("X", Canal.EMAIL, "a@test.com", "t", "p");

        NotificacionException ex = assertThrows(NotificacionException.class, () -> adapter.send(n));

        assertTrue(ex.getMessage().contains("500"));
        assertEquals(EstadoNotificacion.FALLIDA, n.getEstado());
    }

    @Test
    @DisplayName("Si no hay conexión, la notificación queda FALLIDA y se conserva la causa")
    void errorDeConexion() {
        transport.sinConexion = true;
        DiscordAdapter adapter = new DiscordAdapter(transport, "https://discord.test/webhook");
        Notificacion n = new Notificacion("X", Canal.CHAT, null, "t", "p");

        NotificacionException ex = assertThrows(NotificacionException.class, () -> adapter.send(n));

        assertInstanceOf(UncheckedIOException.class, ex.getCause());
        assertEquals(EstadoNotificacion.FALLIDA, n.getEstado());
    }

    @Test
    @DisplayName("Los adaptadores no llaman al proveedor si el canal o el destinatario son inválidos")
    void validaAntesDeEnviar() {
        SendGridAdapter email = new SendGridAdapter(transport, "sg-key", "no-reply@escrims.test");
        FirebasePushAdapter push = new FirebasePushAdapter(transport, "escrims-test", () -> "t");

        assertThrows(IllegalArgumentException.class,
                () -> email.send(new Notificacion("X", Canal.PUSH, "a@test.com", "t", "p")));
        assertThrows(IllegalArgumentException.class,
                () -> email.send(new Notificacion("X", Canal.EMAIL, " ", "t", "p")));
        assertThrows(IllegalArgumentException.class,
                () -> push.send(new Notificacion("X", Canal.PUSH, null, "t", "p")));
        assertEquals(0, transport.llamadas);
    }
}
