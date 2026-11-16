package com.escrims.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica las reglas de negocio y las invariantes de las entidades
 * del dominio de eScrims mediante pruebas unitarias con JUnit 5.
 */
class DominioEntidadesTest {

    /**
     * Agrupa las pruebas de sanciones, historial, Fair Play
     * e identidad de la entidad Usuario.
     */
    @Nested
    @DisplayName("Pruebas de Usuario")
    class UsuarioTests {

        /**
         * Verifica la acumulación de strikes y la detección
         * de sanciones con períodos de cooldown vigentes.
         */
        @Test
        @DisplayName("Cálculo de strikes acumulados y detección de sanciones activas")
        void deberiaCalcularStrikesYDetectarSancionActiva() {
            Usuario u = new Usuario("gamer1", 1500, 30, Region.LAS);
            assertEquals(0, u.getStrikes());
            assertFalse(u.tieneCooldownActivo());
            u.agregarSancion(new Sancion(2, LocalDateTime.now().plusDays(2)));
            u.agregarSancion(new Sancion(1, LocalDateTime.now().minusDays(1))); // Pasada
            assertEquals(3, u.getStrikes());
            assertTrue(u.tieneCooldownActivo());
        }

        /**
         * Verifica el cálculo de la tasa de abandono y el rechazo
         * de historiales con cantidades de partidas inconsistentes.
         */
        @Test
        @DisplayName("Historial y tasa de abandono")
        void deberiaCalcularTasaAbandonoCorrectamente() {
            Usuario u = new Usuario("gamer2", 1200, 45, Region.SA);
            assertEquals(0.0, u.getTasaAbandono());
            u.registrarHistorial(10, 2);
            assertEquals(0.2, u.getTasaAbandono(), 1e-9);
            assertThrows(IllegalArgumentException.class, () -> u.registrarHistorial(5, 6));
            assertThrows(IllegalArgumentException.class, () -> u.registrarHistorial(-1, 0));
        }

        /**
         * Verifica que el rating de Fair Play acepte valores válidos
         * y rechace puntuaciones fuera del intervalo permitido.
         */
        @Test
        @DisplayName("Validación de rating de Fair Play")
        void deberiaValidarLimitesDeRating() {
            Usuario u = new Usuario("gamer3", 1400, 25, Region.LAS);
            u.setRatingFairPlay(4.5);
            assertEquals(4.5, u.getRatingFairPlay());
            assertThrows(IllegalArgumentException.class, () -> u.setRatingFairPlay(-0.1));
            assertThrows(IllegalArgumentException.class, () -> u.setRatingFairPlay(5.1));
        }

        /**
         * Verifica que dos usuarios distintos no sean iguales
         * y que una instancia sea igual a sí misma.
         */
        @Test
        @DisplayName("Igualdad basada exclusivamente en ID")
        void deberiaCompararPorId() {
            Usuario u1 = new Usuario("userA", 1500, 30, Region.LAS);
            Usuario u2 = new Usuario("userB", 1500, 30, Region.LAS);
            assertNotEquals(u1, u2);
            assertEquals(u1, u1);
        }
    }

    /**
     * Agrupa las pruebas de incorporación, asignación de roles
     * y eliminación de jugadores en la entidad Equipo.
     */
    @Nested
    @DisplayName("Pruebas de Equipo")
    class EquipoTests {

