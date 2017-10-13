package com.defunkt.Indoor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText txtEmailForgot;
    private Button btnForgot;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmailForgot = (EditText) findViewById(R.id.txtEmailForgot);
        btnForgot = (Button) findViewById(R.id.btnForgot);
        firebaseAuth = FirebaseAuth.getInstance();

        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtEmailForgot.getText().toString().isEmpty()) {
                    resetPassword();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter in a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void resetPassword() {

        String email = txtEmailForgot.getText().toString();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Email was sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Email was not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
