package com.tindercatapp.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;

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

public class LoginActivity extends AppCompatActivity {

    private Button mLogin;
    private TextView mRegister;
    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private static final int RC_GOOGLE_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton facebookLoginButton;
    private com.google.android.gms.common.SignInButton mGoogleSignInButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLogin = findViewById(R.id.login);
        mRegister = findViewById(R.id.register);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mGoogleSignInButton = findViewById(R.id.sign_in_button);

        /* 1 Standart Login*/
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d("TAG", "FirebaseAuthState changed: " + firebaseAuth);
                FirebaseUser user = mAuth.getCurrentUser();
                if (user !=null){
                    Log.i("TAG", "Valid mAuth user detected and AuthStateChanged");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Sign In error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        /* 2 Facebook Login */
        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TAG", " Facebook registerCallback called. Login to Facebook successful. Your login result: " + loginResult);
                AccessToken resultToken = loginResult.getAccessToken();
                String successToken = loginResult.getAccessToken().getToken(); // AccessToken's String value
                Log.d("TAG", "Your Success Token (String): "+ successToken);
                FirebaseUser currrentUser = mAuth.getCurrentUser(); // So far this is null

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                Log.d("TAG", "Your currentAccessToken: " + accessToken);
                Log.d("TAG", "Logged in? (AccessToken): " + isLoggedIn);

                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));

                Profile mProfile = Profile.getCurrentProfile(); // Facebook profile
                Log.d("TAG", "Current Facebook profile: " + mProfile);

                if(mProfile != null) {
                    Log.d("TAG", "Facebook profile succesfully retrieved");
                    handleFacebookAccessToken(resultToken, mProfile);
                    GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.d("TAG", "Graphrequest succesfully called and completed");
                            getFacebookData(object);
                        }
                    });
                } else {
                    Log.e("TAG", "Error: Current Facebook profile Null");
                }
                //TODO possible UI update
            } // Facebook login callback ends here

            @Override
            public void onCancel() {
                Log.d("TAG", "Facebook callback has been cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("TAG", " Facebook registerCallback called. Login encountered an error: " + error);
            }
        }); // Facebook login button ends

        /* 3 Google Sign In */
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
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
                // TODO possible UI update
            }
        }
    }

    /* Google Sign In */
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

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
    } // Registering with Google ends

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogleAccount:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            System.out.println(user);
                            // TODO - possible UI update while loading
                        } else {
                            Log.w("TAG", "Firebase sign in with Google credentials failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    } /* Google Sign In Ends*/

    /* Facebook Login */
    private void handleFacebookAccessToken(AccessToken token, final Profile mProfile) {
        Log.i("TAG", "Handling FB with token: " + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.i("TAG", "FacebookAuthProvider credential acquired: " + credential);
        mAuth.signInWithCredential(credential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG", "Signing in Firebase with Facebook successful");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Log.i("TAG", "User Email: " + user.getEmail());
                        registerUserWithFaceBook(mProfile);
                    }
                } else {
                    Log.e("TAG", "Signing in Firebase failed.");
                }
            }
        });
    } // handleFacebookToken ends here

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

    private void registerUserWithFaceBook (final Profile mProfile) {
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());
        Map <String, Object> userInfo = new HashMap<>();
        userInfo.put("name", mProfile.getFirstName());
        try {
            currentUserDb.updateChildren(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Log.i("TAG", "Firstname: " + mProfile.getFirstName() + ", Lastname: " + mProfile.getLastName() + ", ID: " + mProfile.getId() + ", Email: NaN");
                    // TODO possible UI update
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "Registering to Firebase with Facebook failed: " + e);
        }
    } /* Registering with Facebook ends */

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    public void navRegisterPage (View view){
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
        return;
    }
}