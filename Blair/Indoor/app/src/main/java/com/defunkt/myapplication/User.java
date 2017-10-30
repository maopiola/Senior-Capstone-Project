package com.defunkt.myapplication;

import com.google.firebase.database.DatabaseReference;

public class User {
    public String email;
    private DatabaseReference mDatabase;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public User(String email) {

        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

}
