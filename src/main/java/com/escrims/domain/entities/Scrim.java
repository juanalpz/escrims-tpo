package com.escrims.domain.entities;

import com.escrims.domain.state.ScrimState;

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
 * Entidad central del dominio que representa un scrim, su configuración,
 * participantes y ciclo de vida gestionado mediante el patrón State.
 */
public class Scrim {

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

    private final Set<Usuario> jugadores = new LinkedHashSet<>();
    private Set<Postulacion> postulaciones = new HashSet<>();
    private Set<Confirmacion> confirmaciones = new HashSet<>();
    private ScrimState estado;

    /**
     * Crea un scrim con su configuración completa, mediante el patrón Builder.
     *
     * @param organizador usuario organizador del scrim
     * @param juego juego en el que se disputará la partida
     * @param formato cantidad de jugadores por equipo
     * @param region región del servidor de juego
     * @param rangoMin rango competitivo mínimo permitido
     * @param rangoMax rango competitivo máximo permitido
     * @param latenciaMax latencia máxima permitida, si está definida
     * @param fechaHora fecha y hora programadas para la partida
     * @param duracion duración estimada de la partida
     * @param modalidad modalidad de juego seleccionada
     * @param cupos cantidad máxima de participantes
     * @param reglasRoles distribución de cupos requeridos por rol
     * @throws NullPointerException si el juego o el formato son nulos
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
     * Crea un scrim de prueba con límites competitivos explícitos.
     *
     * @param juego juego del scrim
     * @param formato formato de la partida
     * @param region región del servidor
     * @param rangoMin rango competitivo mínimo
     * @param rangoMax rango competitivo máximo
     * @param latenciaMax latencia máxima permitida
     * @throws NullPointerException si el juego o el formato son nulos
     */
    public Scrim(Juego juego, Formato formato, Region region,
                 Rango rangoMin, Rango rangoMax, int latenciaMax) {
        this(null, juego, formato, region, rangoMin, rangoMax,
                latenciaMax > 0 ? latenciaMax : null,
                null, null, null, formato.getCuposTotales(), null);
    }

    /**
     * Crea un scrim de prueba limitado a un único rango competitivo.
     *
     * @param juego juego del scrim
     * @param formato formato de la partida
     * @param region región del servidor
     * @param rangoUnico rango competitivo permitido
     * @param latenciaMax latencia máxima permitida
     * @throws NullPointerException si el juego o el formato son nulos
     */
    public Scrim(Juego juego, Formato formato, Region region,
                 Rango rangoUnico, int latenciaMax) {
        this(null, juego, formato, region, rangoUnico, rangoUnico,
                latenciaMax > 0 ? latenciaMax : null,
                null, null, null, formato.getCuposTotales(), null);
    }

    /**
     * Obtiene el identificador único del scrim.
     *
     * @return identificador del scrim
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el usuario organizador del scrim.
     *
     * @return usuario organizador
     */
    public Usuario getOrganizador() { return organizador; }

    /**
     * Obtiene el juego asociado al scrim.
     *
     * @return juego seleccionado
     */
    public Juego getJuego() { return juego; }

    /**
     * Obtiene el formato de la partida.
     *
     * @return formato del scrim
     */
    public Formato getFormato() { return formato; }

    /**
     * Obtiene la región del servidor de juego.
     *
     * @return región configurada
     */
    public Region getRegion() { return region; }

    /**
     * Obtiene el rango competitivo mínimo permitido.
     *
     * @return rango mínimo o null si no está definido
     */
    public Rango getRangoMin() { return rangoMin; }

    /**
     * Obtiene el rango competitivo máximo permitido.
     *
     * @return rango máximo o null si no está definido
     */
    public Rango getRangoMax() { return rangoMax; }

    /**
     * Obtiene la latencia máxima configurada.
     *
     * @return latencia máxima o null si no está definida
     */
    public Integer getLatenciaMax() { return latenciaMax; }

    /**
     * Obtiene la latencia máxima como valor primitivo.
     *
     * @return latencia máxima configurada o cero si no está definida
     */
    public int getLatenciaMaxima() { return latenciaMax != null ? latenciaMax : 0; }

    /**
     * Obtiene la fecha y hora programadas para el scrim.
     *
     * @return fecha y hora de la partida
     */
    public LocalDateTime getFechaHora() { return fechaHora; }

    /**
     * Obtiene la duración estimada de la partida.
     *
     * @return duración configurada
     */
    public Integer getDuracion() { return duracion; }

