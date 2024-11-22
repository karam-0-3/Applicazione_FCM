package com.example.mobifog_fcm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class WebhookClient {
    private static final String TAG = "WebhookClient";
    private final Context context;

    public WebhookClient(Context context) {
        this.context = context;
    }


     //Metodo per avviare il RegisterWebhookWorker e registrare il webhook.

    public void registerWebhook() {
        OneTimeWorkRequest registerWebhookRequest = new OneTimeWorkRequest.Builder(RegisterWebhookWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED) // Richiede una connessione a Internet
                        .build())
                .build();

        WorkManager.getInstance(context).enqueue(registerWebhookRequest);
        Log.d(TAG, "RegisterWebhookWorker avviato per registrare il webhook.");
    }


     //Metodo per avviare il servizio FCM (TaskMessagingService).
    public void startFcmService() {
        Intent intent = new Intent(context, TaskMessagingService.class);
        context.startService(intent);
        Log.d(TAG, "TaskMessagingService avviato.");
    }

    //Metodo per fermare il servizio FCM (TaskMessagingService).
    public static void stopFcmService(Context context) {
        Intent intent = new Intent(context, TaskMessagingService.class);
        context.stopService(intent);
        Log.d(TAG, "TaskMessagingService terminato.");
    }
}

