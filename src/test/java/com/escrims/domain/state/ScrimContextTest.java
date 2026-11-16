package com.escrims.domain.state;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica la gestión y delegación de estados del ciclo de vida
 * de los scrims mediante el patrón State, incluyendo RN-09.
 */
class ScrimContextTest {

    private Scrim scrimBase;
    private Usuario organizador;
    private Usuario jugador1;
    private Usuario jugador2;

    /**
     * Inicializa los usuarios de prueba y un scrim válido
     * de formato 1v1 antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        organizador = new Usuario();
        organizador.setEmailVerificado(true);

        jugador1 = new Usuario();
        jugador1.setEmailVerificado(true);

        jugador2 = new Usuario();
        jugador2.setEmailVerificado(true);

        // Creamos un Scrim 1v1 (2 cupos totales)
        scrimBase = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();
    }

    /**
     * Verifica que el contexto rechace un scrim nulo y mantenga
     * sincronizado su estado inicial con la entidad asociada.
     */
    @Test
    @DisplayName("El contexto no permite instanciarse con Scrim nulo y sincroniza estado inicial")
    void deberiaValidarScrimNoNuloEInicializarEstado() {
        assertThrows(IllegalArgumentException.class, () -> new ScrimContext(null));

        ScrimContext ctx = new ScrimContext(scrimBase);
        assertSame(scrimBase, ctx.getScrim());
        assertNotNull(ctx.getState());
        assertSame(ctx.getState(), scrimBase.getEstado(),
                "El contexto y el Scrim deben compartir la misma referencia de estado.");
    }

    /**
     * Verifica que los cambios de estado se reflejen tanto en el contexto
     * como en el scrim y que no se permita asignar un estado nulo.
     */
    @Test
    @DisplayName("setState actualiza el estado en el contexto y en la entidad Scrim, y rechaza null")
    void deberiaSincronizarCambioDeEstado() {
        ScrimContext ctx = new ScrimContext(scrimBase);
        assertThrows(IllegalArgumentException.class, () -> ctx.setState(null));

        EstadoSpy nuevoEstado = new EstadoSpy();
        ctx.setState(nuevoEstado);

        assertSame(nuevoEstado, ctx.getState());
        assertSame(nuevoEstado, scrimBase.getEstado());
    }

    /**
     * Verifica que el contexto delegue las operaciones de postulación,
     * confirmación y transición del ciclo de vida al estado activo.
     */
    @Test
    @DisplayName("Delega correctamente todas las acciones del ciclo de vida al ScrimState activo")
    void deberiaDelegarTodasLasAccionesAlEstadoActivo() {
        ScrimContext ctx = new ScrimContext(scrimBase);
        EstadoSpy spy = new EstadoSpy();
        ctx.setState(spy);

        ctx.postular(jugador1, RolJuego.DUELIST);
        assertTrue(spy.postularLlamado);
        assertSame(ctx, spy.ultimoContexto);
        assertSame(jugador1, spy.ultimoUsuario);
        assertEquals(RolJuego.DUELIST, spy.ultimoRol);

        ctx.confirmar(jugador2);
        assertTrue(spy.confirmarLlamado);
        assertSame(jugador2, spy.ultimoUsuario);

        ctx.iniciar();
        assertTrue(spy.iniciarLlamado);

        ctx.finalizar();
        assertTrue(spy.finalizarLlamado);

        ctx.cancelar();
        assertTrue(spy.cancelarLlamado);
    }

    /**
     * Verifica que el cupo del scrim se considere completo únicamente
     * cuando todas las plazas estén cubiertas por postulaciones aceptadas.
     */
    @Test
    @DisplayName("estaCupoCompleto detecta correctamente cuando se alcanzan los cupos con postulaciones ACEPTADAS")
    void deberiaEvaluarSiElCupoEstaCompleto() {
        ScrimContext ctx = new ScrimContext(scrimBase); // 1v1 -> 2 cupos
        assertFalse(ctx.estaCupoCompleto(), "Con 0 postulaciones no debe estar completo.");

        Set<Postulacion> postulaciones = new HashSet<>();

        Postulacion p1 = new Postulacion(jugador1, scrimBase, RolJuego.DUELIST);
        p1.aceptar();
        postulaciones.add(p1);
        scrimBase.setPostulaciones(postulaciones);
        assertFalse(ctx.estaCupoCompleto(), "Con 1 de 2 cupos aún no debe estar completo.");

        Postulacion p2 = new Postulacion(jugador2, scrimBase, RolJuego.DUELIST);
        // p2 sigue PENDIENTE, no debe contar como cupo cubierto
        postulaciones.add(p2);
        scrimBase.setPostulaciones(postulaciones);
        assertFalse(ctx.estaCupoCompleto(), "Las postulaciones PENDIENTES no deben llenar el cupo.");

        p2.aceptar();
        assertTrue(ctx.estaCupoCompleto(), "Con 2 postulaciones ACEPTADAS debe marcar cupo completo.");
    }

