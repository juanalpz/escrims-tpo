package com.escrims.infrastructure.notification;

import java.util.function.Supplier;

/**
 * Credenciales y parametros de los proveedores reales. Los valores deben
 * venir de la configuracion del entorno, nunca estar escritos en el codigo
 *
 * @param discordWebhookUrl   URL del webhook del canal de Discord
 * @param sendGridApiKey      API key de SendGrid
 * @param emailRemitente      direccion "from" verificada en SendGrid
 * @param firebaseProjectId   id del proyecto de Firebase
 * @param firebaseAccessToken proveedor del token OAuth2 de la cuenta de servicio
 */
public record ProdNotifierConfig(
        String discordWebhookUrl,
        String sendGridApiKey,
        String emailRemitente,
        String firebaseProjectId,
        Supplier<String> firebaseAccessToken) {

    /**
     * Lee la configuracion de las variables de entorno DISCORD_WEBHOOK_URL,
     * SENDGRID_API_KEY, EMAIL_REMITENTE, FIREBASE_PROJECT_ID y
     * FIREBASE_ACCESS_TOKEN.
     */
    public static ProdNotifierConfig desdeEntorno() {
        return new ProdNotifierConfig(
                System.getenv("DISCORD_WEBHOOK_URL"),
                System.getenv("SENDGRID_API_KEY"),
                System.getenv("EMAIL_REMITENTE"),
                System.getenv("FIREBASE_PROJECT_ID"),
                () -> System.getenv("FIREBASE_ACCESS_TOKEN"));
    }

    /** No expone credenciales en logs. */
    @Override
    public String toString() {
        return "ProdNotifierConfig[firebaseProjectId=" + firebaseProjectId + "]";
    }
}
