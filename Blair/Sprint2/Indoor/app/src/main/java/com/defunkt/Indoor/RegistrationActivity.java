package com.defunkt.Indoor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

//AIzaSyCkpeoXnFJQSqciHQkMAqQGX7iOKuUBSAM
public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnRegister;
    private TextView lblSignIn;
    private EditText txtEmail;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtPasswordConfirm;
    private String TAG;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private final List<DataSnapshot> parent = new ArrayList<DataSnapshot>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setTitle("OU Indoor Registration");

        progressDialog= new ProgressDialog(this);

        btnRegister = (Button)findViewById(R.id.btnRegister);

        lblSignIn = (TextView)findViewById(R.id.lblSignIn);

        txtEmail = (EditText)findViewById(R.id.txtREmail);

        txtPassword = (EditText)findViewById(R.id.txtRPassword);

        txtPasswordConfirm = (EditText)findViewById(R.id.txtPasswordConfirm);

        btnRegister.setOnClickListener(this);

        //initializes our firebase authentication system
        firebaseAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    //gets the string value for the email and password
    private void registerUser(View view) {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String cPassword = txtPasswordConfirm.getText().toString().trim();

        //error checking for email text box
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter in an email", Toast.LENGTH_SHORT).show();
            return;
        }


        //error checking for the password text box
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter in a password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 5) {
            Toast.makeText(this, "The Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i <= 10; i++) {
            if (password.contains(Integer.toString(i))) {
                break;
            }
            else if (i == 10) {
                Toast.makeText(this, "Your password must contain at least one number", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!password.equals(cPassword)) {
            Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }



        //addOnCompleteListener listens to see if the registration is complete

        progressDialog.setMessage("Registering User");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            /*********************added method*******************************/

                            onAuth(task.getResult().getUser());
                            logIn();

                            /*********************added method*******************************/
                            sendEmailVerification();


                        }else{
                            Toast.makeText(RegistrationActivity.this, "Registration Unsuccessful.", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    private void onAuth(FirebaseUser user) {
        createUser(user.getUid());
    }

    // TODO: Need to figure out how to write user data to database. It's not adding.e
    // This will be needed to save the user's routes. 9/25/17
    private void createUser(String uid) {
        User user = new User(txtEmail.getText().toString());
        mDatabase.child(uid).setValue(user);

    }

    private void sendEmailVerification() {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegistrationActivity.this, "Email verification sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }


    }

    /******************************added method*************************************/
    public void logIn() {
        Intent myIntent = new Intent(this, MainActivity.class);
        final EditText emailInput = (EditText) findViewById(R.id.txtREmail);
        String emailPass = emailInput.getText().toString();
        myIntent.putExtra("email", emailPass);
        startActivity(myIntent);
    }

 /******************************added method************************************/
    @Override
    public void onClick(View view) {
        if(view == btnRegister){
            registerUser(view);
        } else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    public void signInUser(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
