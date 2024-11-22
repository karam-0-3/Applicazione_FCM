package com.example.mobifog_fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TaskWorker extends Worker {
    private static final String TAG = "TaskWorker"; // Tag per il log
    private final OkHttpClient client;
    private final String ForchUrl;
    private static final String CHANNEL_ID = "PollingWorkerChannel"; // ID per il canale di notifica

    public TaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs",
                Context.MODE_PRIVATE);
        this.ForchUrl = sharedPreferences.getString("ForchUrl", "");
        // Inizializzazione del client HTTP singleton
        this.client = HttpClientSingleton.INSTANCE.getClient();
        createNotificationChannel();
    }

    @NonNull
    @Override
    public Result doWork() {

        setForegroundAsync(getForegroundInfo());
        String taskId = getInputData().getString("task_id");

        if (taskId == null) {
            Log.e(TAG, "Task ID non fornito.");
            return Result.failure();
        }

        boolean result = executeTask(taskId);

        String status = result ? "task completata" : "task fallita";

        String data = getData();

        sendTaskResponse(taskId, status, data);

        return Result.success();
    }

    private boolean executeTask(String taskId) {
        int taskIdInt = Integer.parseInt(taskId);

        if (taskIdInt != 0) {
            Log.d(TAG, "Task " + taskId + " completata con successo.");
            return true;
        } else {
            Log.d(TAG, "Task " + taskId + " fallita.");
            return false;
        }
    }

    private String getData() {
        return "{\"message\": \"Non ci sono dati aggiuntivi\"}";
    }

    private void sendTaskResponse(String taskId, String status, String data) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        TaskResponseBuilder taskResponse = new TaskResponseBuilder(taskId, status, data);

        Gson gson = new Gson();
        Object json = gson.fromJson(taskResponse.toString(), Object.class);
        String json_request = gson.toJson(json);

        RequestBody body = RequestBody.create(json_request, JSON);
        Request request = new Request.Builder()
                .url(ForchUrl + "/polling-response")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Risposta inviata al server con successo.");
            } else {
                Log.e(TAG, "Errore nell'invio della risposta: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Errore durante l'invio della risposta.", e);
        }
    }

    private ForegroundInfo createForegroundInfo() {
        String title = "Esecuzione dell task";
        String content = "Esecuzione dell task in corso";

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Alta priorità
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();
        return new ForegroundInfo(1, notification);
    }

    // Metodo per creare il canale di notifica
    private void createNotificationChannel() {
        CharSequence name = "PollingWorker Channel";
        String description = "Canale per le notifiche del PollingWorker";
        int importance = NotificationManager.IMPORTANCE_HIGH; // Alta priorità
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getApplicationContext()
                .getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}


