package com.escrims.infrastructure.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/** Implementacion real de {@link HttpTransport} sobre {@code java.net.http} */
public class JavaHttpTransport implements HttpTransport {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    @Override
    public int postJson(String url, Map<String, String> headers, String body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        headers.forEach(request::header);
        try {
            return client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException("Envío interrumpido", e));
        }
    }
}
