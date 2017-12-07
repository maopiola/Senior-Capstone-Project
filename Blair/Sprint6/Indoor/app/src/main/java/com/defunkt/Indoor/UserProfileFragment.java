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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class UserProfileFragment extends Fragment implements View.OnClickListener, PasswordDialog.OnPasswordListener{

    private TextView lblDisplayP;
    private TextView lblEmailP;
    private TextView lblUpdateEmail;
    private TextView lblUpdatePassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText txtEmailP;
    private Button btnSubmitP;
    private String newEmail;

    /*PasswordDialog.OnPasswordListener{
        @Override
        public void onPassword(String pass){

        }
    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("OU Indoors: User Profile");
        lblDisplayP = (TextView) getView().findViewById(R.id.lblDisplayP);
        lblEmailP = (TextView) getView().findViewById(R.id.lblEmailP);
        lblUpdateEmail = (TextView) getView().findViewById(R.id.lblUpdateEmail);
        lblUpdatePassword = (TextView) getView().findViewById(R.id.lblUpdatePassword);
        txtEmailP = (EditText) getView().findViewById(R.id.txtEmailP);
        btnSubmitP = (Button) getView().findViewById(R.id.btnSubmitP);

        btnSubmitP.setVisibility(View.INVISIBLE);
        txtEmailP.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        final FirebaseUser user = mAuth.getCurrentUser();

        lblDisplayP.setText(user.getDisplayName());
        lblEmailP.setText(user.getEmail());

        lblUpdatePassword.setOnClickListener(this);
        lblUpdateEmail.setOnClickListener(this);
        lblUpdatePassword.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        //reference to the database instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //adds a listener to the child "Users" this runs upon start of the activity and when anything changes in the DB
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //list of data for the users
                List<User> data = new ArrayList<>();

                //if there's a database
                if (dataSnapshot.exists()) {

                    //searches through the database and grabs all the keys (uids) and throws them in the arraylist
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        User user = snap.getValue(User.class);
                        user.setUid(snap.getKey());
                        data.add(user);
                    }

                    //searches through the array list and checks to see what user is logged in to display their displayName
                    for (User user : data) {
                        if (user.getUid().equals(mAuth.getCurrentUser().getUid())) {
                            lblDisplayP.setText(user.getDisplayName());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database", databaseError.toException());

            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view == lblUpdateEmail){

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
                    PasswordDialog dialog = new PasswordDialog();
                    dialog.show(getFragmentManager(), getString(R.string.confirm_password));
                    dialog.setTargetFragment(UserProfileFragment.this, 1);
                }
            });
        }

        else if (view == lblUpdatePassword){
            Intent myIntent = new Intent(this.getActivity(), ForgotPasswordActivity.class);
            startActivity(myIntent);

        }
    }

    @Override
    public void onPassword(String password) {


        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            //checks to see if the email was already used
                            mAuth.fetchProvidersForEmail(txtEmailP.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                                    if (task.isSuccessful()){
                                        if (task.getResult().getProviders().size() == 1){
                                            Toast.makeText(getActivity(), "That Email is already in use", Toast.LENGTH_SHORT).show();
                                        }

                                        else if (task.getResult().getProviders().size() == 0){

                                            //updates the email if it's not a dupe
                                            mAuth.getCurrentUser().updateEmail(txtEmailP.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(TAG, "User email address updated.");
                                                                Toast.makeText(getActivity(), "The email was updated", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getActivity(), "Email verification sent to " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                                        mAuth.signOut();
                                                    }
                                                }
                                            });

                                            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("email").setValue(txtEmailP.getText().toString());

                                            txtEmailP.setVisibility(View.INVISIBLE);
                                            lblEmailP.bringToFront();
                                            lblEmailP.setText(txtEmailP.getText().toString());
                                            lblEmailP.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            });
                        }else {
                            Log.d(TAG, "Re-authentication failed.");
                        }
                    }
                });
    }
}
