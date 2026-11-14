package com.escrims.domain.observer;

import java.time.LocalDateTime;

/**
 * Contrato base para todos los eventos de dominio del sistema eScrims.
 */
public interface DomainEvent {
    LocalDateTime ocurridoEn();
}
