package com.example.mobifog_fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNewTokenWorker extends Worker {
    private static final String TAG = "SendNewToken";
    private final OkHttpClient client;
    private final String ForchUrl;

    public SendNewTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs",
                Context.MODE_PRIVATE);
        this.ForchUrl = sharedPreferences.getString("ForchUrl", "");
        // Inizializzazione del client HTTP singleton
        this.client = HttpClientSingleton.INSTANCE.getClient();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Recupera il token dai parametri
        String token = getInputData().getString("token");
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token non valido");
            return Result.failure();
        }

        // Creazione del payload JSON
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonPayload = new Gson().toJson(new SendNewTokenBuilder(token));

        // Corpo della richiesta
        RequestBody body = RequestBody.create(jsonPayload, JSON);

        // Creazione della richiesta HTTP POST
        Request request = new Request.Builder()
                .url(ForchUrl+"/polling-response")
                .post(body)
                .build();

        // Invio della richiesta
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Token registrato con successo al server.");
                return Result.success();
            } else {
                Log.e(TAG, "Errore nella registrazione del token: " + response.code());
                return Result.retry(); // Riprova in caso di errore
            }
        } catch (IOException e) {
            Log.e(TAG, "Errore di rete durante la registrazione del token.", e);
            return Result.retry();
        }
    }
}
