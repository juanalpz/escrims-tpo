package com.escrims.domain.builder;

import com.escrims.domain.entities.*;
import com.escrims.domain.state.BuscandoState;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * «Builder» Construye un {@link Scrim} de forma incremental con validaciones
 * encadenadas (fluent interface) y verifica todas las invariantes de negocio
 * antes de devolver la instancia.
 *
 * <p>Reglas de negocio que valida:
 * <ul>
 *   <li>RN-01 / RN-06: email verificado y sin sanciones activas.</li>
 *   <li>RN-03: coherencia de rangos (ambos null o min ≤ max).</li>
 *   <li>RN-04: latencia máxima entre 1 y 500 ms (si se define).</li>
 *   <li>RN-05: roles obligatorios no superan cupos totales.</li>
 *   <li>RN-08: estado inicial {@code BuscandoState}, fecha futura.</li>
 * </ul>
 */
public class ScrimBuilder {

    private Usuario organizador;
    private Juego juego;
    private Formato formato;
    private Region region;
    private Rango rangoMin;
    private Rango rangoMax;
    private Integer latenciaMax;
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private Modalidad modalidad;
    private Integer cuposTotales;
    private final Map<RolJuego, Integer> reglasRoles = new EnumMap<>(RolJuego.class);

    public ScrimBuilder() {
        this.modalidad = Modalidad.CASUAL;
        this.duracionMinutos = 60;
    }

    // ── Métodos encadenados (Fluent Interface) ─────────────────

    public ScrimBuilder organizador(Usuario organizador) {
        this.organizador = organizador;
        return this;
    }

    public ScrimBuilder juego(Juego juego) {
        this.juego = juego;
        return this;
    }

    public ScrimBuilder formato(Formato formato) {
        this.formato = formato;
        return this;
    }

    public ScrimBuilder region(Region region) {
        this.region = region;
        return this;
    }

    /**
     * Define los límites de rango del scrim. Para un scrim abierto (sin
     * restricción de rango), no invocar este método o pasar ambos como null.
     */
    public ScrimBuilder rango(Rango min, Rango max) {
        this.rangoMin = min;
        this.rangoMax = max;
        return this;
    }

    /**
     * Define la latencia máxima en milisegundos. Si no se invoca, el scrim
     * no tendrá restricción de latencia.
     */
    public ScrimBuilder latenciaMax(int latenciaMaxMs) {
        this.latenciaMax = latenciaMaxMs;
        return this;
    }

    public ScrimBuilder fechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        return this;
    }

    public ScrimBuilder duracion(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
        return this;
    }

    public ScrimBuilder modalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
        return this;
    }

    /** Permite forzar cupos manuales. Si no se llama, se calcula por el Formato. */
    public ScrimBuilder cupos(int cuposTotales) {
        this.cuposTotales = cuposTotales;
        return this;
    }

    /** Agrega un cupo obligatorio para un rol específico (ej. 2 Duelistas). */
    public ScrimBuilder agregarReglaRol(RolJuego rol, int cantidadRequerida) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        if (cantidadRequerida <= 0) {
            throw new IllegalArgumentException("La cantidad requerida para el rol debe ser mayor a 0.");
        }
        this.reglasRoles.put(rol, cantidadRequerida);
        return this;
    }

    // ── Método final de construcción ───────────────────────────

    public Scrim build() {
        validarCamposObligatorios();
        validarOrganizador();
        validarRangos();
        validarLatencia();
        validarFechaYDuracion();
        int cuposCalculados = resolverYValidarCupos();
        validarReglasDeRoles(cuposCalculados);

        Scrim scrim = new Scrim(
                this.organizador,
                this.juego,
                this.formato,
                this.region,
                this.rangoMin,
                this.rangoMax,
                this.latenciaMax,
                this.fechaHora,
                this.duracionMinutos,
                this.modalidad,
                cuposCalculados,
                new EnumMap<>(this.reglasRoles)
        );

        // RN-08: Todo Scrim nace en estado "Buscando jugadores"
        scrim.setEstado(new BuscandoState());

        return scrim;
    }

    // ── Validaciones privadas ──────────────────────────────────

    /** Verifica que no falte ningún dato esencial de la partida. */
    private void validarCamposObligatorios() {
        if (this.organizador == null) throw new IllegalStateException("Falta el organizador.");
        if (this.juego == null)       throw new IllegalStateException("Falta el juego.");
        if (this.formato == null)     throw new IllegalStateException("Falta el formato.");
        if (this.region == null)      throw new IllegalStateException("Falta la región.");
        if (this.fechaHora == null)   throw new IllegalStateException("Falta la fecha y hora.");
    }

    /** RN-01 y RN-06: email verificado y sin sanciones activas. */
    private void validarOrganizador() {
        if (!this.organizador.isEmailVerificado()) {
            throw new IllegalStateException("El organizador debe tener su email verificado.");
        }
        if (this.organizador.tieneCooldownActivo()) {
            throw new IllegalStateException("El organizador tiene una sanción activa.");
        }
    }

    /** RN-03: coherencia de rangos. Ambos null (abierto) o min ≤ max. */
    private void validarRangos() {
        if ((this.rangoMin == null) != (this.rangoMax == null)) {
            throw new IllegalArgumentException("Debe definir rango mínimo y máximo, o dejar ambos abiertos.");
        }
        if (this.rangoMin != null && this.rangoMin.ordinal() > this.rangoMax.ordinal()) {
            throw new IllegalArgumentException("El rango mínimo no puede ser mayor al rango máximo.");
        }
    }

    /** RN-04: si se definió latencia, debe ser entre 1 y 500 ms. */
    private void validarLatencia() {
        if (this.latenciaMax != null && (this.latenciaMax <= 0 || this.latenciaMax > 500)) {
            throw new IllegalArgumentException("La latencia máxima debe estar entre 1 y 500 ms.");
        }
    }

    /** Fecha estrictamente futura y duración razonable. */
    private void validarFechaYDuracion() {
        if (!this.fechaHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha del scrim debe ser posterior al momento actual.");
        }
        if (this.duracionMinutos == null || this.duracionMinutos < 15 || this.duracionMinutos > 360) {
            throw new IllegalArgumentException("La duración debe estar entre 15 y 360 minutos.");
        }
    }

    /** Deduce cupos por formato y valida si se pasaron a mano (RN-05). */
    private int resolverYValidarCupos() {
        int cuposPorFormato = this.formato.getCuposTotales();
        if (this.cuposTotales == null) {
            return cuposPorFormato;
        }
        if (this.cuposTotales != cuposPorFormato) {
            throw new IllegalArgumentException("Los cupos no coinciden con el formato elegido.");
        }
        return this.cuposTotales;
    }

    /** RN-05: la suma de roles obligatorios no puede superar los cupos totales. */
    private void validarReglasDeRoles(int cuposTotales) {
        int sumaRoles = this.reglasRoles.values().stream().mapToInt(Integer::intValue).sum();
        if (sumaRoles > cuposTotales) {
            throw new IllegalArgumentException("La suma de roles obligatorios supera los cupos totales.");
        }
    }
}
