package com.escrims.domain.calendar;

import com.escrims.domain.entities.Scrim;

/**
 * «Target» Contrato del dominio para sincronizar scrims con un calendario
 * externo. Lo implementa {@code ICalAdapter}.
 */
public interface CalendarioService {

    /** Crea o actualiza el evento de calendario del scrim. */
    void sincronizar(Scrim scrim);

    /** Marca como cancelado el evento de calendario del scrim. */
    void cancelar(Scrim scrim);
}
