package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Representa a un jugador de eScrims, incluyendo su perfil competitivo,
 * preferencias de juego, historial de participación y sanciones
 * (RN-01, RN-03, RN-06).
 */
public class Usuario {

    /** Puntuación máxima permitida para el rating de fair play. */
    public static final double RATING_MAXIMO = 5.0;

    private final UUID id;
    private String username;
    private String email;
    private boolean emailVerificado;
    private int mmr;
    private int latenciaPromedio;
    private Region region;
    private final Set<RolJuego> rolesPreferidos = EnumSet.noneOf(RolJuego.class);
    private final Map<Juego, Rango> rangosPorJuego = new EnumMap<>(Juego.class);
    private final List<Sancion> sanciones = new ArrayList<>();
    private int scrimsJugados;
    private int abandonos;
    private double ratingFairPlay = RATING_MAXIMO;

    /**
     * Crea un usuario con sus datos básicos de perfil competitivo.
     *
     * @param username nombre de usuario en la plataforma
     * @param mmr puntuación competitiva inicial
     * @param latenciaPromedio latencia promedio en milisegundos
     * @param region región de juego del usuario
     * @throws NullPointerException si el nombre de usuario es nulo
     */
    public Usuario(String username, int mmr, int latenciaPromedio, Region region) {
        this.id = UUID.randomUUID();
        this.username = Objects.requireNonNull(username, "username");
        this.mmr = mmr;
        this.latenciaPromedio = latenciaPromedio;
        this.region = region;
    }

    /**
     * Crea un usuario con un nombre generado a partir de su identificador único.
     */
    public Usuario() {
        this.id = UUID.randomUUID();
        this.username = "usuario-" + id.toString().substring(0, 8);
    }

    /**
     * Obtiene el identificador único del usuario.
     *
     * @return identificador del usuario
     */
    public UUID getId() {
        return id;
    }

    /**
     * Obtiene el nombre de usuario.
     *
     * @return nombre de usuario registrado
     */
    public String getUsername() {
        return username;
    }

    /**
     * Establece el nombre de usuario.
     *
     * @param username nuevo nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return dirección de correo electrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param email nueva dirección de correo electrónico
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Verifica si el correo electrónico del usuario fue validado (RN-01).
     *
     * @return true si el correo está verificado; false en caso contrario
     */
    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    /**
     * Actualiza el estado de verificación del correo electrónico (RN-01).
     *
     * @param emailVerificado nuevo estado de verificación
     */
    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    /**
     * Verifica si el usuario tiene un cooldown vigente (RN-06).
     *
     * @return true si existe un cooldown activo; false en caso contrario
     * @see #tieneSancionActiva(LocalDateTime)
     */
    public boolean tieneCooldownActivo() {
        return tieneSancionActiva(LocalDateTime.now());
    }

    /**
     * Verifica si alguna sanción del usuario está activa
     * en un momento determinado (RN-06).
     *
     * @param momento fecha y hora en la que se evalúan las sanciones
     * @return true si existe una sanción activa; false en caso contrario
     * @throws NullPointerException si el momento es nulo y se evalúa
     *         una sanción con cooldown definido
     */
    public boolean tieneSancionActiva(LocalDateTime momento) {
        return sanciones.stream().anyMatch(s -> s.estaActiva(momento));
    }

    /**
     * Obtiene las sanciones registradas del usuario.
     *
     * @return lista no modificable de sanciones
     */
    public List<Sancion> getSanciones() {
        return Collections.unmodifiableList(sanciones);
    }

    /**
     * Registra una sanción en el historial del usuario.
     *
     * @param sancion sanción que se desea registrar
     * @throws NullPointerException si la sanción es nula
     */
    public void agregarSancion(Sancion sancion) {
        sanciones.add(Objects.requireNonNull(sancion, "sancion"));
    }

    /**
     * Calcula la cantidad total de strikes acumulados por el usuario.
     *
     * @return suma de los strikes de todas las sanciones registradas
     */
    public int getStrikes() {
        return sanciones.stream().mapToInt(Sancion::getStrikes).sum();
    }

    /**
     * Obtiene la puntuación competitiva del usuario.
     *
     * @return valor actual de MMR
     */
    public int getMmr() {
        return mmr;
    }

    /**
     * Actualiza la puntuación competitiva del usuario.
     *
     * @param mmr nuevo valor de MMR
     */
    public void setMmr(int mmr) {
        this.mmr = mmr;
    }

    /**
     * Obtiene la latencia promedio del usuario.
     *
     * @return latencia promedio en milisegundos
     */
    public int getLatenciaPromedio() {
        return latenciaPromedio;
    }

    /**
     * Actualiza la latencia promedio del usuario.
     *
     * @param latenciaPromedio nueva latencia en milisegundos
     */
    public void setLatenciaPromedio(int latenciaPromedio) {
        this.latenciaPromedio = latenciaPromedio;
    }

    /**
     * Obtiene la región de juego del usuario.
     *
     * @return región configurada
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Establece la región de juego del usuario.
     *
     * @param region nueva región de juego
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    /**
     * Obtiene los roles de juego preferidos del usuario.
     *
     * @return conjunto no modificable de roles preferidos
     */
    public Set<RolJuego> getRolesPreferidos() {
        return Collections.unmodifiableSet(rolesPreferidos);
    }

