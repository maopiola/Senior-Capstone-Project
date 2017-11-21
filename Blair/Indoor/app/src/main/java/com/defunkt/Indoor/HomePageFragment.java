package com.defunkt.Indoor;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomePageFragment extends Fragment {


    public HomePageFragment() {
        // Required empty public constructor
    }

    private TextView lblEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("OU Indoors: Home");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        //initializes the view
        lblEmail = (TextView) getView().findViewById(R.id.lblEmail);

        //reference to the database instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //adds a listener to the child "Users" this runs upon start of the activity and when anything changes in the DB
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //list of data for the users
                List<User> data = new ArrayList<>();

                //if there's a database
                if(dataSnapshot.exists()){

                    //searches through the database and grabs all the keys (uids) and throws them in the arraylist
                    for(DataSnapshot snap : dataSnapshot.getChildren()){
                        User user = snap.getValue(User.class);
                        user.setUid(snap.getKey());
                        data.add(user);
                    }

                    //searches through the array list and checks to see what user is logged in to display their displayName
                    for(User user : data){
                        if(user.getUid().equals(mAuth.getCurrentUser().getUid())){
                            lblEmail.setText(user.getDisplayName());
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

}
