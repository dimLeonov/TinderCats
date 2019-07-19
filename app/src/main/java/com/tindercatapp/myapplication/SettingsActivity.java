package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void navProfilePage (View view){
        Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void saveSettings (View view){
        Toast.makeText(SettingsActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }
    /*TODO implement mute sounds setting*/
    /*TODO implement settings save button functionality*/
}