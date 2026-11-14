package com.escrims.infrastructure.adapter;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.Notificacion;
import com.escrims.infrastructure.http.HttpTransport;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * «Adapter» Traduce una {@link Notificacion} a un mensaje de la API HTTP v1 de
 * Firebase Cloud Messaging. El destinatario es el token del dispositivo
 */
public class FirebasePushAdapter extends HttpNotifierAdapter {

    private final String projectId;
    private final Supplier<String> accessToken;

    /**
     * @param accessToken proveedor del token OAuth2 de la cuenta de servicio; se
     *                    consulta en cada envío porque el token vence
     */
    public FirebasePushAdapter(HttpTransport transport, String projectId, Supplier<String> accessToken) {
        super(transport);
        this.projectId = requerido(projectId, "el id del proyecto de Firebase");
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken");
    }

    @Override
    public Canal canal() {
        return Canal.PUSH;
    }

    @Override
    protected String proveedor() {
        return "Firebase";
    }

    @Override
    protected String url() {
        return "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";
    }

    @Override
    protected Map<String, String> headers() {
        return Map.of("Authorization", "Bearer " + accessToken.get());
    }

    @Override
    protected String cuerpo(Notificacion n) {
        return "{\"message\":{\"token\":" + Json.texto(destinatario(n)) + ","
                + "\"notification\":{\"title\":" + Json.texto(n.getTitulo()) + ","
                + "\"body\":" + Json.texto(n.getPayload()) + "},"
                + "\"data\":{\"tipo\":" + Json.texto(n.getTipo()) + "}}}";
    }
}
