package com.example.demo;

public class Utente {
    private String username;
    private String password;
    private String currUser;

    public Utente(){}
    public Utente(String username, String password){
        this.username = username;
        this.password = password;
    }
    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }
    public String getCurrUser(){
        return this.currUser;
    }

    public void setCurrUser(String currUser){
        this.currUser = currUser;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public void storedPassword(String password){
        this.password = password;
    }
}
