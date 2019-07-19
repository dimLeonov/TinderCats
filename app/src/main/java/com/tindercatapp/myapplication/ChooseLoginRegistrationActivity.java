package com.tindercatapp.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);
        mAuth = FirebaseAuth.getInstance();
    }

    public void navLoginPage (View view) {
        Intent intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        return;
    }
        public void navRegisterPage (View view){
            if(mAuth != null) {
                mAuth.signOut();
            }
            Intent intent = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
            startActivity(intent);
            return;
        }

}
