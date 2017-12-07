package com.defunkt.Indoor;

import com.google.firebase.database.DatabaseReference;

public class User {
    public String email;
    public String displayName;
    public String uid;
    private DatabaseReference mDatabase;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public User(String email, String displayName) {

        this.email = email;
        this.displayName = displayName;
    }

    public String getEmail() {

        return this.email;
    }

    public String getDisplayName(){

        return this.displayName;
    }

    public String getUid(){

        return this.uid;
    }

    public void setEmail(String email){

        this.email = email;
    }

    public void setDisplayName(String displayName){

        this.displayName = displayName;
    }

    public void setUid(String uid){

        this.uid = uid;
    }

}
