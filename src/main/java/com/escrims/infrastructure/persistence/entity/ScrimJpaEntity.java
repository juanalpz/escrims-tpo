package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    protected ScrimJpaEntity() {}

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

    public UUID getId() { return id; }
    public UsuarioJpaEntity getOrganizador() { return organizador; }
    public Juego getJuego() { return juego; }
    public Formato getFormato() { return formato; }
    public Region getRegion() { return region; }
    public Rango getRangoMin() { return rangoMin; }
    public Rango getRangoMax() { return rangoMax; }
    public int getLatenciaMax() { return latenciaMax; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public Modalidad getModalidad() { return modalidad; }
    public int getCupos() { return cupos; }
    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }
}
