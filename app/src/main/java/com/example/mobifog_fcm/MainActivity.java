package com.example.mobifog_fcm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.content.SharedPreferences;

import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private EditText ForchUrlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ForchUrlEditText = findViewById(R.id.forch_url);

        // Recupero dell'URL dell'orchestratore dalle SharedPreferences all'avvio
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedForchUrl = sharedPreferences.getString("ForchUrl", "");
        // Imposta l'URL salvato nell'EditText
        ForchUrlEditText.setText(savedForchUrl);
        // Inizializzazione del servizio FCM
        FirebaseApp.initializeApp(this);
        // Richiesta all'utente di evitare le ottimizzazioni per il consumo della batteria
        BatteryOptimizationRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BatteryOptimizationRequest();
    }

    /* Metodo per richiedere di evitare le ottimizzazioni per il consumo della batteria, in modo
    da evitare che l'esecuzione del polling venga interrotta in Doze Mode */
    private void BatteryOptimizationRequest() {
        // Intent implicito per la richiedere di annullare le ottimizzazioni di risparmio energetico
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        // Setting di un identificativo dell'app
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    // Metodo per il listener del button di registrazione del Webhook
    public void onRegisterWebhookClick(View v) {

        // Ottenimento dell'URL dall'Edittext rimuovendo gli spazi
        String ForchUrl = ForchUrlEditText.getText().toString().trim();

        // Verifica che l'URL dell'orchestratore non sia vuoto
        if (ForchUrl.isEmpty()) {
            Toast.makeText(MainActivity.this, "Inserisci l'URL dell'orchestratore",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Salavtaggio nelle SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ForchUrl", ForchUrl);
        editor.apply();

        // Creazione di un oggetto WebhookClient e registrazione del webhook
        WebhookClient webhookClient = new WebhookClient(MainActivity.this);
        webhookClient.registerWebhook();
        Toast.makeText(MainActivity.this, "Webhook registrato",
                Toast.LENGTH_SHORT).show();
    }

    // Metodo per il listener del bottone di avvio del polling periodico
    public void onStartFcmClick(View v) {
        // Ottenimento dell'URL dalle SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String ForchUrl = sharedPreferences.getString("ForchUrl", "");

        // Verifica che l'URL dell'orchestratore non sia vuoto
        if (ForchUrl.isEmpty()) {
            Toast.makeText(MainActivity.this,
                    "Inserisci l'URL dell'orchestratore", Toast.LENGTH_SHORT).show();
            return;
        }


        // Impostazione del flag di polling attivo
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFcmActive", true);
        editor.apply();

        // Avvio del polling periodico
        WebhookClient webhookClient= new WebhookClient(getApplicationContext());
        webhookClient.startFcmService();

        Toast.makeText(MainActivity.this, "Servizio FCM avviato.", Toast.LENGTH_SHORT).show();
    }

    public void onStopFcmClick(View v) {
        // Verifica che il polling sia in esecuzione
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isFcmActive = sharedPreferences.getBoolean("isFcmActive",
                false);

        if (!isFcmActive) {
            Toast.makeText(MainActivity.this, "Il servizio FCM non è attivo.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Interrompe il polling
        WebhookClient.stopFcmService(MainActivity.this);

        // Aggiorna il flag
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFcmActive", false);
        editor.apply();

        Toast.makeText(MainActivity.this, "Servizio FCM interrotto.", Toast.LENGTH_SHORT).show();
    }
}