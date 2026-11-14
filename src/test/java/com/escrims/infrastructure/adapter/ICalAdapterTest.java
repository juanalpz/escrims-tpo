package com.escrims.infrastructure.adapter;

import com.escrims.domain.entities.Formato;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ICalAdapterTest {

    private static final ZoneId BUENOS_AIRES = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);

    private Map<String, String> publicados;
    private ICalAdapter adapter;

    @BeforeEach
    void setUp() {
        publicados = new LinkedHashMap<>();
        adapter = new ICalAdapter(publicados::put, BUENOS_AIRES, 2, RELOJ);
    }

    private static Scrim scrim(LocalDateTime fechaHora, Integer duracion) {
        return new Scrim(new Usuario("capitan,1", 1500, 30, null), Juego.VALORANT, Formato.CINCO_VS_CINCO,
                null, null, null, null, fechaHora, duracion, null, 10, null);
    }

    private static List<String> lineas(String ics) {
        return List.of(ics.split("\r\n"));
    }

    @Test
    @DisplayName("Genera un VEVENT con inicio y fin en UTC y recordatorio N horas antes")
    void generaEventoConRecordatorio() {
        Scrim scrim = scrim(LocalDateTime.of(2026, 11, 20, 21, 0), 90);

        adapter.sincronizar(scrim);

        String ics = publicados.get("scrim-" + scrim.getId() + ".ics");
        assertNotNull(ics);
        List<String> lineas = lineas(ics);
        assertEquals("BEGIN:VCALENDAR", lineas.get(0));
        assertEquals("END:VCALENDAR", lineas.get(lineas.size() - 1));
        assertTrue(lineas.contains("VERSION:2.0"));
        assertTrue(lineas.contains("METHOD:PUBLISH"));
        assertTrue(lineas.contains("UID:" + scrim.getId() + "@escrims.com"));
        assertTrue(lineas.contains("SEQUENCE:0"));
        assertTrue(lineas.contains("DTSTAMP:20261114T120000Z"));
        // 21:00 en Buenos Aires (UTC-3) = 00:00 UTC del día siguiente
        assertTrue(lineas.contains("DTSTART:20261121T000000Z"));
        assertTrue(lineas.contains("DTEND:20261121T013000Z"));
        assertTrue(lineas.contains("SUMMARY:Scrim VALORANT 5v5"));
        assertTrue(lineas.contains("STATUS:CONFIRMED"));
        assertTrue(lineas.contains("BEGIN:VALARM"));
        assertTrue(lineas.contains("TRIGGER:-PT2H"));
        assertTrue(lineas.contains("END:VALARM"));
        assertEquals(ics, adapter.obtenerIcs(scrim.getId()).orElseThrow());
    }

    @Test
    @DisplayName("Escapa los caracteres reservados de iCal en los textos")
    void escapaTextos() {
        Scrim scrim = scrim(LocalDateTime.of(2026, 11, 20, 21, 0), 60);

        adapter.sincronizar(scrim);

        String ics = adapter.obtenerIcs(scrim.getId()).orElseThrow();
        assertTrue(ics.contains("DESCRIPTION:Scrim VALORANT 5v5\\nOrganizador: capitan\\,1"));
        assertEquals("a\\\\b\\;c\\,d\\ne", ICalAdapter.escapar("a\\b;c,d\ne"));
    }

    @Test
    @DisplayName("Usa 60 minutos cuando el scrim no define duración")
    void duracionPorDefecto() {
        Scrim scrim = scrim(LocalDateTime.of(2026, 11, 20, 21, 0), null);

        adapter.sincronizar(scrim);

        assertTrue(lineas(adapter.obtenerIcs(scrim.getId()).orElseThrow()).contains("DTEND:20261121T010000Z"));
    }

    @Test
    @DisplayName("Al cancelar publica el mismo evento como CANCELLED, sin alarma y con SEQUENCE mayor")
    void cancelaEvento() {
        Scrim scrim = scrim(LocalDateTime.of(2026, 11, 20, 21, 0), 60);
        adapter.sincronizar(scrim);

        adapter.cancelar(scrim);

        List<String> lineas = lineas(adapter.obtenerIcs(scrim.getId()).orElseThrow());
        assertTrue(lineas.contains("METHOD:CANCEL"));
        assertTrue(lineas.contains("STATUS:CANCELLED"));
        assertTrue(lineas.contains("UID:" + scrim.getId() + "@escrims.com"));
        assertTrue(lineas.contains("SEQUENCE:1"));
        assertFalse(lineas.contains("BEGIN:VALARM"));
        assertEquals(1, publicados.size(), "Debe reemplazar el mismo archivo");
    }

    @Test
    @DisplayName("Pliega las líneas para que ninguna supere los 75 octetos")
    void pliegaLineasLargas() {
        Scrim scrim = new Scrim(new Usuario("organizador-con-un-nombre-muy-largo-ñandú-".repeat(4), 1500, 30, null),
                Juego.LOL, Formato.UNO_VS_UNO, null, null, null, null,
                LocalDateTime.of(2026, 11, 20, 21, 0), 60, null, 2, null);

        adapter.sincronizar(scrim);

        String ics = adapter.obtenerIcs(scrim.getId()).orElseThrow();
        for (String linea : lineas(ics)) {
            assertTrue(linea.getBytes(StandardCharsets.UTF_8).length <= 75, "Línea demasiado larga: " + linea);
        }
        assertTrue(ics.replace("\r\n ", "").contains("ñandú-organizador-con-un-nombre-muy-largo"));
    }

    @Test
    @DisplayName("Rechaza scrims sin fecha y recordatorios inválidos")
    void validaciones() {
        assertThrows(IllegalArgumentException.class, () -> adapter.sincronizar(scrim(null, 60)));
        assertTrue(publicados.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new ICalAdapter(publicados::put, BUENOS_AIRES, 0));
    }

    @Test
    @DisplayName("ArchivoICalDestino escribe el .ics en el directorio indicado")
    void escribeArchivo(@TempDir Path dir) throws IOException {
        Path carpeta = dir.resolve("calendarios");
        ICalAdapter aDisco = new ICalAdapter(new ArchivoICalDestino(carpeta), BUENOS_AIRES, 2, RELOJ);
        Scrim scrim = scrim(LocalDateTime.of(2026, 11, 20, 21, 0), 60);

        aDisco.sincronizar(scrim);

        Path archivo = carpeta.resolve(ICalAdapter.nombreArchivo(scrim.getId()));
        assertEquals(aDisco.obtenerIcs(scrim.getId()).orElseThrow(), Files.readString(archivo));
    }
}
