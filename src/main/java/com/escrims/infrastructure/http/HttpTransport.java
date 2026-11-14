package com.escrims.infrastructure.http;

import java.util.Map;

/**
 * Transporte HTTP minimo que usan los adaptadores para hablar con las APIs
 * externas. Permite reemplazar la red por un fake en los tests
 */
public interface HttpTransport {

    /**
     * Ejecuta un POST con cuerpo JSON.
     *
     * @return código de estado HTTP de la respuesta
     * @throws java.io.UncheckedIOException si falla la conexión
     */
    int postJson(String url, Map<String, String> headers, String body);
}
