package com.escrims.infrastructure.adapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Publica los .ics como archivos dentro de un directorio */
public class ArchivoICalDestino implements ICalDestino {

    private final Path directorio;

    public ArchivoICalDestino(Path directorio) {
        this.directorio = Objects.requireNonNull(directorio, "directorio");
    }

    @Override
    public void publicar(String nombreArchivo, String contenidoIcs) {
        try {
            Files.createDirectories(directorio);
            Files.writeString(directorio.resolve(nombreArchivo), contenidoIcs, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir " + nombreArchivo, e);
        }
    }
}
