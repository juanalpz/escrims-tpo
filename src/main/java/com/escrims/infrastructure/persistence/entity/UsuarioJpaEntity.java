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

    protected UsuarioJpaEntity() {}

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

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEmailVerificado() { return emailVerificado; }
    public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }
    public Juego getJuegoPrincipal() { return juegoPrincipal; }
    public Rango getRangoActual() { return rangoActual; }
    public Region getRegion() { return region; }
    public Set<RolJuego> getRolesPreferidos() { return rolesPreferidos; }
    public int getStrikes() { return strikes; }
    public void setStrikes(int strikes) { this.strikes = strikes; }
    public LocalDateTime getCooldownHasta() { return cooldownHasta; }
    public void setCooldownHasta(LocalDateTime cooldownHasta) { this.cooldownHasta = cooldownHasta; }
}
