package com.escrims.domain.calendar;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.observer.Subscriber;
import com.escrims.domain.state.CanceladoState;
import com.escrims.domain.state.ConfirmadoState;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * «Observer» Mantiene el calendario sincronizado con el ciclo de vida del
 * scrim: al pasar a Confirmado publica el evento (con su recordatorio) y al
 * pasar a Cancelado lo da de baja
 */
public class CalendarioSubscriber implements Subscriber {

    private static final String CONFIRMADO = new ConfirmadoState().getNombreEstado();
    private static final String CANCELADO = new CanceladoState().getNombreEstado();

    private final CalendarioService calendario;
    private final Function<UUID, Optional<Scrim>> buscadorDeScrims;

    /**
     * @param calendario       servicio de calendario (ej. {@code ICalAdapter})
     * @param buscadorDeScrims resuelve el scrim a partir del id del evento
     *                         (ej. {@code scrimRepository::findById})
     */
    public CalendarioSubscriber(CalendarioService calendario,
                                Function<UUID, Optional<Scrim>> buscadorDeScrims) {
        this.calendario = Objects.requireNonNull(calendario, "calendario");
        this.buscadorDeScrims = Objects.requireNonNull(buscadorDeScrims, "buscadorDeScrims");
    }

    @Override
    public void onEvent(DomainEvent event) {
        if (!(event instanceof ScrimStateChanged cambio)) {
            return;
        }
        boolean confirmado = CONFIRMADO.equals(cambio.nuevoEstado());
        boolean cancelado = CANCELADO.equals(cambio.nuevoEstado());
        if (!confirmado && !cancelado) {
            return;
        }
        Optional<Scrim> scrim = buscadorDeScrims.apply(cambio.scrimId());
        if (scrim.isEmpty()) {
            return;
        }
        if (confirmado) {
            calendario.sincronizar(scrim.get());
        } else {
            calendario.cancelar(scrim.get());
        }
    }
}
