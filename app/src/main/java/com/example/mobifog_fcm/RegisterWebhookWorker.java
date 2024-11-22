package com.example.mobifog_fcm;

import android.content.SharedPreferences;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterWebhookWorker extends Worker {

    private final String ForchUrl;
    private final OkHttpClient client;
    private static final String TAG = "RegisterWebhookWorker";
    private final Context context;

    public RegisterWebhookWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters){
        super(context, workerParameters);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        this.ForchUrl = sharedPreferences.getString("ForchUrl", "");
        this.client = HttpClientSingleton.INSTANCE.getClient();
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Ottieni l'IP del dispositivo
        String NodeUrl = getDeviceIp();

        // Ottieni il token FCM
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Errore nel recupero del token FCM", task.getException());
                return;
            }

            String fcmToken = task.getResult();

            MediaType JSON = MediaType.get("application/json; charset=utf-8");

            // Creazione di un oggetto WebhookRegistrationBuilder per convertire il corpo della richiesta in JSON
            WebhookRegistrationBuilder registrationRequest = new WebhookRegistrationBuilder(NodeUrl, "id_number_task", fcmToken);

            // Conversione in JSON
            Gson gson = new Gson();
            String json_request = gson.toJson(registrationRequest);

            // Creazione dell corpo della richiesta in formato JSON
            RequestBody body = RequestBody.create(json_request, JSON);

            // Creazione della richiesta HTTP
            Request request = new Request.Builder()
                    .url(ForchUrl + "/polling-response")
                    .post(body)
                    .build();

            // Esegui la richiesta in modo asincrono con enqueue
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Errore di rete durante la registrazione del Webhook.", e);

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Webhook registrato con successo.");
                    } else {
                        Log.e(TAG, "Errore nella registrazione del Webhook: " + response.code());
                    }
                }
            });
        });

        // Restituisci il risultato come success
        return Result.success();
    }


    // Metodo per estrarre l'IP del dispositivo
    public String getDeviceIp() {
        String ip = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork != null) {
                // Verifica la connessione Wi-Fi
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null && wifiManager.isWifiEnabled()) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        int ipAddress = wifiInfo.getIpAddress();
                        ip = String.format(Locale.getDefault(), "%d.%d.%d.%d",
                                (ipAddress & 0xff),
                                (ipAddress >> 8 & 0xff),
                                (ipAddress >> 16 & 0xff),
                                (ipAddress >> 24 & 0xff));
                    }
                }
            }
        }

        if (ip == null) {
            // Se non è riuscito a ottenere l'IP Wi-Fi, tenta di ottenere l'IP tramite altre interfacce di rete
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress inetAddress = addresses.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            ip = inetAddress.getHostAddress();
                            if (ip != null) {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ip;
    }
}


