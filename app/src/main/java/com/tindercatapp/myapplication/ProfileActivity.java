package com.tindercatapp.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.tindercatapp.myapplication.Matches.MatchesActivity;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void navMainPage (View view){
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void editProfilePage (View view){
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    public void navBioPage (View view){

        Intent intent = new Intent(ProfileActivity.this, BioActivity.class);
        intent.putExtra("UID", currentUserID);
        intent.putExtra("source", "profile");
        startActivity(intent);
        finish();
    }

    public void navSettingsPage (View view){

        Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void navMatchesPage (View view){
        Intent intent = new Intent(ProfileActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    public void logoutUser(View view) {
        Toast.makeText(ProfileActivity.this, "Signing out", Toast.LENGTH_SHORT).show();
        // Firebase Sign Out + Should sign out Facebook too
        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("TAG", "Google Sign Out successful.");
            }
        });
        // Facebook log out
        LoginManager.getInstance().logOut();
        Log.i("TAG", "User Signed Out");
        Intent intent = new Intent(ProfileActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

}