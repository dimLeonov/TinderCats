package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat sounds_switch;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference usersDb;
    private boolean isSoundMute ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sounds_switch = findViewById(R.id.switch_sounds);

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
                        sounds_switch.setChecked(isSoundMute);s
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sounds_switch.setChecked(isSoundMute);



       // M;ainActivity.catMeowSound.setVolume(0,0) for mute

        sounds_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    usersDb.child(currentUid).child("settings").child("mute").setValue(true);
                    }else{
                    usersDb.child(currentUid).child("settings").child("mute").setValue(false);
                }
            }
        });



    }



    public void navProfilePage (View view){
        Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void saveSettings (View view){
        //Toast.makeText(SettingsActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }
    /*TODO implement mute sounds setting*/
    /*TODO implement settings save button functionality*/

   // sounds_switch.OnCheckedChangeListener(){}




}