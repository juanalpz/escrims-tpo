package com.escrims.infrastructure.notification;

/**
 * Implementa el Adaptee del patrón Adapter, representando un cliente
 * de Firebase Cloud Messaging para el envío de notificaciones push.
 */
public class FirebaseClient {

    /**
     * Simula el envío de una notificación push a un dispositivo mediante Firebase.
     *
     * @param token identificador del dispositivo destinatario
     * @param mensaje contenido de la notificación que se desea enviar
     * @return true como resultado simulado de un envío exitoso
     */
    public boolean enviarPush(String token, String mensaje) {
        // Integración real con Firebase (stub)
        return true;
    }
}
