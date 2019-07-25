package com.tindercatapp.myapplication.Matches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tindercatapp.myapplication.MainActivity;
import com.tindercatapp.myapplication.ProfileActivity;
import com.tindercatapp.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MatchesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mMatchesAdapter;
    private RecyclerView.LayoutManager mMatchesLayoutManager;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // currentUserID="Fm1";

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mMatchesLayoutManager = new LinearLayoutManager(MatchesActivity.this);
        mRecyclerView.setLayoutManager(mMatchesLayoutManager);

        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), MatchesActivity.this);
        mRecyclerView.setAdapter(mMatchesAdapter);

        getUserMatchId();

        if(MainActivity.notificationManager!=null){
            MainActivity.notificationManager.cancelAll();
        }

          }

    public void navMainPage(View view) {
        Intent intent = new Intent(MatchesActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void navProfilePage(View view) {
        Intent intent = new Intent(MatchesActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }


    private void FetchMatchInformation(String key) {
        //  DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(key);

        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";
                    int age =0;
                    String location ="";


                    if (dataSnapshot.child("name").getValue() != null) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("profileImageUrl")){
                    if (dataSnapshot.child("profileImageUrl").getValue() != null) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }}

                    if(dataSnapshot.hasChild("age")){
                        if (dataSnapshot.child("age").getValue() != null) {
                            age = Integer.parseInt(dataSnapshot.child("age").getValue().toString());
                        }

                    }

                    if(dataSnapshot.hasChild("location")){
                        if (dataSnapshot.child("location").getValue() != null) {
                            location = dataSnapshot.child("location").getValue().toString();
                        }
                    }




                    MatchesObject obj = new MatchesObject(userId, name, profileImageUrl,age,location);
                    resultMatches.add(obj);
                    mMatchesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<MatchesObject> resultMatches = new ArrayList<MatchesObject>();

    private List<MatchesObject> getDataSetMatches() {
        return resultMatches;
    }

    private void getUserMatchId() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(currentUserID).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot match : dataSnapshot.getChildren()) {
                        FetchMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

