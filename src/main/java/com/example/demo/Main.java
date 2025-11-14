package com.example.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;

import static spark.Spark.*;

public class Main {

    // 1. DICHIARO la variabile, ma non la inizializzo qui.
    private static Register registerService;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // --- 1. INIZIALIZZAZIONE FIREBASE ---
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://clipboard-ed5ff-default-rtdb.europe-west1.firebasedatabase.app/")
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase è stato inizializzato con successo!");

        } catch (IOException e) {
            System.err.println("ERRORE CRITICO: File 'serviceAccountKey.json' non trovato.");
            System.err.println("Il server non può partire senza la chiave di Firebase. Dettagli: " + e.getMessage());
            return; // Interrompe l'avvio del server
        }

        // --- 2. INIZIALIZZO IL SERVICE ORA CHE FIREBASE E' PRONTO ---
        registerService = new Register();

        // --- 3. CONFIGURAZIONE DEL SERVER API ---
        port(4567);

        System.out.println("Server API in ascolto su http://localhost:4567");
        // NUOVO: Log di ogni richiesta in arrivo per debug
        
        before((request, response) -> {
            System.out.println(">>> Richiesta ricevuta: " + request.requestMethod() + " " + request.uri());
        });

        // --- ENDPOINT: Registrazione ---
        post("/register", (request, response) -> {
            response.type("application/json");
            Utente user = gson.fromJson(request.body(), Utente.class);
            registerService.registration(user);
            return gson.toJson(new ApiResponse("success", "Registrazione tentata."));
        });

        // --- ENDPOINT: Login ---
        post("/login", (request, response) -> {
            response.type("application/json");
            Utente user = gson.fromJson(request.body(), Utente.class);
            registerService.login(user);
            return gson.toJson(new ApiResponse("success", "Login tentato."));
        });

        // --- ENDPOINT: Aggiunta Appunto ---
        post("/add-note", (request, response) -> {
            response.type("application/json");
            NoteData noteData = gson.fromJson(request.body(), NoteData.class);

            System.out.println("Nuovo appunto ricevuto dall'utente '" + noteData.getUsername() + "': " + noteData.getContent());

            // Qui implementerai il salvataggio su Firebase

            return gson.toJson(new ApiResponse("success", "Appunto ricevuto."));
        });

        // Gestione di endpoint non trovati
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ApiResponse("error", "Endpoint non valido"));
        });
    }
}

// --- CLASSI DI SUPPORTO PER I DATI JSON ---

class ApiResponse {
    private String status;
    private String message;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}

class NoteData {
    private String username;
    private String content;

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }
}
