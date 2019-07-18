package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tindercatapp.myapplication.Matches.MatchesActivity;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void navMainPage (View view){
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        startActivity(intent);
        return;
    }

    public void navMatchesPage (View view){
        Intent intent = new Intent(EditProfileActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    public void logoutUser(View view) {
        /*TODO implement sign out method here!!!*/
        Intent intent = new Intent(EditProfileActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

}