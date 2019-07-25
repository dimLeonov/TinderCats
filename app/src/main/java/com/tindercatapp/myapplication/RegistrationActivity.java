package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegisterButton;
    private EditText mEmail, mPassword, mName, mLocation,mAge,mBio;
    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initializeFirebase();
        initializeXML();

        mRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String name = mName.getText().toString();
                final String password = mPassword.getText().toString();
                final String location = mLocation.getText().toString();
                final String age = mAge.getText().toString();
                final String bio = mBio.getText().toString();
                final RadioButton radioButton = (RadioButton) findViewById(mRadioGroup.getCheckedRadioButtonId());
                if (radioButton == null) {
                    Toast.makeText(RegistrationActivity.this, "Select your cat's gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (inputValidation(email, name, password, location, age) == true) {
                    Log.d("REGISTER", "All inputs valid");
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, "Sign up error!", Toast.LENGTH_SHORT).show();
                            } else {
                                String userId = mAuth.getCurrentUser().getUid();

                                //Added by Amal
                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(userId);
                                Map userInfo = new HashMap<>();
                                userInfo.put("name", name);
                                userInfo.put("location", location);
                                userInfo.put("age", age);
                                userInfo.put("bio", bio);
                                userInfo.put("sex", radioButton.getText().toString());

                                currentUserDb.updateChildren(userInfo);
                                currentUserDb.child("settings").child("mute").setValue(false);
                                currentUserDb.child("settings").child("level").setValue(20);
                                currentUserDb.child("settings").child("happysound").setValue("happy1");
                                currentUserDb.child("settings").child("nopesound").setValue("nope1");
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean inputValidation(String email, String name, String password, String location, String age) {
        Boolean status = true;
        StringBuilder sb = new StringBuilder();
        if (email == null || email.length() < 9) {
            sb.append("Email");
        }
        if (name == null || name.length() < 3) {
            if (sb.toString().length() != 0){
                sb.append(", ");
            }
            sb.append("Name");
        }
        if (password == null || password.length() < 6) {
            if (sb.toString().length() != 0) {
                sb.append(", ");
            }
            sb.append("Password");
        }
        if (location == null || location.length() < 3) {
            if (sb.toString().length() != 0) {
                sb.append(", ");
            }
            sb.append("Location");
        }
        if (age == null) {
            if (sb.toString().length() != 0) {
                sb.append(", ");
            }
            sb.append("Age");
        } else {
            try {
                int numberAge = Integer.parseInt(age);
                if (numberAge < 0 || numberAge > 32) {
                    if (sb.toString().length() != 0) {
                        sb.append(", ");
                    }
                    sb.append("Age");
                }
            } catch (NumberFormatException e) {
                Log.d("REGISTRATION", e.toString());
                if (sb.toString().length() != 0) {
                    sb.append(", ");
                }
                sb.append("Age");
            }
        }


        if(sb.toString().length() != 0) {
            sb.append(" fields are invalid");
            status = false;
            Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
        }
        return status;
    }

    public void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    public void initializeXML() {
        mRegisterButton = findViewById(R.id.register);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mName = findViewById(R.id.name);
        mLocation = findViewById(R.id.location);
        mAge = findViewById(R.id.age);
        mBio = findViewById(R.id.bio);
        mRadioGroup = findViewById(R.id.radioGroup);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void navChooseLoginRegistration(View view) {
        Intent intent = new Intent(RegistrationActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
    }

    public void navLoginPage(View view) {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}