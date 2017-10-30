package com.defunkt.Indoor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.ContentValues.TAG;

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    private TextView lblDisplayP;
    private TextView lblEmailP;
    private TextView lblUpdateName;
    private TextView lblUpdateEmail;
    private TextView lblUpdatePassword;
    private FirebaseAuth mAuth;
    private EditText txtEmailP;
    private Button btnSubmitP;
    private String newEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        lblDisplayP = (TextView) getView().findViewById(R.id.lblDisplayP);
        lblEmailP = (TextView) getView().findViewById(R.id.lblEmailP);
        lblUpdateName = (TextView) getView().findViewById(R.id.lblUpdateName);
        lblUpdateEmail = (TextView) getView().findViewById(R.id.lblUpdateEmail);
        lblUpdatePassword = (TextView) getView().findViewById(R.id.lblUpdatePassword);
        txtEmailP = (EditText) getView().findViewById(R.id.txtEmailP);
        btnSubmitP = (Button) getView().findViewById(R.id.btnSubmitP);

        btnSubmitP.setVisibility(View.INVISIBLE);
        txtEmailP.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        lblDisplayP.setText(user.getDisplayName());
        lblEmailP.setText(user.getEmail());

        lblUpdatePassword.setOnClickListener(this);
        lblUpdateEmail.setOnClickListener(this);
        lblUpdatePassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == lblUpdateName){

        }

        else if (view == lblUpdateEmail){
            final FirebaseUser user = mAuth.getCurrentUser();
            txtEmailP.bringToFront();
            lblEmailP.setVisibility(View.INVISIBLE);
            btnSubmitP.setVisibility(View.VISIBLE);
            String email = lblEmailP.getText().toString();
            txtEmailP.setText(email);
            newEmail = txtEmailP.getText().toString();
            txtEmailP.setVisibility(View.VISIBLE);



            btnSubmitP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()){
                                Log.d(TAG, "The Email was not updated");
                                Toast.makeText(UserProfileFragment.
                                        this.getActivity(), "The Email was unable to be updated", Toast.LENGTH_SHORT).show();

                            }else if(task.isSuccessful()){
                                Toast.makeText(UserProfileFragment.
                                        this.getActivity(), "The Email was updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

        else if (view == lblUpdatePassword){
            Intent myIntent = new Intent(this.getActivity(), ForgotPasswordActivity.class);
            startActivity(myIntent);

        }
    }
}
