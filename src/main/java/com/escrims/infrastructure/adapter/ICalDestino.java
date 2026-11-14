package com.escrims.infrastructure.adapter;

/**
 * Lugar donde {@link ICalAdapter} publica los archivos .ics generados
 * (carpeta servida por HTTP, adjunto de email, almacenamiento en la nube...).
 */
@FunctionalInterface
public interface ICalDestino {

    /**
     * @param nombreArchivo nombre del archivo, ej. {@code scrim-<uuid>.ics}
     * @param contenidoIcs  calendario completo en formato iCalendar (RFC 5545)
     */
    void publicar(String nombreArchivo, String contenidoIcs);
}
