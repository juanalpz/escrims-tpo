package com.escrims.infrastructure.notification;

/**
 * Implementa el Adaptee del patrón Adapter, representando un cliente
 * de Discord para la publicación de mensajes mediante webhooks o bots.
 */
public class DiscordClient {

    private final String endpoint;

    /**
     * Inicializa el cliente con el endpoint predeterminado de Discord.
     */
    public DiscordClient() {
        this.endpoint = "https://discord.com/api/webhooks/stub";
    }

    /**
     * Inicializa el cliente con un endpoint personalizado de Discord.
     *
     * @param endpoint dirección del servicio de Discord
     */
    public DiscordClient(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Simula la publicación de un mensaje en un canal de Discord.
     *
     * @param mensaje contenido del mensaje que se desea publicar
     * @param canal identificador del canal de destino
     * @return true como resultado simulado de una publicación exitosa
     */
    public boolean publicar(String mensaje, String canal) {
        // Integración real con Discord (stub)
        return true;
    }

    /**
     * Obtiene el endpoint configurado para el cliente de Discord.
     *
     * @return dirección del endpoint de Discord
     */
    public String getEndpoint() { return endpoint; }
}
