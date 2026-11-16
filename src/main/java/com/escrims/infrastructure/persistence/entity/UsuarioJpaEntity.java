package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.RolJuego;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad JPA que representa las cuentas y perfiles de los usuarios
 * de eScrims, mapeada a la tabla {@code usuarios}.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado;

    @Enumerated(EnumType.STRING)
    @Column(name = "juego_principal")
    private Juego juegoPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "rango_actual")
    private Rango rangoActual;

    @Enumerated(EnumType.STRING)
    private Region region;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles_preferidos", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Set<RolJuego> rolesPreferidos = new HashSet<>();

    @Column(nullable = false)
    private int strikes = 0;

    @Column(name = "cooldown_hasta")
    private LocalDateTime cooldownHasta;

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    protected UsuarioJpaEntity() {}

    /**
     * Crea una entidad de persistencia con los datos de cuenta
     * y perfil del usuario, generando un identificador si no se proporciona.
     *
     * @param id identificador del usuario o null para generar uno nuevo
     * @param username nombre de usuario en la plataforma
     * @param email dirección de correo electrónico del usuario
     * @param passwordHash hash de la contraseña del usuario
     * @param emailVerificado indica si el correo electrónico fue verificado
     * @param juegoPrincipal juego principal seleccionado por el usuario
     * @param rangoActual rango competitivo actual del usuario
     * @param region región de juego del usuario
     */
    public UsuarioJpaEntity(UUID id, String username, String email, String passwordHash,
                            boolean emailVerificado, Juego juegoPrincipal, Rango rangoActual, Region region) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerificado = emailVerificado;
        this.juegoPrincipal = juegoPrincipal;
        this.rangoActual = rangoActual;
        this.region = region;
    }

    /**
     * Obtiene el identificador único del usuario.
     *
     * @return identificador del usuario
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el nombre de usuario registrado.
     *
     * @return nombre de usuario
     */
    public String getUsername() { return username; }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return dirección de correo electrónico
     */
    public String getEmail() { return email; }

    /**
     * Obtiene el hash de la contraseña del usuario.
     *
     * @return hash de la contraseña almacenada
     */
    public String getPasswordHash() { return passwordHash; }

    /**
     * Verifica si el correo electrónico del usuario fue validado (RN-01).
     *
     * @return true si el correo está verificado; false en caso contrario
     */
    public boolean isEmailVerificado() { return emailVerificado; }

    /**
     * Actualiza el estado de verificación del correo electrónico (RN-01).
     *
     * @param emailVerificado nuevo estado de verificación
     */
    public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }

    /**
     * Obtiene el juego principal del usuario.
     *
     * @return juego principal o null si no fue definido
     */
    public Juego getJuegoPrincipal() { return juegoPrincipal; }

    /**
     * Obtiene el rango competitivo actual del usuario.
     *
     * @return rango competitivo o null si no fue definido
     */
    public Rango getRangoActual() { return rangoActual; }

    /**
     * Obtiene la región de juego del usuario.
     *
     * @return región configurada o null si no fue definida
     */
    public Region getRegion() { return region; }

    /**
     * Obtiene los roles de juego preferidos del usuario.
     *
     * @return conjunto de roles preferidos
     */
    public Set<RolJuego> getRolesPreferidos() { return rolesPreferidos; }

    /**
     * Obtiene la cantidad de strikes registrados del usuario.
     *
     * @return número de strikes acumulados
     */
    public int getStrikes() { return strikes; }

    /**
     * Actualiza la cantidad de strikes del usuario.
     *
     * @param strikes nueva cantidad de strikes
     */
    public void setStrikes(int strikes) { this.strikes = strikes; }

    /**
     * Obtiene la fecha y hora de finalización del cooldown (RN-06).
     *
     * @return fecha de finalización del cooldown o null si no está definido
     */
    public LocalDateTime getCooldownHasta() { return cooldownHasta; }

    /**
     * Establece la fecha y hora de finalización del cooldown (RN-06).
     *
     * @param cooldownHasta fecha de finalización del cooldown
     *                      o null para eliminar el límite registrado
     */
    public void setCooldownHasta(LocalDateTime cooldownHasta) { this.cooldownHasta = cooldownHasta; }
}
