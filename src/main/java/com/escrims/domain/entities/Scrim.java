package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad central del dominio: representa una partida amistosa (scrim) con toda
 * su configuración y estado operativo.
 */
public class Scrim {

    // ── Configuración inmutable ────────────────────────────────

    private final UUID id;
    private final Usuario organizador;
    private final Juego juego;
    private final Formato formato;
    private final Region region;
    private final Rango rangoMin;
    private final Rango rangoMax;
    private final Integer latenciaMax;
    private final LocalDateTime fechaHora;
    private final Integer duracion;
    private final Modalidad modalidad;
    private final int cupos;
    private final Map<RolJuego, Integer> reglasRoles;

    // ── Estado operativo mutable ───────────────────────────────

    private final Set<Usuario> jugadores = new LinkedHashSet<>();
    private Set<Postulacion> postulaciones = new HashSet<>();
    private Set<Confirmacion> confirmaciones = new HashSet<>();
    private Object estado; // ScrimState — se tipará al implementar el patrón State

    // ── Constructores ──────────────────────────────────────────

    /**
     * Constructor completo — usado por {@code ScrimBuilder}.
     */
    public Scrim(Usuario organizador, Juego juego, Formato formato, Region region,
                 Rango rangoMin, Rango rangoMax, Integer latenciaMax,
                 LocalDateTime fechaHora, Integer duracion, Modalidad modalidad,
                 int cupos, Map<RolJuego, Integer> reglasRoles) {
        this.id = UUID.randomUUID();
        this.organizador = organizador;
        this.juego = Objects.requireNonNull(juego, "juego");
        this.formato = Objects.requireNonNull(formato, "formato");
        this.region = region;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fechaHora = fechaHora;
        this.duracion = duracion;
        this.modalidad = modalidad;
        this.cupos = cupos;

        if (reglasRoles == null || reglasRoles.isEmpty()) {
            this.reglasRoles = new EnumMap<>(RolJuego.class);
        } else {
            this.reglasRoles = new EnumMap<>(reglasRoles);
        }
    }

    /**
     * Constructor para tests con rango mínimo y máximo explícitos.
     */
    public Scrim(Juego juego, Formato formato, Region region,
                 Rango rangoMin, Rango rangoMax, int latenciaMax) {
        this(null, juego, formato, region, rangoMin, rangoMax,
                latenciaMax > 0 ? latenciaMax : null,
                null, null, null, formato.getCuposTotales(), null);
    }

    /**
     * Constructor para tests con rango único (mínimo = máximo).
     */
    public Scrim(Juego juego, Formato formato, Region region,
                 Rango rangoUnico, int latenciaMax) {
        this(null, juego, formato, region, rangoUnico, rangoUnico,
                latenciaMax > 0 ? latenciaMax : null,
                null, null, null, formato.getCuposTotales(), null);
    }

    // ── Getters de configuración ───────────────────────────────

    public UUID getId() { return id; }
    public Usuario getOrganizador() { return organizador; }
    public Juego getJuego() { return juego; }
    public Formato getFormato() { return formato; }
    public Region getRegion() { return region; }
    public Rango getRangoMin() { return rangoMin; }
    public Rango getRangoMax() { return rangoMax; }
    public Integer getLatenciaMax() { return latenciaMax; }
    public int getLatenciaMaxima() { return latenciaMax != null ? latenciaMax : 0; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Integer getDuracion() { return duracion; }
    public Modalidad getModalidad() { return modalidad; }
    public int getCupos() { return cupos; }
    public Map<RolJuego, Integer> getReglasRoles() { return Collections.unmodifiableMap(reglasRoles); }

    /**
     * Devuelve el rango cuando rangoMin == rangoMax, o null si está abierto.
     */
    public Rango getRango() {
        return Objects.equals(rangoMin, rangoMax) ? rangoMin : null;
    }

    // ── Verificación de MMR en rango (usado por estrategias) ───

    /**
     * Indica si un valor de MMR está dentro del rango [rangoMin, rangoMax].
     * Si ambos extremos son {@code null}, admite cualquier nivel (scrim abierto).
     */
    public boolean mmrDentroDeRango(int mmr) {
        if (rangoMin == null && rangoMax == null) {
            return true;
        }
        int min = rangoMin != null ? rangoMin.getMmrMin() : 0;
        int max = rangoMax != null ? rangoMax.getMmrMax() : Integer.MAX_VALUE;
        return mmr >= min && mmr <= max;
    }

    // ── Jugadores ──────────────────────────────────────────────

    public Set<Usuario> getJugadores() {
        return Collections.unmodifiableSet(jugadores);
    }

    /**
     * Agrega un jugador al scrim. Falla si no hay cupos o si el jugador
     * ya está inscrito (uso de {@code Set} para evitar duplicados).
     */
    public void agregarJugador(Usuario usuario) {
        Objects.requireNonNull(usuario, "El usuario no puede ser nulo.");
        if (cuposDisponibles() == 0) {
            throw new IllegalStateException("El scrim no tiene cupos disponibles.");
        }
        if (!jugadores.add(usuario)) {
            throw new IllegalStateException("El jugador ya está en el scrim.");
        }
    }

    public int cuposDisponibles() {
        return cupos - jugadores.size();
    }

    // ── Roles buscados (derivados de reglasRoles) ──────────────

    /** Roles que el scrim necesita cubrir. Vacío si no exige roles. */
    public Set<RolJuego> getRolesBuscados() {
        return Collections.unmodifiableSet(reglasRoles.keySet());
    }

    /** Agrega un rol buscado con cantidad por defecto 1 (útil en tests). */
    public void buscarRol(RolJuego rol) {
        reglasRoles.putIfAbsent(rol, 1);
    }

    // ── Postulaciones y Confirmaciones ─────────────────────────

    public Set<Postulacion> getPostulaciones() {
        return Collections.unmodifiableSet(postulaciones);
    }

    public void setPostulaciones(Set<Postulacion> postulaciones) {
        this.postulaciones = postulaciones;
    }

    public Set<Confirmacion> getConfirmaciones() {
        return Collections.unmodifiableSet(confirmaciones);
    }

    public void setConfirmaciones(Set<Confirmacion> confirmaciones) {
        this.confirmaciones = confirmaciones;
    }

    // ── Estado del ciclo de vida (ScrimState) ──────────────────

    public Object getEstado() {
        return estado;
    }

    public void setEstado(Object estado) {
        this.estado = estado;
    }

    // ── equals / hashCode ──────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Scrim s && id.equals(s.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