    /**
     * Obtiene la modalidad de juego del scrim.
     *
     * @return modalidad seleccionada
     */
    public Modalidad getModalidad() { return modalidad; }

    /**
     * Obtiene la capacidad máxima de participantes del scrim.
     *
     * @return cantidad total de cupos
     */
    public int getCupos() { return cupos; }

    /**
     * Obtiene las cantidades de jugadores requeridas por rol.
     *
     * @return mapa no modificable de roles y cupos requeridos
     */
    public Map<RolJuego, Integer> getReglasRoles() { return Collections.unmodifiableMap(reglasRoles); }

    /**
     * Obtiene el rango competitivo único cuando ambos límites coinciden.
     *
     * @return rango competitivo común o null si los límites son distintos
     */
    public Rango getRango() {
        return Objects.equals(rangoMin, rangoMax) ? rangoMin : null;
    }

    /**
     * Verifica si un valor de MMR pertenece al intervalo competitivo
     * permitido, admitiendo límites abiertos cuando no están definidos.
     *
     * @param mmr puntuación competitiva que se desea evaluar
     * @return true si el MMR está dentro de los límites; false en caso contrario
     */
    public boolean mmrDentroDeRango(int mmr) {
        if (rangoMin == null && rangoMax == null) {
            return true;
        }
        int min = rangoMin != null ? rangoMin.getMmrMin() : 0;
        int max = rangoMax != null ? rangoMax.getMmrMax() : Integer.MAX_VALUE;
        return mmr >= min && mmr <= max;
    }

    /**
     * Obtiene los jugadores incorporados al scrim.
     *
     * @return conjunto no modificable de jugadores
     */
    public Set<Usuario> getJugadores() {
        return Collections.unmodifiableSet(jugadores);
    }

    /**
     * Incorpora un jugador al scrim si existen cupos disponibles
     * y el usuario no se encuentra inscrito previamente.
     *
     * @param usuario jugador que se desea incorporar
     * @throws NullPointerException si el usuario es nulo
     * @throws IllegalStateException si no hay cupos disponibles
     *         o si el jugador ya pertenece al scrim
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

    /**
     * Calcula la cantidad de cupos restantes del scrim.
     *
     * @return diferencia entre la capacidad máxima y los jugadores inscritos
     */
    public int cuposDisponibles() {
        return cupos - jugadores.size();
    }

    /**
     * Obtiene los roles que deben cubrirse según la configuración del scrim.
     *
     * @return conjunto no modificable de roles buscados
     */
    public Set<RolJuego> getRolesBuscados() {
        return Collections.unmodifiableSet(reglasRoles.keySet());
    }

    /**
     * Agrega un rol requerido con una cantidad predeterminada de un jugador,
     * si todavía no se encuentra configurado.
     *
     * @param rol rol de juego que se desea incorporar
     */
    public void buscarRol(RolJuego rol) {
        reglasRoles.putIfAbsent(rol, 1);
    }

    /**
     * Obtiene las postulaciones asociadas al scrim.
     *
     * @return conjunto no modificable de postulaciones
     */
    public Set<Postulacion> getPostulaciones() {
        return Collections.unmodifiableSet(postulaciones);
    }

    /**
     * Establece el conjunto de postulaciones del scrim.
     *
     * @param postulaciones conjunto de postulaciones a asignar
     */
    public void setPostulaciones(Set<Postulacion> postulaciones) {
        this.postulaciones = postulaciones;
    }

    /**
     * Obtiene las confirmaciones de participación del scrim.
     *
     * @return conjunto no modificable de confirmaciones
     */
    public Set<Confirmacion> getConfirmaciones() {
        return Collections.unmodifiableSet(confirmaciones);
    }

    /**
     * Establece el conjunto de confirmaciones del scrim.
     *
     * @param confirmaciones conjunto de confirmaciones a asignar
     */
    public void setConfirmaciones(Set<Confirmacion> confirmaciones) {
        this.confirmaciones = confirmaciones;
    }

    /**
     * Obtiene el estado actual del ciclo de vida del scrim.
     *
     * @return estado del scrim según el patrón State
     */
    public ScrimState getEstado() {
        return estado;
    }

    /**
     * Establece el estado operativo del scrim mediante el patrón State.
     *
     * @param estado nuevo estado del ciclo de vida
     */
    public void setEstado(ScrimState estado) {
        this.estado = estado;
    }

    /**
     * Compara dos scrims según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambos scrims tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Scrim s && id.equals(s.id));
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash del scrim
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
