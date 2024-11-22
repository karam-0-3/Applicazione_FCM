package com.example.mobifog_fcm;

public class WebhookRegistrationBuilder {
    final String node_url;
    final String event_types;
    final String fcmToken;

    public WebhookRegistrationBuilder(String node_url, String event_types, String fcmToken) {
        this.node_url = node_url;
        this.event_types = event_types;
        this.fcmToken = fcmToken;
    }
}
