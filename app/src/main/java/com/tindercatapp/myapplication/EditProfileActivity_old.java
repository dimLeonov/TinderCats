package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity_old extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile_old);
    }

    public void navProfilePage (View view){
        Intent intent = new Intent(EditProfileActivity_old.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void changePic (View view){
        Toast.makeText(EditProfileActivity_old.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }

    public void saveProfileEdit (View view){
        Toast.makeText(EditProfileActivity_old.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
        return;
    }
    /*TODO implement upload new pic function*/
    /*TODO implement save button functionality*/
}