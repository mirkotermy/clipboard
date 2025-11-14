package com.example.demo;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

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
                        latch.countDown();
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


    public void login(Utente user){
        final CountDownLatch latch = new CountDownLatch(1);
            ref.child(user.getUsername()).addListenerForSingleValueEvent(new ValueEventListener(){ //verifica del nome utente all'interno del Database
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot){
                 latch.countDown();
                    if(ref.child(user.getUsername()) != null && dataSnapshot.child(user.getUsername()).child("password").getValue(String.class).equals(user.getPassword())){
                        System.out.println("Login avvenuto con successo");
                        user.setCurrUser(user.getUsername());
                    }else{
                        System.out.println("Username o password errati");
                    }
                }
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError){
                }
            });
    }
}
