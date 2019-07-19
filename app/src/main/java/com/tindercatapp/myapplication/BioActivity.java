package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class BioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bio);
    }

    public void navProfilePage (View view){
        Intent intent = new Intent(BioActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

}