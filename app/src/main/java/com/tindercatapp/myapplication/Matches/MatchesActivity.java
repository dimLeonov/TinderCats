package com.tindercatapp.myapplication.Matches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView.Adapter  mMatchesAdapter;
    private RecyclerView.LayoutManager mMatchesLayoutManager;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        //currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserID="Fm1";

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mMatchesLayoutManager= new LinearLayoutManager(MatchesActivity.this);
        mRecyclerView.setLayoutManager(mMatchesLayoutManager);

        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), MatchesActivity.this);
        mRecyclerView.setAdapter(mMatchesAdapter);

        getUserMatchId();

        //for(int i=0;i<100;i++){
           // MatchesObject obj = new MatchesObject(Integer.toString(i));
           // resultMatches.add(obj);

        //}


       // mMatchesAdapter.notifyDataSetChanged();



   /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        */
    }

    public void navMainPage (View view){
        Intent intent = new Intent(MatchesActivity.this, MainActivity.class);
        startActivity(intent);
        return;
    }

    public void navProfilePage (View view){
        Intent intent = new Intent(MatchesActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }


    private void FetchMatchInformation(String key){
      //  DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    String name ="";
                    String profileImageUrl="";
                    if(dataSnapshot.child("name").getValue()!= null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!= null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }

                    MatchesObject obj = new MatchesObject(userId,name,profileImageUrl);
                    resultMatches.add(obj);
                    mMatchesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<MatchesObject> resultMatches =  new ArrayList<MatchesObject>();
    private List<MatchesObject> getDataSetMatches(){
        return resultMatches;
    }

    private void getUserMatchId(){
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(currentUserID).child("connections").child("matches");
   matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
       @Override
       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
           if(dataSnapshot.exists()){
               for(DataSnapshot match : dataSnapshot.getChildren()){
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

