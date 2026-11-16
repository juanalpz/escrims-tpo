package com.escrims.domain.builder;

import com.escrims.domain.entities.*;
import com.escrims.domain.state.BuscandoState;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Construye instancias válidas de {@link Scrim} de forma incremental verificando las invariantes de negocio (Patrón Builder).
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

    /**
     * Inicializa el constructor con valores por defecto (modalidad {@code CASUAL} y duración de 60 minutos).
     */
    public ScrimBuilder() {
        this.modalidad = Modalidad.CASUAL;
        this.duracionMinutos = 60;
    }

    /**
     * Asigna el usuario organizador responsable de crear el encuentro.
     *
     * @param organizador usuario creador del scrim.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder organizador(Usuario organizador) {
        this.organizador = organizador;
        return this;
    }

    /**
     * Define el videojuego en el que se disputará la partida.
     *
     * @param juego videojuego seleccionado.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder juego(Juego juego) {
        this.juego = juego;
        return this;
    }

    /**
     * Establece el formato de equipos del encuentro (ej. 5v5, 3v3, 1v1).
     *
     * @param formato formato competitivo del scrim.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder formato(Formato formato) {
        this.formato = formato;
        return this;
    }

    /**
     * Define el servidor o región geográfica donde se jugará el scrim.
     *
     * @param region región del encuentro.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder region(Region region) {
        this.region = region;
        return this;
    }

    /**
     * Define los límites de rango mínimo y máximo admitidos (RN-03).
     *
     * @param min rango mínimo permitido, o {@code null} si es abierto.
     * @param max rango máximo permitido, o {@code null} si es abierto.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder rango(Rango min, Rango max) {
        this.rangoMin = min;
        this.rangoMax = max;
        return this;
    }

    /**
     * Establece el umbral de latencia máxima tolerada en milisegundos (RN-04).
     *
     * @param latenciaMaxMs ping máximo permitido en milisegundos (entre 1 y 500 ms).
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder latenciaMax(int latenciaMaxMs) {
        this.latenciaMax = latenciaMaxMs;
        return this;
    }

    /**
     * Define la fecha y hora programada para el inicio de la partida.
     *
     * @param fechaHora fecha y hora futura del encuentro.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder fechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        return this;
    }

    /**
     * Define la duración estimada del encuentro en minutos.
     *
     * @param duracionMinutos tiempo estimado de juego en minutos.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder duracion(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
        return this;
    }

    /**
     * Define la modalidad de juego del scrim.
     *
     * @param modalidad modalidad seleccionada (casual, competitivo, práctica).
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder modalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
        return this;
    }

    /**
     * Define manualmente la cantidad total de cupos del scrim.
     *
     * @param cuposTotales cantidad total de plazas requeridas.
     * @return esta instancia del builder para encadenamiento.
     */
    public ScrimBuilder cupos(int cuposTotales) {
        this.cuposTotales = cuposTotales;
        return this;
    }

    /**
     * Agrega un cupo obligatorio para un rol específico dentro del encuentro (RN-05).
     *
     * @param rol               rol dentro del juego a requerir.
     * @param cantidadRequerida cantidad de plazas obligatorias para ese rol.
     * @return esta instancia del builder para encadenamiento.
     * @throws IllegalArgumentException si el rol es nulo o la cantidad es menor o igual a cero.
     */
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

    /**
     * Valida todas las reglas de negocio configuradas y construye el {@link Scrim} en estado inicial {@link BuscandoState} (RN-08).
     *
     * @return nueva instancia válida de {@link Scrim}.
     * @throws IllegalStateException    si faltan datos obligatorios o el organizador no está habilitado (RN-01, RN-06).
     * @throws IllegalArgumentException si los rangos, latencia, fecha, duración o cupos no cumplen las restricciones (RN-03, RN-04, RN-05).
     */
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

        // RN-08: Todo scrim inicia en estado "Buscando jugadores"
        scrim.setEstado(new BuscandoState());

        return scrim;
    }

    /**
     * Verifica que los atributos esenciales del encuentro hayan sido establecidos.
     *
     * @throws IllegalStateException si falta organizador, juego, formato, región o fecha.
     */
    private void validarCamposObligatorios() {
        if (this.organizador == null) throw new IllegalStateException("Falta el organizador.");
        if (this.juego == null)       throw new IllegalStateException("Falta el juego.");
        if (this.formato == null)     throw new IllegalStateException("Falta el formato.");
        if (this.region == null)      throw new IllegalStateException("Falta la región.");
        if (this.fechaHora == null)   throw new IllegalStateException("Falta la fecha y hora.");
    }

    /**
     * Verifica que el organizador tenga su email verificado y no posea sanciones activas (RN-01, RN-06).
     *
     * @throws IllegalStateException si el organizador no cumple las condiciones de habilitación.
     */
    private void validarOrganizador() {
        if (!this.organizador.isEmailVerificado()) {
            throw new IllegalStateException("El organizador debe tener su email verificado.");
        }
        if (this.organizador.tieneCooldownActivo()) {
            throw new IllegalStateException("El organizador tiene una sanción activa.");
        }
    }

    /**
     * Valida la coherencia de los rangos mínimo y máximo configurados (RN-03).
     *
     * @throws IllegalArgumentException si solo uno de los límites está definido o si el mínimo supera al máximo.
     */
    private void validarRangos() {
        if ((this.rangoMin == null) != (this.rangoMax == null)) {
            throw new IllegalArgumentException("Debe definir rango mínimo y máximo, o dejar ambos abiertos.");
        }
        if (this.rangoMin != null && this.rangoMin.ordinal() > this.rangoMax.ordinal()) {
            throw new IllegalArgumentException("El rango mínimo no puede ser mayor al rango máximo.");
        }
    }

    /**
     * Valida que la latencia máxima se encuentre dentro del umbral permitido de 1 a 500 ms (RN-04).
     *
     * @throws IllegalArgumentException si la latencia definida está fuera de rango.
     */
    private void validarLatencia() {
        if (this.latenciaMax != null && (this.latenciaMax <= 0 || this.latenciaMax > 500)) {
            throw new IllegalArgumentException("La latencia máxima debe estar entre 1 y 500 ms.");
        }
    }

    /**
     * Valida que la fecha sea estrictamente futura y la duración esté entre 15 y 360 minutos.
     *
     * @throws IllegalArgumentException si la fecha ya pasó o la duración está fuera de los límites.
     */
    private void validarFechaYDuracion() {
        if (!this.fechaHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha del scrim debe ser posterior al momento actual.");
        }
        if (this.duracionMinutos == null || this.duracionMinutos < 15 || this.duracionMinutos > 360) {
            throw new IllegalArgumentException("La duración debe estar entre 15 y 360 minutos.");
        }
    }

    /**
     * Calcula los cupos según el formato y verifica coherencia si fueron ingresados manualmente (RN-05).
     *
     * @return cantidad total de cupos validados para el scrim.
     * @throws IllegalArgumentException si los cupos manuales no coinciden con el formato elegido.
     */
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

    /**
     * Verifica que la suma de plazas obligatorias por rol no exceda los cupos totales del encuentro (RN-05).
     *
     * @param cuposTotales cantidad total de plazas disponibles en el scrim.
     * @throws IllegalArgumentException si los roles requeridos superan el cupo total.
     */
    private void validarReglasDeRoles(int cuposTotales) {
        int sumaRoles = this.reglasRoles.values().stream().mapToInt(Integer::intValue).sum();
        if (sumaRoles > cuposTotales) {
            throw new IllegalArgumentException("La suma de roles obligatorios supera los cupos totales.");
        }
    }
}
