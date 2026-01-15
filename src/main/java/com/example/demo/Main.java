package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
    import com.google.auth.oauth2.GoogleCredentials;
    import com.google.firebase.FirebaseApp;
    import com.google.firebase.FirebaseOptions;

    public class Main implements NativeKeyListener {
        private static Register registerService;
        private boolean ctrlpressed = false;
        private String currUser;
        public static void main(String[] args) {
            // --- 1. INIZIALIZZAZIONE FIREBASE ---
            try {
                    InputStream serviceAccount = Main.class.getResourceAsStream("/serviceAccountKey.json");
            if (serviceAccount == null) {
                System.err.println("FATAL ERROR: serviceAccountKey.json not found in classpath.");
                // ...
                System.exit(1); // <-- Questo causa l'errore in Maven
        }

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

        String loggedInUser = null;
        Scanner scanner = new Scanner(System.in);
        Utente user = new Utente();
        Register reg = new Register();
        while (loggedInUser == null) {
            System.out.println("\n--- MENU CLIENT ---");
            System.out.println("1. Registrati");
            System.out.println("2. Login");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine();

           switch(choice){
                case "1":
                    System.out.print("Inserisci username (email): ");
                    user.setUsername(scanner.nextLine());
                    System.out.print("Inserisci password: ");
                    user.storedPassword(scanner.nextLine());
                    reg.registration(user);
                    loggedInUser =  user.getUsername();
                    System.out.println("Benvenuto " + loggedInUser);
                break;
                case "2":
                    System.out.print("Inserisci username (email): ");
                    user.setUsername(scanner.nextLine());
                    System.out.print("Inserisci password: ");
                    user.storedPassword(scanner.nextLine());
                try {
                    reg.login(user);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                    loggedInUser =  user.getCurrUser();
                    
                default:
                break;
           }
        }        
        System.out.println("Benvenuto " + loggedInUser);        
        KeyList key = new KeyList(loggedInUser);
        key.monitorClipboard();
        key.monitorFirebase();
        String lastext = "";
        
        // Se il login ha avuto successo, avvia il monitoraggio degli appunti
    }

    //TODO: creare una classe apparte per gli eventi da tastiera che ogni volta che si preme ctrl+c salva nel database 
    //e quando si preme ctrl + v si prende i dati dal database devo creare una classe apparte dove posso passarci username
    // 

    public void setCurrUser(String user){
        currUser = user;
    
    }
    public String getCurrUser(){
        return currUser;
    }
}



