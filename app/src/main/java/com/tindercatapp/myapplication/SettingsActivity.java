package com.tindercatapp.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tindercatapp.myapplication.Matches.MatchesActivity;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat sounds_switch;
    SeekBar sounds_volume;
    private RadioGroup happySounds, nopeSounds;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference usersDb;
    private boolean isSoundMute;
    private int volume;
    private String happySound, happySoundDB;
    private String nopeSound, nopeSoundDB;
    static MediaPlayer catSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sounds_switch = findViewById(R.id.switch_sounds);
        sounds_volume = findViewById(R.id.seekBar);

        happySounds = (RadioGroup) findViewById(R.id.happySounds);
        nopeSounds = (RadioGroup) findViewById(R.id.nopeSounds);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Cats");
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(currentUid);

        sounds_volume.setProgress(20);

        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("settings").child("mute").getValue() != null) {
                        isSoundMute = Boolean.parseBoolean(dataSnapshot.child("settings").child("mute").getValue().toString());
                        sounds_switch.setChecked(isSoundMute);
                    }

                    if (dataSnapshot.child("settings").child("level").getValue() != null) {
                        volume = Integer.parseInt(dataSnapshot.child("settings").child("level").getValue().toString());
                        sounds_volume.setProgress(volume);
                    }

                    if (dataSnapshot.child("settings").child("happysound").getValue() != null) {
                        happySoundDB = dataSnapshot.child("settings").child("happysound").getValue().toString();

                        switch (happySoundDB) {
                            case "happy1":
                                ((RadioButton) ((RadioGroup) findViewById(R.id.happySounds)).getChildAt(0)).setChecked(true);
                                break;
                            case "happy2":
                                ((RadioButton) ((RadioGroup) findViewById(R.id.happySounds)).getChildAt(1)).setChecked(true);
                                break;
                            case "happy3":
                                ((RadioButton) ((RadioGroup) findViewById(R.id.happySounds)).getChildAt(2)).setChecked(true);
                                break;

                        }

                    }

                    if (dataSnapshot.child("settings").child("nopesound").getValue() != null) {
                        nopeSoundDB = dataSnapshot.child("settings").child("nopesound").getValue().toString();

                        switch (nopeSoundDB) {
                            case "nope1":
                                ((RadioButton) ((RadioGroup) findViewById(R.id.nopeSounds)).getChildAt(0)).setChecked(true);
                                break;
                            case "nope2":
                                ((RadioButton) ((RadioGroup) findViewById(R.id.nopeSounds)).getChildAt(1)).setChecked(true);
                                break;
                            case "nope3":
                                ((RadioButton) ((RadioGroup) findViewById(R.id.nopeSounds)).getChildAt(2)).setChecked(true);
                                break;

                        }

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
                } else {
                    usersDb.child(currentUid).child("settings").child("mute").setValue(false);
                }
            }
        });

        sounds_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                usersDb.child(currentUid).child("settings").child("level").setValue(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


/*
        happySounds.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i== R.id.happy1){
                    happySound="happy1";
                }else if(i== R.id.happy2){
                    happySound="happy2";
                 }else{
                    happySound="happy3";
                }
                System.out.println(">>>> Sound Selected "+happySound);
                playCatSound(happySound);
                usersDb.child(currentUid).child("settings").child("happysound").setValue(happySound);

            }
        });


        nopeSounds.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i== R.id.nope1){
                    nopeSound="nope1";
                }else if(i== R.id.nope2){
                    nopeSound="nope2";
                }else{
                    nopeSound="nope3";
                }
                System.out.println(">>>> Sound Selected "+nopeSound);
                playCatSound(nopeSound);
                usersDb.child(currentUid).child("settings").child("nopesound").setValue(nopeSound);
            }
        });
        */
    }


    public void goToMainPageFromSettings(View view) {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    //Added Amal
    public void goToMatchesFromSettings(View view) {
        Intent intent = new Intent(SettingsActivity.this, MatchesActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void navProfilePageFromSettings(View view) {
        Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
        return;
    }


    public void onRadioButtonClicked(View v) {
        boolean checked = ((RadioButton) v).isChecked();
        switch (v.getId()) {
            case R.id.happy1:
                if (checked)
                    playCatSound("happy1");
                usersDb.child(currentUid).child("settings").child("happysound").setValue("happy1");
                break;

            case R.id.happy2:
                if (checked)
                    playCatSound("happy2");
                usersDb.child(currentUid).child("settings").child("happysound").setValue("happy2");
                break;

            case R.id.happy3:
                if (checked)
                    playCatSound("happy3");
                usersDb.child(currentUid).child("settings").child("happysound").setValue("happy3");
                break;

            case R.id.nope1:
                if (checked)
                    playCatSound("nope1");
                usersDb.child(currentUid).child("settings").child("nopesound").setValue("nope1");
                break;

            case R.id.nope2:
                if (checked)
                    playCatSound("nope2");
                usersDb.child(currentUid).child("settings").child("nopesound").setValue("nope2");
                break;

            case R.id.nope3:
                if (checked)
                    playCatSound("nope3");
                usersDb.child(currentUid).child("settings").child("nopesound").setValue("nope3");
                break;

        }
    }


    public void playCatSound(String soundName) {

        switch (soundName) {
            case "happy1":
                catSound = MediaPlayer.create(this, R.raw.cat_purr_1);
                break;
            case "happy2":
                catSound = MediaPlayer.create(this, R.raw.cat_purr_2);
                break;
            case "happy3":
                catSound = MediaPlayer.create(this, R.raw.cat_purr_3);
                break;

            case "nope1":
                catSound = MediaPlayer.create(this, R.raw.cat_meow_1);
                break;
            case "nope2":
                catSound = MediaPlayer.create(this, R.raw.cat_meow_2);
                break;
            case "nope3":
                catSound = MediaPlayer.create(this, R.raw.cat_meow_3);
                break;
        }
        catSound.start();
    }


    public void navProfilePage(View view) {
        Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void saveSettings(View view) {
        //Toast.makeText(SettingsActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }
    /*TODO implement mute sounds setting*/
    /*TODO implement settings save button functionality*/

    // sounds_switch.OnCheckedChangeListener(){}


}