        /**
         * Verifica la incorporación de jugadores, la asignación de roles,
         * la prevención de duplicados y el respeto del cupo máximo.
         */
        @Test
        @DisplayName("Agrega jugadores y asigna roles respetando cupos")
        void deberiaGestionarJugadoresYRoles() {
            Equipo equipo = new Equipo(LadoEquipo.EQUIPO_A, 2);
            Usuario u1 = new Usuario("jugador1", 1500, 30, Region.LAS);
            Usuario u2 = new Usuario("jugador2", 1600, 35, Region.LAS);
            assertTrue(equipo.agregarJugador(u1, RolJuego.DUELISTA));
            assertEquals(RolJuego.DUELISTA, equipo.getRolesAsignados().get(u1));

            // Set previene duplicados
            assertFalse(equipo.agregarJugador(u1, RolJuego.DUELISTA));
            assertTrue(equipo.agregarJugador(u2, RolJuego.CONTROLADOR));
            assertTrue(equipo.estaCompleto());

            // Superar cupo máximo lanza excepción
            Usuario u3 = new Usuario("jugador3", 1400, 40, Region.LAS);
            assertThrows(IllegalStateException.class, () -> equipo.agregarJugador(u3, RolJuego.INICIADOR));
        }

        /**
         * Verifica la reasignación de roles y el rechazo de operaciones
         * sobre jugadores ajenos al equipo o con roles nulos.
         */
        @Test
        @DisplayName("Reasigna rol a jugador existente y falla si no pertenece al equipo")
        void deberiaReasignarRolCorrectamente() {
            Equipo equipo = new Equipo(LadoEquipo.EQUIPO_B, 3);
            Usuario u1 = new Usuario("jugador1", 1500, 30, Region.LAS);
            Usuario u2 = new Usuario("jugador2", 1600, 35, Region.LAS);
            equipo.agregarJugador(u1, RolJuego.DUELISTA);
            equipo.asignarRol(u1, RolJuego.CENTINELA);
            assertEquals(RolJuego.CENTINELA, equipo.getRolesAsignados().get(u1));
            assertThrows(IllegalStateException.class, () -> equipo.asignarRol(u2, RolJuego.SUPPORT));
            assertThrows(IllegalArgumentException.class, () -> equipo.asignarRol(u1, null));
        }

        /**
         * Verifica que la eliminación de un jugador también
         * elimine su asignación de rol dentro del equipo.
         */
        @Test
        @DisplayName("Remueve jugador y limpia su rol asignado")
        void deberiaRemoverJugadorYRol() {
            Equipo equipo = new Equipo(LadoEquipo.EQUIPO_A, 2);
            Usuario u1 = new Usuario("jugador1", 1500, 30, Region.LAS);
            equipo.agregarJugador(u1, RolJuego.DUELISTA);
            assertTrue(equipo.removerJugador(u1));
            assertFalse(equipo.getJugadores().contains(u1));
            assertNull(equipo.getRolesAsignados().get(u1));
        }
    }

    /**
     * Agrupa las pruebas de creación, aceptación y rechazo
     * de postulaciones según los requisitos del scrim.
     */
    @Nested
    @DisplayName("Pruebas de Postulación")
    class PostulacionTests {

        /**
         * Crea un scrim de prueba con requisitos de rango y latencia.
         *
         * @return scrim configurado para evaluar postulaciones
         */
        private Scrim crearScrim() {
            return new Scrim(Juego.VALORANT, Formato.CINCO_VS_CINCO, Region.LAS,
                    Rango.ORO, Rango.PLATINO, 80);
        }

        /**
         * Verifica que una postulación válida se cree en estado
         * PENDIENTE y pueda pasar al estado ACEPTADA.
         */
        @Test
        @DisplayName("Postulación exitosa en estado PENDIENTE cuando usuario cumple requisitos")
        void deberiaCrearPostulacionPendiente() {
            Scrim scrim = crearScrim();
            Usuario u = new Usuario("candidato", 1400, 40, Region.LAS);
            u.setEmailVerificado(true);
            Postulacion p = new Postulacion(u, scrim, RolJuego.DUELISTA);
            assertEquals(EstadoPostulacion.PENDIENTE, p.getEstado());
            p.aceptar();
            assertEquals(EstadoPostulacion.ACEPTADA, p.getEstado());
        }

