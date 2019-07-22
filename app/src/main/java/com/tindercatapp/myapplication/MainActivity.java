package com.tindercatapp.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.media.MediaPlayer; // added by Natalia 17.7
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.tindercatapp.myapplication.Matches.MatchesActivity;
import com.tindercatapp.myapplication.Utils.PulsatorLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private cards cards[];
    private arrayAdapter arrayAdapter;
    private int i;

    private FirebaseAuth mAuth;

    private String currentUid;
    static MediaPlayer catHissSound, catMeowSound;  // added by Natalia 17.7
    private DatabaseReference usersDb;

    FrameLayout cardFrame, moreFrame;

    SwipeFlingAdapterView flingContainer;

    ListView listView;
    List<cards> rowItems;

    static boolean isSoundMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // added by Natalia
        //String userSex=getIntent().getExtras().getString("userSex");
        catMeowSound = MediaPlayer.create(this, R.raw.cat_meow); // added by Natalia 17.7
        catHissSound = MediaPlayer.create(this, R.raw.cat_hissing);

        //Added by Amal
        cardFrame = findViewById(R.id.card_frame);
        moreFrame = findViewById(R.id.more_frame);

        // start pulsator
        PulsatorLayout mPulsator = findViewById(R.id.pulsator);
        mPulsator.start();

        usersDb = FirebaseDatabase.getInstance().getReference().child("Cats");

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(currentUid);

        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("settings").child("mute").getValue() != null) {
                        isSoundMute = Boolean.parseBoolean(dataSnapshot.child("settings").child("mute").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rowItems = new ArrayList<cards>();

        checkUserSex();

        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems);

        flingContainer  = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);

        checkRowItem();
        updateSwipeCard();
    }

        private void  updateSwipeCard(){

            flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

                @Override
                public void removeFirstObjectInAdapter() {
                    // this is the simplest way to delete an object from the Adapter (/AdapterView)
                    Log.d("LIST", "removed object!");
                    rowItems.remove(0);
                    arrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onLeftCardExit(Object dataObject) {
                    //Do something on the left!
                    //You also have access to the original object.
                    //If you want to use it just cast it (String) dataObject

                    //Added by Amal

                    checkRowItem();

                    cards obj = (cards) dataObject;
                    String userId = obj.getUserId();
                    usersDb.child(userId).child("connections").child("nope").child(currentUid).setValue(true);

                    if(!isSoundMute){
                        catHissSound.start(); // added by Natalia 17.7
                    }else{
                        catHissSound.setVolume(0,0);
                    }


                    //Toast.makeText(MainActivity.this, "Nope", Toast.LENGTH_SHORT).show();

                   /// checkRowItem();
                }

                @Override
                public void onRightCardExit(Object dataObject) {

                    checkRowItem();

                    //Added by Amal
                    cards obj = (cards) dataObject;
                    String userId = obj.getUserId();
                    usersDb.child(userId).child("connections").child("yeps").child(currentUid).setValue(true);
                    isConnectionMatch(userId);

                    if(!isSoundMute){
                        catMeowSound.start(); // added by Natalia 17.7
                    }else{
                        catMeowSound.setVolume(0,0);
                    }


                    //Toast.makeText(MainActivity.this, "Like", Toast.LENGTH_SHORT).show();

                   /// checkRowItem();
                }

                @Override
                public void onAdapterAboutToEmpty(int itemsInAdapter) {
                }

                @Override
                public void onScroll(float scrollProgressPercent) {
                    View view = flingContainer.getSelectedView();
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                }
            });

// Optionally add an OnItemClickListener
            flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                @Override
                public void onItemClicked(int itemPosition, Object dataObject) {
                    Intent intent = new Intent(MainActivity.this, BioActivity.class);
                    startActivity(intent);


                }
            });

        }


    /*
    private String userSex;
    private String oppositeUserSex;
    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference maleDb = FirebaseDatabase.getInstance().getReference().child("Cats").child("Male");
        maleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(user.getUid())){
                    userSex = "Male";
                    oppositeUserSex = "Female";
                    getOppositeSexUsers();
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DatabaseReference femaleDb = FirebaseDatabase.getInstance().getReference().child("Cats").child("Female");
        femaleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(user.getUid())){
                    userSex = "Female";
                    oppositeUserSex = "Male";
                    getOppositeSexUsers();
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    */
// Added by Amal
    private String userSex;
    private String oppositeUserSex;
    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("sex").getValue() != null){
                        userSex = dataSnapshot.child("sex").getValue().toString();
                        switch (userSex){
                            case "Male":
                                oppositeUserSex = "Female";
                                break;
                            case "Female":
                                oppositeUserSex = "Male";
                                break;
                        }
                        getOppositeSexUsers();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


/*
    public void getOppositeSexUsers(){
        DatabaseReference oppositeSexDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(oppositeUserSex);
        oppositeSexDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    //rowItems.add(dataSnapshot.child("name").getValue().toString());
                    cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").toString());
                    rowItems.add(item);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
*/

    //Added by Amal
    public void getOppositeSexUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("sex").getValue() != null) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUid) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUid) && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {
                        String profileImageUrl = "default";
                        int age =0;
                        String location="";

                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }

                        if(dataSnapshot.hasChild("location")){
                            if(dataSnapshot.child("location").getValue()!=null){
                            location=dataSnapshot.child("location").getValue().toString();
                            if(location.length()>2) {
                                location = location.toUpperCase().charAt(0) + location.substring(1, location.length());
                            }
                        }}


                        if(dataSnapshot.hasChild("age")){
                            if(dataSnapshot.child("age").getValue()!=null){
                                age = Integer.valueOf(dataSnapshot.child("age").getValue().toString());
                            }
                        }

                        cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl,age,location);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                        checkRowItem();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    public void navProfilePage(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    //Added Amal
    public void goToMatches(View view){
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    //Added Amal
    public void goToMainPage(View view){
       Intent intent = getIntent();
       finish();
       startActivity(intent);
    }

    //Added Amal
    private void isConnectionMatch(String userId){
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUid).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this,"new connection",Toast.LENGTH_LONG).show();

                    //Newly added by Amal
                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    //usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUid).setValue(true);
                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUid).child("ChatId").setValue(key);

                    // usersDb.child(currentUid).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                    usersDb.child(currentUid).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void navBioPage (View view){
        Intent intent = new Intent(MainActivity.this, BioActivity.class);
        startActivity(intent);
        return;
    }

    //Added by Amal
    private void checkRowItem() {
        if (rowItems.isEmpty()) {
            moreFrame.setVisibility(View.VISIBLE);
            cardFrame.setVisibility(View.GONE);
        }else{
            moreFrame.setVisibility(View.GONE);
            cardFrame.setVisibility(View.VISIBLE);
        }
    }

}