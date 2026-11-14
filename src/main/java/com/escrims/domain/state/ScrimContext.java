package com.escrims.domain.state;

import com.escrims.domain.entities.*;

/**
 * Contexto del Patrón State
 * Administra el estado actual del Scrim y delega las transiciones de ciclo de
 * vida a la implementación concreta de ScrimState activa.
 */
public class ScrimContext {
    private final Scrim scrim;
    private ScrimState state;

    public ScrimContext(Scrim scrim) {
        if (scrim == null) {
            throw new IllegalArgumentException("El Scrim no puede ser nulo en el contexto.");
        }
        this.scrim = scrim;
        // Toma el estado inicial que ya trae el Scrim (por defecto BuscandoState desde
        // ScrimBuilder)
        this.state = (scrim.getEstado() != null) ? scrim.getEstado() : new BuscandoState();
        this.scrim.setEstado(this.state);
    }

    /**
     * Permite a los estados concretos transicionar hacia el siguiente estado
     * manteniendo sincronizada la entidad Scrim[cite: 4, 5].
     */
    public void setState(ScrimState nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }
        this.state = nuevoEstado;
        this.scrim.setEstado(nuevoEstado);
    }

    // --- MÉTODOS DE DELEGACIÓN AL ESTADO ACTUAL ---

    public void postular(Usuario usuario, RolJuego rol) {
        this.state.postular(this, usuario, rol);
    }

    public void confirmar(Usuario usuario) {
        this.state.confirmar(this, usuario);
    }

    public void iniciar() {
        this.state.iniciar(this);
    }

    public void finalizar() {
        this.state.finalizar(this);
    }

    public void cancelar() {
        this.state.cancelar(this);
    }

    // --- MÉTODOS AUXILIARES DE DOMINIO PARA LOS ESTADOS CONCRETOS ---

    public Scrim getScrim() {
        return scrim;
    }

    public ScrimState getState() {
        return state;
    }

    /**
     * Verifica si la cantidad de postulaciones aceptadas (o confirmaciones creadas)
     * ya cubrió la totalidad de cupos del Scrim.
     */
    public boolean estaCupoCompleto() {
        long aceptados = scrim.getPostulaciones().stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.ACEPTADA)
                .count();
        return aceptados >= scrim.getCupos();
    }

    /**
     * Verifica si el 100% de los participantes en el lobby confirmaron su
     * asistencia (RN-09).
     */
    public boolean todosConfirmaron() {
        if (scrim.getConfirmaciones().isEmpty()) {
            return false;
        }
        return scrim.getConfirmaciones().size() == scrim.getCupos()
                && scrim.getConfirmaciones().stream().allMatch(Confirmacion::isConfirmado);
    }
}
