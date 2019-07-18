package com.tindercatapp.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.tindercatapp.myapplication.Matches.MatchesActivity;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
    }

    public void navMainPage (View view){
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        return;
    }

    public void editProfilePage (View view){

        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void navSettingsPage (View view){

        Toast.makeText(ProfileActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }

    public void navMatchesPage (View view){
        Intent intent = new Intent(ProfileActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    public void logoutUser(View view) {
        Toast.makeText(ProfileActivity.this, "Signing out", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

}