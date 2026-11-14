package com.escrims.infrastructure.adapter;

import com.escrims.domain.calendar.CalendarioService;
import com.escrims.domain.entities.Scrim;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * «Adapter» Traduce un {@link Scrim} del dominio a un evento iCalendar
 * (RFC 5545) que cualquier cliente de calendario puede importar o suscribir
 * (Google Calendar, Outlook, Apple Calendar)
 *
 * Cada evento incluye una alarma (VALARM) que dispara el recordatorio
 * automático N horas antes del inicio.
 */
public class ICalAdapter implements CalendarioService {

    static final int DURACION_POR_DEFECTO_MIN = 60;

    private static final String CRLF = "\r\n";
    private static final DateTimeFormatter UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private final ICalDestino destino;
    private final ZoneId zona;
    private final int horasRecordatorio;
    private final Clock reloj;

    /** Último .ics generado y número de revisión (SEQUENCE) por scrim. */
    private final Map<UUID, String> calendarios = new HashMap<>();
    private final Map<UUID, Integer> secuencias = new HashMap<>();

    /**
     * @param destino           dónde se publican los .ics generados
     * @param zona              zona horaria en la que está expresada la
     *                          fecha/hora de los scrims
     * @param horasRecordatorio cuántas horas antes del inicio suena el
     *                          recordatorio
     */
    public ICalAdapter(ICalDestino destino, ZoneId zona, int horasRecordatorio) {
        this(destino, zona, horasRecordatorio, Clock.systemUTC());
    }

    ICalAdapter(ICalDestino destino, ZoneId zona, int horasRecordatorio, Clock reloj) {
        if (horasRecordatorio < 1) {
            throw new IllegalArgumentException("El recordatorio debe ser de al menos 1 hora antes.");
        }
        this.destino = Objects.requireNonNull(destino, "destino");
        this.zona = Objects.requireNonNull(zona, "zona");
        this.horasRecordatorio = horasRecordatorio;
        this.reloj = Objects.requireNonNull(reloj, "reloj");
    }

    @Override
    public void sincronizar(Scrim scrim) {
        publicar(scrim, false);
    }

    @Override
    public void cancelar(Scrim scrim) {
        publicar(scrim, true);
    }

    /** Último .ics publicado para el scrim, si se sincronizó alguna vez. */
    public Optional<String> obtenerIcs(UUID scrimId) {
        return Optional.ofNullable(calendarios.get(scrimId));
    }

    public static String nombreArchivo(UUID scrimId) {
        return "scrim-" + scrimId + ".ics";
    }

    private void publicar(Scrim scrim, boolean cancelado) {
        Objects.requireNonNull(scrim, "scrim");
        if (scrim.getFechaHora() == null) {
            throw new IllegalArgumentException("El scrim no tiene fecha y hora: no se puede agendar.");
        }
        // SEQUENCE arranca en 0 y crece con cada cambio: así el cliente de
        // calendario sabe que debe reemplazar la versión anterior del evento.
        int secuencia = secuencias.merge(scrim.getId(), 1, Integer::sum) - 1;
        String ics = generar(scrim, cancelado, secuencia);
        calendarios.put(scrim.getId(), ics);
        destino.publicar(nombreArchivo(scrim.getId()), ics);
    }

    private String generar(Scrim scrim, boolean cancelado, int secuencia) {
        Instant inicio = scrim.getFechaHora().atZone(zona).toInstant();
        int duracion = scrim.getDuracion() != null ? scrim.getDuracion() : DURACION_POR_DEFECTO_MIN;
        Instant fin = inicio.plusSeconds(duracion * 60L);
        String resumen = resumen(scrim);

        StringBuilder sb = new StringBuilder();
        linea(sb, "BEGIN:VCALENDAR");
        linea(sb, "VERSION:2.0");
        linea(sb, "PRODID:-//eScrims//Scrims//ES");
        linea(sb, "CALSCALE:GREGORIAN");
        linea(sb, "METHOD:" + (cancelado ? "CANCEL" : "PUBLISH"));
        linea(sb, "BEGIN:VEVENT");
        linea(sb, "UID:" + scrim.getId() + "@escrims.com");
        linea(sb, "SEQUENCE:" + secuencia);
        linea(sb, "DTSTAMP:" + UTC.format(reloj.instant()));
        linea(sb, "DTSTART:" + UTC.format(inicio));
        linea(sb, "DTEND:" + UTC.format(fin));
        linea(sb, "SUMMARY:" + escapar(resumen));
        linea(sb, "DESCRIPTION:" + escapar(descripcion(scrim)));
        linea(sb, "STATUS:" + (cancelado ? "CANCELLED" : "CONFIRMED"));
        if (!cancelado) {
            linea(sb, "BEGIN:VALARM");
            linea(sb, "ACTION:DISPLAY");
            linea(sb, "DESCRIPTION:" + escapar("Recordatorio: " + resumen));
            linea(sb, "TRIGGER:-PT" + horasRecordatorio + "H");
            linea(sb, "END:VALARM");
        }
        linea(sb, "END:VEVENT");
        linea(sb, "END:VCALENDAR");
        return sb.toString();
    }

    private static String resumen(Scrim scrim) {
        int porLado = scrim.getFormato().getJugadoresPorLado();
        return "Scrim " + scrim.getJuego() + " " + porLado + "v" + porLado;
    }

    private static String descripcion(Scrim scrim) {
        StringBuilder sb = new StringBuilder(resumen(scrim));
        if (scrim.getRegion() != null) {
            sb.append("\nRegión: ").append(scrim.getRegion());
        }
        if (scrim.getModalidad() != null) {
            sb.append("\nModalidad: ").append(scrim.getModalidad());
        }
        if (scrim.getOrganizador() != null) {
            sb.append("\nOrganizador: ").append(scrim.getOrganizador().getUsername());
        }
        return sb.toString();
    }

    /** Escapa los caracteres reservados de los valores TEXT (RFC 5545 §3.3.11). */
    static String escapar(String texto) {
        return texto.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }

    /**
     * Agrega una línea de contenido plegándola a 75 octetos como exige el
     * RFC 5545 §3.1 (las continuaciones empiezan con un espacio).
     */
    private static void linea(StringBuilder sb, String contenido) {
        int octetos = 0;
        for (int i = 0; i < contenido.length(); ) {
            int codePoint = contenido.codePointAt(i);
            String caracter = new String(Character.toChars(codePoint));
            int largo = caracter.getBytes(StandardCharsets.UTF_8).length;
            if (octetos + largo > 75) {
                sb.append(CRLF).append(' ');
                octetos = 1;
            }
            sb.append(caracter);
            octetos += largo;
            i += Character.charCount(codePoint);
        }
        sb.append(CRLF);
    }
}
