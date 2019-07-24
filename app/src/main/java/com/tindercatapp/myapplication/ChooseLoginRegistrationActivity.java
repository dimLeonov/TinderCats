package com.tindercatapp.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private FirebaseUser user;
    private DatabaseReference currentUserDb;

    private static final int RC_GOOGLE_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private com.google.android.gms.common.SignInButton mSignInButton;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        initializeFirebase();
        initializeXML();


        /* Facebook Login onCreate */
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
        }); /* Facebook onCreate ends here */

        /* Google Sign In onCreate */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        }); /* Google Sign In onCreate ends here */

    } // onCreate() ends

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
                Log.w("GOOGLE: ", "Sign In Failed: ", e);
            }
        }
    } // onActivityResult() ends here

    /* Facebook Methods */
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
                            Toast.makeText(ChooseLoginRegistrationActivity.this, "Authentication failed.",
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
        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());
        Map <String, Object> userInfo = new HashMap<>();
        userInfo.put("name", user.getDisplayName());

        try {
            currentUserDb.updateChildren(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Log.d("TAG", "Name: " + user.getDisplayName() + ", ID: " + user.getUid() + ", Email: " + user.getEmail());
                    Toast.makeText(ChooseLoginRegistrationActivity.this, "Database updated with Facebook data", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "Registering to Firebase with Facebook failed: " + e);
        }
    } // Registering with Facebook ends
    /* Facebook Methods end here */


    /* Google Sign In methods */
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("FIREBASE: ", "AuthWithGoogleAccount:" + account.getDisplayName());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("FIREBASE: ", "Sign In With Google Credentials: success");
                            user = mAuth.getCurrentUser();
                            currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());

                            /* Profile Image Url Data check */
                            currentUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.i("TAG", "Current datasnapshot: " + dataSnapshot.getValue());
                                    if (!dataSnapshot.hasChild("profileImageUrl")) {
                                        Log.d("IMAGE", "User has no profile image. Requesting a cat from the API.");
                                        getRandomCatpicURL();
                                    } else {
                                        Log.i("IMAGE", "Current profile has a picture.");
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                            /* Profile Image Url check Ends */




                        } else {
                            Log.w("FIREBASE", "Sign In With Google Credentials: failure. Exception: ", task.getException());
                            Toast.makeText(ChooseLoginRegistrationActivity.this, "Firebase Sign in has failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    } // Firebase auth with google ends here

    private void registerUserWithGoogle(GoogleSignInAccount account) {
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());

        Log.i("TAG", "User is registering with google.");
        String firstName = account.getGivenName(); // user does not provide only the first name, so account is used here
        System.out.println("User first name is is: " + firstName);
        Log.i("TAG", "Google ID: " + account.getId());

        Map<String, Object> userInfo = new HashMap<>();
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
            Log.e("TAG", "Registering to Firebase with Google failed: " + e);
        }
    }
    /* Google methods end here */

    /* Facebook/Google pic API*/
    private void getRandomCatpicURL() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.thecatapi.com/v1/images/search?size=full";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("Response: " + response);
                        try {
                            JSONObject jsonObject = response.getJSONObject(0);
                            Log.i("JSON", "JSONObject: " + jsonObject);
                            String picUrl = jsonObject.getString("url");
                            Log.i("JSON","picUrl: " + picUrl);
                            getCatPic(picUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }

    private void getCatPic(String picUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);

        ImageRequest imageRequest = new ImageRequest(picUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                if (response != null) {
                    Log.d("BITMAP","Bitmap response: " + response);
                        final String userId = user.getUid();
                        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        response.compress(Bitmap.CompressFormat.JPEG,20,baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = filepath.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("UPLOADTASK", "Failure: " + e);
                                finish();
                            }
                        });
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d("FILEPATH", "UriSuccess: " + uri);
                                        Map userInfo = new HashMap();
                                        userInfo.put("profileImageUrl", uri.toString());
                                        Log.d("UserID ", userId);
                                        Log.d("profileImageUrl ", uri.toString());
                                        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(userId);
                                        currentUserDb.updateChildren(userInfo);
                                        Log.d("UPDATECHILDREN", "Successfully uploaded pic to Storage and Database: " + uri.toString() + "\n" + "Proof of profileImageUrl in Firebase: " +
                                                currentUserDb.child("profileImageUrl"));
                                        finish();
                                        return;
                                    }
                                });
                            }
                        });
                } else {
                    Log.e("BITMAP", "Bitmap Response is null. You need to get your own cat.");
                    return;
                }
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("IMAGEREQUEST", "error: " + error);
                    }
                });
        queue.add(imageRequest);
    }
    /* Facebook/Google pic API ends here */

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.i("onStart: ", "no user detected upon start");
        } else {
            Log.i("onStart: ", "user detected");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.d("AUTHSTATECHANGED", "User detected:" + user.getUid() + " Logging in..");
                    Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    public void initializeXML() {
        mSignInButton = findViewById(R.id.google_sign_in_button);
    }

    public void navLoginPage(View view) {
        Intent intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void navRegisterPage(View view) {
        if (mAuth != null) {
            mAuth.signOut();
        }
        Intent intent = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void navQuit(View view) {
        finish();
        System.exit(0);
    }
}
