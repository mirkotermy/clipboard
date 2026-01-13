package com.example.demo;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends Utente {
    private String username = null;
    private String password = null;
    private Scanner in = new Scanner(System.in);
    public Register(){}

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference("utenti");
    private boolean userExist = false;

    public void registration(Utente user){         
        final CountDownLatch latch = new CountDownLatch(1);
            ref.addListenerForSingleValueEvent(new ValueEventListener(){
                //verifica del nome utente all'interno del Database
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot){
                        if(dataSnapshot.child(user.getUsername()).exists()){
                            System.out.println("Username già esistente");
                            return;
                            
                        }
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("password", user.getPassword());
                        userData.put("clipboard", "");
                        latch.countDown();
                        user.setCurrUser(user.getUsername());
                        ref.child(user.getUsername()).setValueAsync(userData); //registrazione nel database
                        
                        if(ref.child(user.getUsername()) != null){
                                System.out.println("Registrazione avvenuta con successo");
                        }else{
                               System.out.println("Errore durante la registrazione");
                            }
                            
                    }
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError){
                }
            });
    }


public String login(Utente utente) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isPasswordCorrect = new AtomicBoolean(false);

        ref.child(utente.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String passwordFromDb = dataSnapshot.child("password").getValue(String.class);
                    String passwordFromUser = utente.getPassword();
                    if (passwordFromUser != null && passwordFromUser.equals(passwordFromDb)) {
                        isPasswordCorrect.set(true);
                         utente.setCurrUser(utente.getUsername());
                    }
                }else{
                    System.out.println("Username o password errati");
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        latch.await();
        return (utente.getCurrUser() != null)?utente.getCurrUser() : null;
    }
}

