package com.example.mobifog_fcm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TaskMessagingService extends FirebaseMessagingService {
    private static final String TAG = "TaskMessagingService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Messaggio ricevuto: " + remoteMessage.getData());

        // Estrai i dati dal messaggio
        String taskId = remoteMessage.getData().get("task_id");
        if (taskId != null) {
            // Passaggio del task_id al Worker tramite Data
            Data inputData = new Data.Builder()
                    .putString("task_id", taskId)
                    .build();

            // Creazione di una OneTimeWorkRequest per l'esecuzione della task
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TaskWorker.class)
                    .setInputData(inputData)
                    .build();
            // Avvio del TaskWorker
            WorkManager.getInstance(this).enqueue(workRequest);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Nuovo token: " + token);
        WorkRequest pollingWorkRequest = new OneTimeWorkRequest.Builder(SendNewTokenWorker.class)
                .setConstraints(new Constraints.Builder() // Vincolo di connessione a internet
                .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(pollingWorkRequest);
    }

}

