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

import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReference;

    private EditText mNameField, mLocationField, mSexField, mAgeField, mBioField;
    private ImageView mProfileImage;
    private String userId, name, location, sex, age, bio, profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mNameField = (EditText) findViewById(R.id.name);
        mLocationField = (EditText) findViewById(R.id.location);
        mSexField = (EditText) findViewById(R.id.sex);
        mAgeField = (EditText) findViewById(R.id.age);
        mBioField = (EditText) findViewById(R.id.bio);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        firebaseAuth = FirebaseAuth.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Cats").child(userId);

        setUserInfo();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setUserInfo() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) {
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if (map.get("location") != null) {
                        location = map.get("location").toString();
                        mLocationField.setText(location);
                    }
                    if (map.get("sex") != null) {
                        sex = map.get("sex").toString();
                        mSexField.setText(sex);
                    }
                    if (map.get("age") != null) {
                        age = map.get("age").toString();
                        mAgeField.setText(age);
                    }
                    if (map.get("bio") != null) {
                        bio = map.get("bio").toString();
                        mBioField.setText(bio);
                    }
                    if (map.get("profileImageUrl") != null) {
                        profileImageUrl = map.get("profileImageUrl").toString();
                        if (profileImageUrl.contains("firebase")) {
                            Glide.with(getApplication())
                                    .load(profileImageUrl)
                                    .into(mProfileImage);
                        } else {
                            Glide.with(getApplication())
                                    .load(R.drawable.ic_launcher_web)
                                    .into(mProfileImage);
                        }
                        //Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void navMainPage(View view) {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void editProfilePage(View view) {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void navBioPage(View view) {

        Intent intent = new Intent(ProfileActivity.this, BioActivity.class);
        intent.putExtra("UID", userId);
        intent.putExtra("source", "profile");
        startActivity(intent);
        finish();
    }

    public void navSettingsPage(View view) {
        Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    public void navMatchesPage(View view) {
        Intent intent = new Intent(ProfileActivity.this, MatchesActivity.class);
        startActivity(intent);
        finish();
    }

    public void logoutUser(View view) {
        Toast.makeText(ProfileActivity.this, "Signing out", Toast.LENGTH_SHORT).show();
        // Firebase Sign Out + Should sign out Facebook too
        firebaseAuth.signOut();
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
    }

}