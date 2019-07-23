package com.tindercatapp.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegister;
    private EditText mEmail, mPassword, mName;
    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private static final int RC_GOOGLE_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private com.google.android.gms.common.SignInButton mGoogleSignInButton;
    private LoginButton facebookLoginButton;
    private CallbackManager mCallbackManager;
    private AccessToken facebookAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mRegister = findViewById(R.id.register);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mName = findViewById(R.id.name);
        mRadioGroup = findViewById(R.id.radioGroup);
        mGoogleSignInButton = findViewById(R.id.sign_in_button);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d("TAG", "AuthStateChange event detected");
                final FirebaseUser user = mAuth.getCurrentUser();
                if (user !=null){
                    Log.d("TAG", "Valid firebase user detected");
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        /* 2 Facebook Login */
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TAG", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("TAG", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "facebook:onError", error);
            }
        });
        /* Facebook login ends here */
        /* 3 Google Sign In */
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        }); /* Google Sign In ends */

        /* 1 Standart Register */
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectId = mRadioGroup.getCheckedRadioButtonId();

                final RadioButton radioButton = (RadioButton) findViewById(selectId);

                if(radioButton.getText() == null){
                    return;
                }

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                        }else{
                            String userId = mAuth.getCurrentUser().getUid();

                            //Added by Amal
                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(userId);
                            Map userInfo = new HashMap<>();
                            userInfo.put("name", name);
                            userInfo.put("sex", radioButton.getText().toString());
                            userInfo.put("profileImageUrl", "default");
                            userInfo.put("age", "0");
                            userInfo.put("location", "");

                            currentUserDb.updateChildren(userInfo);
                            currentUserDb.child("settings").child("mute").setValue(false);

                            currentUserDb.child("settings").child("level").setValue(20);
                            currentUserDb.child("settings").child("happysound").setValue("happy1");
                            currentUserDb.child("settings").child("nopesound").setValue("nope1");



                            //currentUserDb.setValue(name);
                            // DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(radioButton.getText().toString()).child(userId).child("name");
                            //DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(radioButton.getText().toString()).child(userId).child("name");
                            //currentUserDb.setValue(name);

                        }
                    }
                });
            }
        });/* Register ends here */
    } /* onCreate ends here */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Google
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Google Sign In Failed: ", e);
                // TODO possible UI update here - show Gogole Sign in failed
            }
        }
    } // onActivityResult ends here

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            registerUserWithFaceBook(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            LoginManager.getInstance().logOut();
                        }
                    }
                });
    }

    private void getFacebookData(JSONObject object) {
        try {
            Log.d("TAG", "Facebook JSON retrieved: " + object);
            URL profile_picture = new URL("https://graph.facebook.com/" + object.getString("id") + "/picture?width=250&height=250");
            String email = object.getString("email");
            System.out.println(email);
            String bday = object.getString("birthday");
            System.out.println(bday);
        } catch (JSONException e) {
            Log.e("TAG", "JSONException: " + e);
        } catch (MalformedURLException e) {
            Log.e("TAG", "URLException: " + e);
        }
    }

    private void registerUserWithFaceBook (final FirebaseUser user) {
        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());
        Map <String, Object> userInfo = new HashMap<>();
        userInfo.put("name", user.getDisplayName());
        try {
            currentUserDb.updateChildren(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Log.d("TAG", "Name: " + user.getDisplayName() + ", ID: " + user.getUid() + ", Email: " + user.getEmail());
                    Toast.makeText(RegistrationActivity.this, "Database updated with Facebook data", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "Registering to Firebase with Facebook failed: " + e);
        }
    } // Registering with Facebook ends
    /* Facebook Login ends */

    /* Google Sign In */
    // Google Sign In Intent creation
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogleAccount:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("TAG", "signInFirebase With Google Credentials: success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                          // TODO possible UI update here - display rotating screen until loaded to main
                        } else {
                            Log.w("TAG", "signInFirebase With Google Credentials: failure. Exception: ", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Firebase Sign in has failed", Toast.LENGTH_SHORT).show();
//                          // TODO possible UI update - Could not sign in to firebase with Google account
                        }
                    }
                });
    } // Firebase auth with google ends here

    private void registerUserWithGoogle (GoogleSignInAccount account) {
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());

        Log.i("TAG", "User is registering with google.");
        String firstName = account.getGivenName(); // user does not provide only the first name, so account is used here
        System.out.println("User first name is is: " + firstName);
        Log.i("TAG","Google ID: " + account.getId());

        Map <String, Object> userInfo = new HashMap<>();
        userInfo.put("name", firstName);
        Log.v("TAG", "CurrentUserDB: " + currentUserDb);
        try {
            currentUserDb.updateChildren(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    // TODO possible UI update
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "Registering to Firebase with Google failed: " +  e);
        }
    }  /* Google Sign In ends */

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

    public void navLoginPage (View view){
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        return;
    }
}