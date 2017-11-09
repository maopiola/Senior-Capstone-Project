package com.defunkt.Indoor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.navigine.naviginesdk.NavigineSDK;

import static com.defunkt.Indoor.NavigineFragment.initialize;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    //defining view objects
    private Button btnSignIn;
    private EditText txtPassword;
    private EditText txtEmail;
    private TextView lblClickHere;
    private TextView lblForgotPassword;
    private String TAG;

    //location id for the north foundation
    private int LOCATION_ID =2267;

    //progress dialog
    //these are the little dialogs that pop up saying, "logging in"
    private ProgressDialog progressDialog;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //the title that is set up top of the app
        setTitle("OU Indoor Login");

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //initializes Navigine
        (new InitTask()).execute();

        //initializing views
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        lblClickHere = (TextView)findViewById(R.id.lblClickHere);
        lblForgotPassword = (TextView) findViewById(R.id.lblForgotPassword);
        btnSignIn.setOnClickListener(this);
        lblClickHere.setOnClickListener(this);
        lblForgotPassword.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

        /***************************added bundle****************************************/
        //gets the value of the email from the intent, if it's there, and puts it in the email field
        //this is for when after a user registers
        Bundle emailData = getIntent().getExtras();
        if(emailData == null){
            return;
        }

        String getEmail = emailData.getString("email");
        txtEmail.setText(getEmail);
        /***************************added bundle***************************************/
    }

    //signs the user in
    public void userSignIn(){

        //get the text from both text fields
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        //error checking to see if the user has typed anything in the email and password fields
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter in an Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter in a Password", Toast.LENGTH_SHORT).show();
            return;
        }

        //loading dialog box after logging in
        progressDialog.setMessage("Logging In.. Please Wait...");
        progressDialog.show();

        //logging in the user with email and password
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //gets current user
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        //checks to see if the user is registered
                        if(!task.isSuccessful()){
                            try{
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e){
                                Toast.makeText(MainActivity.this, "Please Register", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        //checks to see if the user's email is verified
                        else if (user.isEmailVerified() && task.isSuccessful()) {
                            if (task.isSuccessful()) {
                                //start the homepage activity
                                startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
                                finish();
                            }
                        }

                        //if the email is not verified make a toast
                        else if (!user.isEmailVerified()) {
                            Toast.makeText(MainActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    public void onClick(View view) {
        //if btnSignIn is click it calls the method userSignIn()
        if(view == btnSignIn){
            userSignIn();


        }

        //if the lblClickHere is clicked it directs the user to the registration activity
        else if(view == lblClickHere){
            Intent myIntent = new Intent(this, RegistrationActivity.class);
            startActivity(myIntent);
        }

        //if the lblForgotPassword is clicked it directs the user to an activity where they can change their password
        else if (view == lblForgotPassword){
            Intent myIntent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(myIntent);
        }
    }

    //initialization of the navigine engine and attempts to load the location.
    class InitTask extends AsyncTask<Void, Void, Boolean> {
        private String  mErrorMsg = null;
        @Override protected Boolean doInBackground(Void... params){

            //checks to see if navigine is initialized
            if (!initialize(getApplicationContext())){
                mErrorMsg = "Error downloading location Navigine!";
                //prints to logcat upon failure
                Log.e(TAG, mErrorMsg);
                return Boolean.FALSE;
            }
            //prints to logcat upon init
            Log.d(TAG, "Initialized!");

            if (!NavigineSDK.loadLocation(LOCATION_ID, 30)){
                mErrorMsg = "Error downloading location 'Navigine!";
                Log.e(TAG, mErrorMsg);
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }
    }
}