        /**
         * Verifica el rechazo automático de postulaciones
         * realizadas por usuarios sin correo electrónico verificado.
         */
        @Test
        @DisplayName("Rechazo automático al postularse si usuario no tiene email verificado")
        void deberiaRechazarSiEmailNoVerificado() {
            Scrim scrim = crearScrim();
            Usuario u = new Usuario("noVerif", 1400, 40, Region.LAS);
            u.setEmailVerificado(false);
            Postulacion p = new Postulacion(u, scrim, RolJuego.DUELISTA);
            assertEquals(EstadoPostulacion.RECHAZADA, p.getEstado());
        }

        /**
         * Verifica el rechazo automático cuando la latencia
         * del usuario supera el máximo permitido por el scrim.
         */
        @Test
        @DisplayName("Rechazo automático al postularse si usuario supera latencia máxima del scrim")
        void deberiaRechazarSiSuperaLatencia() {
            Scrim scrim = crearScrim(); // Max 80ms
            Usuario u = new Usuario("lag", 1400, 120, Region.LAS);
            u.setEmailVerificado(true);
            Postulacion p = new Postulacion(u, scrim, RolJuego.DUELISTA);
            assertEquals(EstadoPostulacion.RECHAZADA, p.getEstado());
        }

        /**
         * Verifica el rechazo automático de postulaciones
         * cuyo MMR se encuentra fuera del rango permitido.
         */
        @Test
        @DisplayName("Rechazo automático si usuario está fuera de rango")
        void deberiaRechazarSiFueraDeRango() {
            Scrim scrim = crearScrim(); // Oro (1200) a Platino (1800)
            Usuario u = new Usuario("silver", 1000, 30, Region.LAS);
            u.setEmailVerificado(true);
            Postulacion p = new Postulacion(u, scrim, RolJuego.DUELISTA);
            assertEquals(EstadoPostulacion.RECHAZADA, p.getEstado());
        }
    }

    /**
     * Agrupa las pruebas de confirmación de asistencia
     * y prevención de confirmaciones duplicadas.
     */
    @Nested
    @DisplayName("Pruebas de Confirmación")
    class ConfirmacionTests {

        /**
         * Verifica el registro de una confirmación, su fecha
         * y el rechazo de una segunda confirmación.
         */
        @Test
        @DisplayName("Confirma asistencia exitosamente y previene doble confirmación")
        void deberiaConfirmarYBloquearSegundaConfirmacion() {
            Scrim scrim = new Scrim(Juego.CS2, Formato.CINCO_VS_CINCO, Region.LAS, null, null, 0);
            Usuario u = new Usuario("confirmador", 1500, 30, Region.LAS);
            Confirmacion c = new Confirmacion(u, scrim);
            assertFalse(c.isConfirmado());
            assertNull(c.getFechaConfirmacion());
            c.confirmar();
            assertTrue(c.isConfirmado());
            assertNotNull(c.getFechaConfirmacion());
            assertThrows(IllegalStateException.class, c::confirmar);
        }
    }

    /**
     * Agrupa las pruebas de cálculo de estadísticas
     * y validación de resultados individuales.
     */
    @Nested
    @DisplayName("Pruebas de Estadística")
    class EstadisticaTests {

        /**
         * Verifica el cálculo del ratio KDA, su representación
         * textual y el tratamiento de partidas sin muertes.
         */
        @Test
        @DisplayName("Cálculo de ratio KDA sin división por cero")
        void deberiaCalcularRatioKDASinDivisionPorCero() {
            Scrim scrim = new Scrim(Juego.LOL, Formato.CINCO_VS_CINCO, Region.LAS, null, null, 0);
            Usuario u = new Usuario("faker", 2500, 20, Region.LAS);

            // deaths == 0: Math.max(1, 0) -> divide por 1
            Estadistica estInmortal = new Estadistica(scrim, u, 10, 0, 5, true, "Perfect Game");
            assertEquals(15.0, estInmortal.calcularRatioKDA());
            assertEquals("10/0/5", estInmortal.getKdaFormateado());
            assertTrue(estInmortal.isMvp());

            // Partida regular
            Estadistica estNormal = new Estadistica(scrim, u, 14, 2, 8, false, null);
            assertEquals(11.0, estNormal.calcularRatioKDA());
            assertEquals("14/2/8", estNormal.getKdaFormateado());
        }