    /**
     * Reemplaza los roles preferidos del usuario por los indicados.
     *
     * @param roles conjunto de roles preferidos o null para vaciarlo
     * @throws NullPointerException si el conjunto contiene un rol nulo
     */
    public void setRolesPreferidos(Set<RolJuego> roles) {
        this.rolesPreferidos.clear();
        if (roles != null) {
            this.rolesPreferidos.addAll(roles);
        }
    }

    /**
     * Agrega un rol a las preferencias de juego del usuario.
     *
     * @param rol rol de juego que se desea agregar
     * @throws NullPointerException si el rol es nulo
     */
    public void agregarRolPreferido(RolJuego rol) {
        rolesPreferidos.add(rol);
    }

    /**
     * Obtiene el rango competitivo actual del usuario, utilizando
     * los rangos registrados o su MMR como alternativa (RN-03).
     *
     * @return primer rango registrado, rango derivado del MMR
     *         o null si no puede determinarse
     */
    public Rango getRangoActual() {
        if (!rangosPorJuego.isEmpty()) {
            return rangosPorJuego.values().iterator().next();
        }
        if (mmr > 0) {
            try {
                return Rango.desdeMMR(mmr);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    /**
     * Establece un rango competitivo común para todos los juegos
     * y actualiza el MMR al punto medio de dicho rango.
     *
     * @param rango rango competitivo que se desea asignar
     */
    public void setRangoActual(Rango rango) {
        if (rango != null) {
            this.mmr = (rango.getMmrMin() + rango.getMmrMax()) / 2;
            for (Juego j : Juego.values()) {
                asignarRango(j, rango);
            }
        }
    }

    /**
     * Obtiene el rango competitivo del usuario para un juego específico,
     * utilizando el MMR como alternativa cuando no existe uno asignado (RN-03).
     *
     * @param juego juego cuyo rango se desea consultar
     * @return rango registrado, rango derivado del MMR
     *         o null si no puede determinarse
     */
    public Rango getRangoParaJuego(Juego juego) {
        if (juego == null) {
            return null;
        }
        Rango rango = rangosPorJuego.get(juego);
        if (rango == null && mmr > 0) {
            try {
                return Rango.desdeMMR(mmr);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return rango;
    }

    /**
     * Asigna o elimina el rango competitivo del usuario para un juego (RN-03).
     *
     * @param juego juego cuya clasificación se desea modificar
     * @param rango rango que se desea asignar o null para eliminarlo
     * @throws IllegalArgumentException si el juego es nulo
     */
    public void asignarRango(Juego juego, Rango rango) {
        if (juego == null) {
            throw new IllegalArgumentException("El juego no puede ser nulo.");
        }
        if (rango == null) {
            rangosPorJuego.remove(juego);
        } else {
            rangosPorJuego.put(juego, rango);
        }
    }

    /**
     * Obtiene los rangos competitivos registrados por juego.
     *
     * @return mapa no modificable de juegos y rangos asignados
     */
    public Map<Juego, Rango> getRangosPorJuego() {
        return Collections.unmodifiableMap(rangosPorJuego);
    }

    /**
     * Obtiene la cantidad de scrims disputados por el usuario.
     *
     * @return número de scrims jugados
     */
    public int getScrimsJugados() {
        return scrimsJugados;
    }

    /**
     * Obtiene la cantidad de scrims abandonados por el usuario.
     *
     * @return número de abandonos registrados
     */
    public int getAbandonos() {
        return abandonos;
    }

    /**
     * Actualiza el historial de participación y abandonos del usuario.
     *
     * @param scrimsJugados cantidad total de scrims disputados
     * @param abandonos cantidad de scrims abandonados
     * @throws IllegalArgumentException si alguna cantidad es negativa
     *         o si los abandonos superan los scrims jugados
     */
    public void registrarHistorial(int scrimsJugados, int abandonos) {
        if (scrimsJugados < 0 || abandonos < 0 || abandonos > scrimsJugados) {
            throw new IllegalArgumentException("Historial inválido");
        }
        this.scrimsJugados = scrimsJugados;
        this.abandonos = abandonos;
    }

    /**
     * Calcula la proporción de partidas abandonadas respecto de las disputadas.
     *
     * @return tasa de abandono entre 0 y 1, o cero si no existe historial
     */
    public double getTasaAbandono() {
        return scrimsJugados == 0 ? 0.0 : (double) abandonos / scrimsJugados;
    }

    /**
     * Obtiene la puntuación de comportamiento del usuario.
     *
     * @return rating de fair play entre 0 y {@link #RATING_MAXIMO}
     */
    public double getRatingFairPlay() {
        return ratingFairPlay;
    }

    /**
     * Actualiza la puntuación de fair play del usuario.
     *
     * @param ratingFairPlay nueva puntuación de comportamiento
     * @throws IllegalArgumentException si el rating es menor que cero
     *         o supera {@link #RATING_MAXIMO}
     */
    public void setRatingFairPlay(double ratingFairPlay) {
        if (ratingFairPlay < 0 || ratingFairPlay > RATING_MAXIMO) {
            throw new IllegalArgumentException("El rating debe estar entre 0 y " + RATING_MAXIMO);
        }
        this.ratingFairPlay = ratingFairPlay;
    }

    /**
     * Compara dos usuarios según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambos usuarios tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Usuario otro && id.equals(otro.id));
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash del usuario
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Obtiene la representación textual del usuario.
     *
     * @return nombre de usuario registrado
     */
    @Override
    public String toString() {
        return username;
    }
}
