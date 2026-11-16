package com.escrims.domain.entities;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa una búsqueda favorita guardada por un usuario para recibir alertas cuando se publiquen scrims coincidentes.
 */
public class BusquedaGuardada {

    private final UUID id;
    private final DatosScrim filtros;
    private final Usuario usuario;

    /**
     * Inicializa una búsqueda guardada asociando los criterios de filtrado con el usuario interesado.
     *
     * @param filtros criterios de búsqueda configurados para evaluar coincidencias.
     * @param usuario usuario propietario de la alerta de búsqueda.
     * @throws IllegalArgumentException si los filtros o el usuario son nulos.
     */
    public BusquedaGuardada(DatosScrim filtros, Usuario usuario) {
        if (filtros == null || usuario == null) {
            throw new IllegalArgumentException("Filtros y usuario son obligatorios.");
        }
        this.id = UUID.randomUUID();
        this.filtros = filtros;
        this.usuario = usuario;
    }

    public UUID getId() { return id; }
    public DatosScrim getFiltros() { return filtros; }
    public Usuario getUsuario() { return usuario; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof BusquedaGuardada b && id.equals(b.id));
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
