package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class BioActivity extends AppCompatActivity {
    public String source, currentUserID, profileImageUrl, name, age, location, biotext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bio);
        //gets passed current UID from clicked button(main activity or profile page)
        Intent intent = getIntent();
        currentUserID = intent.getStringExtra("UID");
        source = intent.getStringExtra("source");

        //gets all user info from firebase DB for current UID
        Toast.makeText(BioActivity.this, "UID:"+currentUserID, Toast.LENGTH_SHORT).show();
        FetchUserInformation(currentUserID);

    }


    //gets user data from specific UID database hive.
    private void FetchUserInformation(String key){
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    if(dataSnapshot.child("name").getValue()!= null){
                        setUserName(dataSnapshot.child("name").getValue().toString());
                    }
                    else {setUserName("unknown_name");}

                    if(dataSnapshot.child("profileImageUrl").getValue()!= null)
                    {
                        setProfileImageUrl(dataSnapshot.child("profileImageUrl").getValue().toString());
                    }
                    else if(!((dataSnapshot.child("profileImageUrl").getValue().toString()).contains("firebasestorage")))
                    {
                        setProfileImageUrl("https://firebasestorage.googleapis.com/v0/b/tindercats-749f9.appspot.com/o/profileImages%2FDefault_UserPic.png?alt=media&token=8e28361f-68ad-41e7-801a-4bff99d1d381");
                    }
                    else
                    {
                        setProfileImageUrl("https://firebasestorage.googleapis.com/v0/b/tindercats-749f9.appspot.com/o/profileImages%2FDefault_UserPic.png?alt=media&token=8e28361f-68ad-41e7-801a-4bff99d1d381");
                    }

                    if(dataSnapshot.child("age").getValue()!= null){
                        setAge(dataSnapshot.child("age").getValue().toString());
                    }
                    else {setAge("unknown_age");}

                    if(dataSnapshot.child("location").getValue()!= null){
                        setLocation(dataSnapshot.child("location").getValue().toString());
                    }
                    else {setLocation("unknown_location");}

                    if(dataSnapshot.child("bio").getValue()!= null){
                        setBioText(dataSnapshot.child("bio").getValue().toString());
                    }
                    else {setBioText("This cat has no bio...");}
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void setUserName(String name){
        this.name = name;
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        ((TextView) findViewById(R.id.bioname)).setText(name);
    }

    public void setAge(String age){
        this.age = age;
        ((TextView) findViewById(R.id.bioage)).setText(", "+age);
    }

    public void setLocation(String location){
        this.location = location;
        ((TextView) findViewById(R.id.biolocation)).setText(location);
    }

    public void setBioText(String biotext){
        this.biotext = biotext;
        ((TextView) findViewById(R.id.biotext)).setText(biotext);
    }

    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
        ImageView biopicture = (ImageView) findViewById(R.id.biopicture);
        Glide.clear(biopicture);

        if (profileImageUrl.contains("firebase")){
            Glide.with(this)
                    .load(profileImageUrl)
                    .into(biopicture);
        }
        else{
            Glide.with(this)
                    .load(R.drawable.ic_launcher_web)
                    .into(biopicture);
        }
    }

    /*TODO create proper getters and put this into its own class*/
    public String getUserName() {
        return name;
    }
    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public void navBack (View view){
        if (source.contains("main")){
            NavMain(view);
        }
        else if (source.contains("profile")){
            NavProfile(view);
        }

            finish();
    }

    public void NavProfile(View view){
        Intent intent = new Intent(BioActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void NavMain(View view){
        Intent intent = new Intent(BioActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}