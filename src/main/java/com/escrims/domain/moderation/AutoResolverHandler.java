package com.escrims.domain.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.function.ToIntFunction;

/**
 * Primer eslabón del patrón Chain of Responsibility: cierra sin intervención
 * los reportes que no ameritan sanción.
 *
 * <ol>
 *   <li>Reportes improcedentes: el reportado no participó del scrim.</li>
 *   <li>Primer reporte contra un jugador sin strikes: se desestima.</li>
 * </ol>
 * El resto se deriva al siguiente manejador.
 */
public class AutoResolverHandler extends ReportHandler {

    private final ToIntFunction<Usuario> reportesPrevios;

    /**
     * Crea el manejador sin historial de reportes: todo reporte se trata
     * como el primero que recibe el jugador.
     */
    public AutoResolverHandler() {
        this(usuario -> 0);
    }

    /**
     * Crea el manejador con acceso a la cantidad de reportes anteriores de cada jugador.
     *
     * @param reportesPrevios función que devuelve cuántos reportes ya se
     *                        registraron contra un usuario, sin contar el actual
     * @throws IllegalArgumentException si la función es nula
     */
    public AutoResolverHandler(ToIntFunction<Usuario> reportesPrevios) {
        if (reportesPrevios == null) {
            throw new IllegalArgumentException("La consulta de reportes previos es obligatoria.");
        }
        this.reportesPrevios = reportesPrevios;
    }

    /**
     * Desestima el reporte si es improcedente o si es el primero contra
     * un jugador sin antecedentes; en otro caso lo deja sin resolver.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado sin sanción si el reporte se desestima;
     *         resultado sin resolver en caso contrario
     * @throws IllegalStateException si el reporte ya fue resuelto o rechazado
     */
    @Override
    protected ResultadoModeracion intentarResolver(ReporteConducta reporte) {
        Usuario reportado = reporte.getReportado();

        if (!participoDelScrim(reporte.getScrim(), reportado)) {
            reporte.rechazar();
            return ResultadoModeracion.resueltoSinSancion(
                    "Auto-resuelto: reporte improcedente, el reportado no participó del scrim.");
        }

        if (reportado.getStrikes() == 0 && reportesPrevios.applyAsInt(reportado) == 0) {
            reporte.rechazar();
            return ResultadoModeracion.resueltoSinSancion(
                    "Auto-resuelto: primer reporte contra un jugador sin historial negativo.");
        }
        return ResultadoModeracion.sinResolver();
    }

    private boolean participoDelScrim(Scrim scrim, Usuario usuario) {
        return usuario.equals(scrim.getOrganizador()) || scrim.esParticipante(usuario);
    }
}