    /**
     * Verifica que todos los participantes deban confirmar su asistencia
     * para completar el proceso de confirmación del scrim (RN-09).
     */
    @Test
    @DisplayName("todosConfirmaron exige que el 100% de los participantes confirmen (RN-09)")
    void deberiaEvaluarSiTodosConfirmaron() {
        ScrimContext ctx = new ScrimContext(scrimBase); // 1v1 -> 2 cupos
        assertFalse(ctx.todosConfirmaron(), "Sin confirmaciones debe dar false.");

        Confirmacion c1 = new Confirmacion(jugador1, scrimBase);
        Confirmacion c2 = new Confirmacion(jugador2, scrimBase);

        Set<Confirmacion> confirmaciones = new HashSet<>();
        confirmaciones.add(c1);
        confirmaciones.add(c2);
        scrimBase.setConfirmaciones(confirmaciones);

        assertFalse(ctx.todosConfirmaron(), "Si ninguno confirmó debe dar false.");

        c1.confirmar();
        assertFalse(ctx.todosConfirmaron(), "Si confirmó solo 1 de 2 (50%) debe dar false.");

        c2.confirmar();
        assertTrue(ctx.todosConfirmaron(), "Si confirmaron 2 de 2 (100%) debe dar true.");
    }

    /**
     * Implementación auxiliar de ScrimState mediante un Test Spy
     * para verificar la delegación de operaciones del contexto.
     */
    private static class EstadoSpy implements ScrimState {

        boolean postularLlamado = false;
        boolean confirmarLlamado = false;
        boolean iniciarLlamado = false;
        boolean finalizarLlamado = false;
        boolean cancelarLlamado = false;

        ScrimContext ultimoContexto;
        Usuario ultimoUsuario;
        RolJuego ultimoRol;

        /**
         * Registra la invocación de la operación de postulación.
         *
         * @param ctx contexto que solicita la operación
         * @param usuario usuario que realiza la postulación
         * @param rol rol solicitado por el usuario
         */
        @Override
        public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
            this.postularLlamado = true;
            this.ultimoContexto = ctx;
            this.ultimoUsuario = usuario;
            this.ultimoRol = rol;
        }

        /**
         * Registra la invocación de la operación de confirmación.
         *
         * @param ctx contexto que solicita la operación
         * @param usuario usuario que confirma su participación
         */
        @Override
        public void confirmar(ScrimContext ctx, Usuario usuario) {
            this.confirmarLlamado = true;
            this.ultimoContexto = ctx;
            this.ultimoUsuario = usuario;
        }

        /**
         * Registra la invocación de la operación de inicio del scrim.
         *
         * @param ctx contexto que solicita la operación
         */
        @Override
        public void iniciar(ScrimContext ctx) {
            this.iniciarLlamado = true;
            this.ultimoContexto = ctx;
        }

        /**
         * Registra la invocación de la operación de finalización del scrim.
         *
         * @param ctx contexto que solicita la operación
         */
        @Override
        public void finalizar(ScrimContext ctx) {
            this.finalizarLlamado = true;
            this.ultimoContexto = ctx;
        }

        /**
         * Registra la invocación de la operación de cancelación del scrim.
         *
         * @param ctx contexto que solicita la operación
         */
        @Override
        public void cancelar(ScrimContext ctx) {
            this.cancelarLlamado = true;
            this.ultimoContexto = ctx;
        }

        /**
         * Devuelve el nombre identificador del estado simulado.
         *
         * @return nombre del estado de prueba
         */
        @Override
        public String getNombreEstado() {
            return "SPY_STATE";
        }
    }
}
