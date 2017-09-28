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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    //defining view objects
    private Button btnSignIn;
    private EditText txtPassword;
    private EditText txtEmail;
    private TextView lblClickHere;

    //progress dialog
    private ProgressDialog progressDialog;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("OU Indoor Login");

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //initializing views
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        lblClickHere = (TextView)findViewById(R.id.lblClickHere);
        btnSignIn.setOnClickListener(this);
        lblClickHere.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);


        /***************************added bundle****************************************/
        Bundle emailData = getIntent().getExtras();
        if(emailData == null){
            return;
        }

        String getEmail = emailData.getString("email");
        txtEmail.setText(getEmail);
        /***************************added bundle***************************************/
    }


    public void userSignIn(){

        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter in an Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter in a Password", Toast.LENGTH_SHORT).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Logging In.. Please Wait...");
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //if the task is successfull
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if(user.isEmailVerified()){
                            if(task.isSuccessful()){
                                //start the profile activity
                                finish();
                                startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
                            }
                        }else{
                            Toast.makeText(MainActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        }

                    }
                });



    }


    @Override
    public void onClick(View view) {

        if(view == btnSignIn){
            userSignIn();

        }else if(view == lblClickHere){
            Intent myIntent = new Intent(this, RegistrationActivity.class);
            startActivity(myIntent);
        }
    }
}
