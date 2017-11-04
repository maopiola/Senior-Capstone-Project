package com.defunkt.Indoor;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomePageFragment extends Fragment {


    public HomePageFragment() {
        // Required empty public constructor
    }

    private TextView lblEmail;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //gets the instance of the auth
        mAuth = FirebaseAuth.getInstance();

        //sets the email to the current user's email and splits it to get the display name
        String email = mAuth.getCurrentUser().getEmail();
        String[] split = email.split("@");
        String name = split[0];

        //initializes the view
        lblEmail = (TextView) getView().findViewById(R.id.lblEmail);
        //sets the view's text to the current user email
        lblEmail.setText(name);

    }

}
