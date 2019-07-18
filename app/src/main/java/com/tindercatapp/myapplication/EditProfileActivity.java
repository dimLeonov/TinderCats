package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tindercatapp.myapplication.Matches.MatchesActivity;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
    }

    public void navProfilePage (View view){
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void changePic (View view){
        Toast.makeText(EditProfileActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }

    public void saveProfileEdit (View view){
        Toast.makeText(EditProfileActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }
    /*TODO implement upload new pic function*/
    /*TODO implement save button functionality*/
}