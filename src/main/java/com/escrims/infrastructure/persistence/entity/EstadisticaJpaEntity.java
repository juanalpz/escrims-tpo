package com.escrims.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad JPA que representa las estadísticas individuales de un jugador
 * al finalizar un scrim, mapeada a la tabla {@code estadisticas}.
 */
@Entity
@Table(name = "estadisticas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"scrim_id", "usuario_id"})
})
public class EstadisticaJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    @Column(nullable = false)
    private boolean mvp;

    @Column(nullable = false, length = 30)
    private String kda;

    @Column(length = 500)
    private String observaciones;

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    protected EstadisticaJpaEntity() {}

    /**
     * Crea un registro de estadísticas individuales asociado
     * a un jugador y a un scrim.
     *
     * @param scrim entidad JPA del scrim correspondiente
     * @param usuario entidad JPA del jugador evaluado
     * @param mvp indica si el jugador fue reconocido como MVP
     * @param kda registro de asesinatos, muertes y asistencias del jugador
     * @param observaciones comentarios adicionales sobre su desempeño
     */
    public EstadisticaJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity usuario, boolean mvp, String kda, String observaciones) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.mvp = mvp;
        this.kda = kda;
        this.observaciones = observaciones;
    }

    /**
     * Obtiene el identificador único del registro de estadísticas.
     *
     * @return identificador de las estadísticas
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el scrim asociado a las estadísticas.
     *
     * @return entidad JPA del scrim
     */
    public ScrimJpaEntity getScrim() { return scrim; }

    /**
     * Obtiene el jugador al que pertenecen las estadísticas.
     *
     * @return entidad JPA del usuario
     */
    public UsuarioJpaEntity getUsuario() { return usuario; }

    /**
     * Indica si el jugador fue reconocido como MVP.
     *
     * @return true si el jugador fue MVP; false en caso contrario
     */
    public boolean isMvp() { return mvp; }

    /**
     * Obtiene el registro de asesinatos, muertes y asistencias del jugador.
     *
     * @return estadísticas KDA del jugador
     */
    public String getKda() { return kda; }

    /**
     * Obtiene las observaciones adicionales sobre el desempeño del jugador.
     *
     * @return observaciones registradas o null si no fueron definidas
     */
    public String getObservaciones() { return observaciones; }
}
