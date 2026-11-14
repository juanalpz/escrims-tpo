package com.escrims.domain.observer;

/**
 * «Observer» Contrato que deben implementar todos los suscriptores interesados
 * en reaccionar a eventos de dominio (ej. GestorNotificaciones).
 */
public interface Subscriber {
    void onEvent(DomainEvent event);
}
