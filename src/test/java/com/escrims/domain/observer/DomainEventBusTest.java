package com.escrims.domain.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainEventBusTest {

    private DomainEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DomainEventBus();
    }

    @Test
    @DisplayName("Notifica a todos los suscriptores registrados cuando se publica un evento")
    void deberiaNotificarATodosLosSuscriptores() {
        List<DomainEvent> recibidosSub1 = new ArrayList<>();
        List<DomainEvent> recibidosSub2 = new ArrayList<>();

        Subscriber sub1 = recibidosSub1::add;
        Subscriber sub2 = recibidosSub2::add;

        eventBus.subscribe(sub1);
        eventBus.subscribe(sub2);

        UUID scrimId = UUID.randomUUID();
        ScrimStateChanged evento = new ScrimStateChanged(scrimId, "Buscando jugadores", "Lobby armado");
        eventBus.publish(evento);

        assertEquals(1, recibidosSub1.size());
        assertEquals(1, recibidosSub2.size());
        assertEquals(evento, recibidosSub1.get(0));
    }

    @Test
    @DisplayName("Evita suscripciones duplicadas y deja de notificar tras unsubscribe")
    void deberiaEvitarDuplicadosYPermitirDesuscribir() {
        List<DomainEvent> recibidos = new ArrayList<>();
        Subscriber sub = recibidos::add;

        eventBus.subscribe(sub);
        eventBus.subscribe(sub); // duplicado ignorado por Set
        assertEquals(1, eventBus.getSubscribers().size());

        eventBus.publish(new ScrimStateChanged(UUID.randomUUID(), "Lobby armado", "Confirmado"));
        assertEquals(1, recibidos.size());

        eventBus.unsubscribe(sub);
        eventBus.publish(new ScrimStateChanged(UUID.randomUUID(), "Confirmado", "En juego"));
        assertEquals(1, recibidos.size(), "No debe recibir eventos luego de desuscribirse.");
    }

    @Test
    @DisplayName("Valida parámetros nulos al suscribir o publicar")
    void deberiaRechazarNulos() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(null));
        assertThrows(IllegalArgumentException.class, () -> eventBus.publish(null));
    }
}
