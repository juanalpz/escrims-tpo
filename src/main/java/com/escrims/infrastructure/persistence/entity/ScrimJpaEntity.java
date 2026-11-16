package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa la configuración y el estado de un scrim,
 * mapeada a la tabla {@code scrims} para su persistencia relacional.
 */
@Entity
@Table(name = "scrims")
public class ScrimJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizador_id", nullable = false)
    private UsuarioJpaEntity organizador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Juego juego;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Formato formato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "rango_min")
    private Rango rangoMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "rango_max")
    private Rango rangoMax;

    @Column(name = "latencia_max", nullable = false)
    private int latenciaMax;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private int duracionMinutos;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;

    @Column(nullable = false)
    private int cupos;

    @Column(name = "estado_actual", nullable = false, length = 40)
    private String estadoActual;

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    protected ScrimJpaEntity() {}

    /**
     * Crea una entidad de persistencia con la configuración completa del scrim,
     * generando un identificador único si no se proporciona uno.
     *
     * @param id identificador del scrim o null para generar uno nuevo
     * @param organizador entidad JPA del usuario organizador
     * @param juego juego en el que se disputará el scrim
     * @param formato formato de la partida
     * @param region región del servidor de juego
     * @param rangoMin rango competitivo mínimo permitido
     * @param rangoMax rango competitivo máximo permitido
     * @param latenciaMax latencia máxima configurada en milisegundos
     * @param fechaHora fecha y hora programadas para la partida
     * @param duracionMinutos duración estimada de la partida en minutos
     * @param modalidad modalidad de juego seleccionada
     * @param cupos cantidad máxima de participantes
     * @param estadoActual nombre del estado actual del scrim
     */
    public ScrimJpaEntity(UUID id, UsuarioJpaEntity organizador, Juego juego, Formato formato,
                          Region region, Rango rangoMin, Rango rangoMax, int latenciaMax,
                          LocalDateTime fechaHora, int duracionMinutos, Modalidad modalidad,
                          int cupos, String estadoActual) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.organizador = organizador;
        this.juego = juego;
        this.formato = formato;
        this.region = region;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.latenciaMax = latenciaMax;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.modalidad = modalidad;
        this.cupos = cupos;
        this.estadoActual = estadoActual;
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
     * @return entidad JPA del organizador
     */
    public UsuarioJpaEntity getOrganizador() { return organizador; }

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
     * Obtiene la latencia máxima configurada para el scrim.
     *
     * @return latencia máxima en milisegundos
     */
    public int getLatenciaMax() { return latenciaMax; }

    /**
     * Obtiene la fecha y hora programadas para la partida.
     *
     * @return fecha y hora del scrim
     */
    public LocalDateTime getFechaHora() { return fechaHora; }

    /**
     * Obtiene la duración estimada de la partida.
     *
     * @return duración del scrim en minutos
     */
    public int getDuracionMinutos() { return duracionMinutos; }

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
     * Obtiene el nombre del estado actual del scrim.
     *
     * @return estado actual registrado
     */
    public String getEstadoActual() { return estadoActual; }

    /**
     * Actualiza el nombre del estado actual del scrim.
     *
     * @param estadoActual nuevo estado que se desea registrar
     */
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }
}
