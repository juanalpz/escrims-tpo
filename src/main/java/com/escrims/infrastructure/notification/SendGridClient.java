package com.escrims.infrastructure.notification;

/**
 * Implementa el Adaptee del patrón Adapter, representando un cliente
 * de SendGrid para el envío de correos electrónicos transaccionales.
 */
public class SendGridClient {

    private final String apiKey;

    /**
     * Inicializa el cliente con una clave de API simulada.
     */
    public SendGridClient() {
        this.apiKey = "stub-api-key";
    }

    /**
     * Inicializa el cliente con una clave de API personalizada.
     *
     * @param apiKey clave de autenticación para el servicio de SendGrid
     */
    public SendGridClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Simula el envío de un correo electrónico mediante SendGrid.
     *
     * @param destinatario dirección de correo electrónico del destinatario
     * @param mensaje contenido del correo electrónico que se desea enviar
     * @return true como resultado simulado de un envío exitoso
     */
    public boolean enviarEmail(String destinatario, String mensaje) {
        // Integración real con SendGrid (stub)
        return true;
    }

    /**
     * Obtiene la clave de API configurada para el cliente.
     *
     * @return clave de API de SendGrid
     */
    public String getApiKey() { return apiKey; }
}
