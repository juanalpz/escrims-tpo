package com.escrims.domain.calendar;

import com.escrims.domain.entities.Formato;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.observer.SuplentePromovidoEvent;
import com.escrims.infrastructure.adapter.ICalAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarioSubscriberTest {

    private DomainEventBus bus;
    private Map<UUID, Scrim> scrims;
    private List<String> archivosPublicados;
    private ICalAdapter ical;
    private Scrim scrim;

    @BeforeEach
    void setUp() {
        bus = new DomainEventBus();
        scrims = new HashMap<>();
        archivosPublicados = new ArrayList<>();
        ical = new ICalAdapter((nombre, contenido) -> archivosPublicados.add(nombre), ZoneOffset.UTC, 3);
        bus.subscribe(new CalendarioSubscriber(ical, id -> Optional.ofNullable(scrims.get(id))));

        scrim = new Scrim(null, Juego.CS2, Formato.TRES_VS_TRES, null, null, null, null,
                LocalDateTime.of(2026, 12, 1, 18, 30), 45, null, 6, null);
        scrims.put(scrim.getId(), scrim);
    }

    @Test
    @DisplayName("Al confirmarse el scrim se publica su evento iCal con recordatorio")
    void sincronizaAlConfirmar() {
        bus.publish(new ScrimStateChanged(scrim.getId(), "Lobby armado", "Confirmado"));

        String ics = ical.obtenerIcs(scrim.getId()).orElseThrow();
        assertTrue(ics.contains("DTSTART:20261201T183000Z"));
        assertTrue(ics.contains("DTEND:20261201T191500Z"));
        assertTrue(ics.contains("SUMMARY:Scrim CS2 3v3"));
        assertTrue(ics.contains("TRIGGER:-PT3H"));
        assertEquals(List.of(ICalAdapter.nombreArchivo(scrim.getId())), archivosPublicados);
    }

    @Test
    @DisplayName("Al cancelarse el scrim se cancela su evento iCal")
    void cancelaAlCancelar() {
        bus.publish(new ScrimStateChanged(scrim.getId(), "Lobby armado", "Confirmado"));
        bus.publish(new ScrimStateChanged(scrim.getId(), "Confirmado", "Cancelado"));

        String ics = ical.obtenerIcs(scrim.getId()).orElseThrow();
        assertTrue(ics.contains("STATUS:CANCELLED"));
        assertEquals(2, archivosPublicados.size());
    }

    @Test
    @DisplayName("Ignora otros estados, otros eventos y scrims desconocidos")
    void ignoraLoQueNoCorresponde() {
        bus.publish(new ScrimStateChanged(scrim.getId(), "Buscando jugadores", "Lobby armado"));
        bus.publish(new ScrimStateChanged(scrim.getId(), "Confirmado", "En juego"));
        bus.publish(new ScrimStateChanged(scrim.getId(), "En juego", "Finalizado"));
        bus.publish(new SuplentePromovidoEvent(scrim.getId(), UUID.randomUUID(), UUID.randomUUID()));
        bus.publish(new ScrimStateChanged(UUID.randomUUID(), "Lobby armado", "Confirmado"));

        assertTrue(archivosPublicados.isEmpty());
        assertTrue(ical.obtenerIcs(scrim.getId()).isEmpty());
    }
}