        /**
         * Verifica que la creación de estadísticas rechace
         * cantidades negativas de eliminaciones.
         */
        @Test
        @DisplayName("Falla si se pasan valores negativos de KDA")
        void deberiaFallarConValoresNegativos() {
            Scrim scrim = new Scrim(Juego.LOL, Formato.CINCO_VS_CINCO, Region.LAS, null, null, 0);
            Usuario u = new Usuario("player", 1500, 30, Region.LAS);
            assertThrows(IllegalArgumentException.class,
                    () -> new Estadistica(scrim, u, -1, 2, 3, false, null));
        }
    }

    /**
     * Agrupa las pruebas del ciclo de vida de los reportes
     * de conducta y la aplicación de sanciones.
     */
    @Nested
    @DisplayName("Pruebas de Reporte de Conducta")
    class ReporteConductaTests {

        /**
         * Verifica la transición de un reporte desde PENDIENTE
         * hasta RESUELTO y la aplicación de la sanción correspondiente.
         */
        @Test
        @DisplayName("Flujo completo: PENDIENTE -> EN_REVISION -> RESUELTO con sanción")
        void deberiaResolverReporteConSancion() {
            Scrim scrim = new Scrim(Juego.VALORANT, Formato.CINCO_VS_CINCO, Region.LAS, null, null, 0);
            Usuario reportante = new Usuario("victima", 1500, 30, Region.LAS);
            Usuario reportado = new Usuario("toxico", 1500, 30, Region.LAS);
            ReporteConducta rep = new ReporteConducta(scrim, reportante, reportado, "AFK intencional");
            assertEquals(EstadoReporte.PENDIENTE, rep.getEstado());
            rep.escalarARevision();
            assertEquals(EstadoReporte.EN_REVISION, rep.getEstado());
            Sancion sancion = new Sancion(1, LocalDateTime.now().plusDays(3));
            rep.resolverConSancion(sancion);
            assertEquals(EstadoReporte.RESUELTO, rep.getEstado());
            assertEquals(sancion, rep.getSancion());
            assertTrue(reportado.tieneCooldownActivo());
            assertEquals(1, reportado.getStrikes());
        }

        /**
         * Verifica que no se permita crear un reporte
         * cuando el reportante y el reportado son el mismo usuario.
         */
        @Test
        @DisplayName("Falla si un usuario intenta reportarse a sí mismo")
        void deberiaRechazarAutoReporte() {
            Scrim scrim = new Scrim(Juego.VALORANT, Formato.CINCO_VS_CINCO, Region.LAS, null, null, 0);
            Usuario user = new Usuario("mismo", 1500, 30, Region.LAS);
            assertThrows(IllegalArgumentException.class,
                    () -> new ReporteConducta(scrim, user, user, "Auto-denuncia"));
        }

        /**
         * Verifica que un reporte rechazado no permita
         * nuevas operaciones de rechazo ni resolución con sanción.
         */
        @Test
        @DisplayName("Falla si se intenta modificar un reporte ya resuelto")
        void deberiaBloquearAccionesEnReporteResuelto() {
            Scrim scrim = new Scrim(Juego.VALORANT, Formato.CINCO_VS_CINCO, Region.LAS, null, null, 0);
            Usuario u1 = new Usuario("u1", 1500, 30, Region.LAS);
            Usuario u2 = new Usuario("u2", 1500, 30, Region.LAS);
            ReporteConducta rep = new ReporteConducta(scrim, u1, u2, "No-show");
            rep.rechazar();
            assertEquals(EstadoReporte.RECHAZADO, rep.getEstado());
            assertThrows(IllegalStateException.class, rep::rechazar);
            assertThrows(IllegalStateException.class,
                    () -> rep.resolverConSancion(new Sancion(1, LocalDateTime.now().plusHours(1))));
        }
    }
}
