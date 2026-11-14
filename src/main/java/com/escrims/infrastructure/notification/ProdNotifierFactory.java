package com.escrims.infrastructure.notification;

import com.escrims.domain.notification.Notifier;
import com.escrims.domain.notification.NotifierFactory;
import com.escrims.infrastructure.adapter.DiscordAdapter;
import com.escrims.infrastructure.adapter.FirebasePushAdapter;
import com.escrims.infrastructure.adapter.SendGridAdapter;
import com.escrims.infrastructure.http.HttpTransport;
import com.escrims.infrastructure.http.JavaHttpTransport;

import java.util.Objects;

/**
 * «Concrete Factory» Entorno de produccion: cada canal se resuelve con el
 * adaptador del proveedor real (Firebase, SendGrid, Discord).
 */
public class ProdNotifierFactory implements NotifierFactory {

    private final ProdNotifierConfig config;
    private final HttpTransport transport;

    public ProdNotifierFactory(ProdNotifierConfig config) {
        this(config, new JavaHttpTransport());
    }

    /** Permite inyectar el transporte HTTP (útil para tests). */
    public ProdNotifierFactory(ProdNotifierConfig config, HttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    @Override
    public Notifier createPush() {
        return new FirebasePushAdapter(transport, config.firebaseProjectId(), config.firebaseAccessToken());
    }

    @Override
    public Notifier createEmail() {
        return new SendGridAdapter(transport, config.sendGridApiKey(), config.emailRemitente());
    }

    @Override
    public Notifier createChat() {
        return new DiscordAdapter(transport, config.discordWebhookUrl());
    }
}
