package com.tindercatapp.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.maps.GoogleMap;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private FirebaseUser user;
    private DatabaseReference currentUserDb;

    private static final int RC_GOOGLE_SIGN_IN = 123;
    private static final int ACCESS_COARSE_LOCATION = 0;
    private GoogleSignInClient mGoogleSignInClient;
    private com.google.android.gms.common.SignInButton mSignInButton;
    private CallbackManager mCallbackManager;
    private GoogleMap mMap;

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
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        }); /* Google Sign In onCreate ends here */

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate", "Location Permission granted");
//            mMap = new GoogleMap();
//            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

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
        Log.d("FACEBOOK", "Facebook access token:" + token);
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.d("FACEBOOK", "Attempting to assign a variable to a facebook profile...");
        Profile mProfile = Profile.getCurrentProfile();
        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, final Profile currentProfile) {
                if (currentProfile != null) {
                    Log.d("FACEBOOK", "Profile retrieved with profiletracker:" + currentProfile);

                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(ChooseLoginRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("FIREBASE", "Sign In With Facebook Credential:success");
                                        final FirebaseUser user = mAuth.getCurrentUser();
                                        final String userId = user.getUid();
                                        Log.d("USER", user.getUid());
                                        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(userId);
                                        final Map userInfo = new HashMap();
                                        /* Profile Data check */
                                        currentUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Log.i("TAG", "Current datasnapshot: " + dataSnapshot.getValue());
                                                if (!dataSnapshot.hasChild("profileImageUrl")) {
                                                    Log.d("IMAGE", "User has no profile image. Requesting a cat from the API.");
                                                    getRandomCatpicURL(userId);
                                                } else {
                                                    Log.i("IMAGE", "Current profile has a picture.");
                                                }
                                                if (!dataSnapshot.hasChild("name")) {
                                                    Log.d("NAME", "Current user account has no name. User must have signed in with Facebook for the 1st time." +
                                                            " Adding user's name to DB");
                                                    userInfo.put("name", currentProfile.getFirstName());
                                                } else {
                                                    Log.i("NAME", "User has a name assigned. Therefore, this is not users's first Sign In With Facebook.");
                                                }
                                                if (userInfo.size() < 1 ) {
                                                    Log.i("USERINFO", "No new values to add to users account:" + userInfo.size());
                                                    return;
                                                } else {
                                                    try {
                                                        currentUserDb.updateChildren(userInfo, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                Log.d("FIREBASE", "Username successfully added to firebase");
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        Log.e("FIREBASE", "Registering Facebook user's name in firebase failed: " + e);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        }); /* Profile Data check Ends */
                                    } else {
                                        Log.e("FIREBASE", "Sign In With Facebook Credential failed: ", task.getException());
                                        Toast.makeText(ChooseLoginRegistrationActivity.this, "Firebase Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                    }
                                }
                            });
                    Log.d("INTENT", "User logged in with Facebook.");
                    Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
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

    /* Google Sign In methods */
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d("FIREBASE: ", "AuthWithGoogleAccount:" + account.getDisplayName());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("FIREBASE: ", "Sign In With Google Credentials: success");
                            Log.i("GOOGLEACCOUNT", "Your name:" + account.getGivenName());
                            Log.d("GOOGLEACCOUNT", "Your granted scopes:" + account.getGrantedScopes());
                            Log.d("GOOGLEACCOUNT", "Your Requested scopes:" + account.getRequestedScopes());

                            Log.d("GOOGLEACCOUNT", account.toString());

                            user = mAuth.getCurrentUser();
                            Log.i("ID", "User ID:" + user.getUid());
                            currentUserDb = FirebaseDatabase.getInstance().getReference().child("Cats").child(user.getUid());
                            final Map userInfo = new HashMap();
                            /* Profile Data check */
                            currentUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.i("TAG", "Current datasnapshot: " + dataSnapshot.getValue());
                                    if (!dataSnapshot.hasChild("profileImageUrl")) {
                                        Log.d("IMAGE", "User has no profile image. Requesting a cat from the API.");
                                        getRandomCatpicURL(null);
                                    } else {
                                        Log.i("IMAGE", "Current profile has a picture.");
                                    }
                                    if (!dataSnapshot.hasChild("name")) {
                                        Log.d("NAME", "Current user account has no name. User must have signed in with Google for the 1st time." +
                                                " Adding user's name to DB");
                                        userInfo.put("name", account.getGivenName());
                                    } else {
                                        Log.i("NAME", "User has a name assigned. Therefore, this is not users's first Sign In.");
                                    }

                                    if (!dataSnapshot.hasChild("age")) {
                                        //if no age field exists, create with no value to prevent null array exceptions
                                        userInfo.put("age", "0");
                                    }

                                    if (!dataSnapshot.hasChild("location")) {
                                        Log.i("LOCATION", "User has not provided location. Performing API call");
                                        String city = getLocation();

                                        if (city != null) {
                                            userInfo.put("location", city);
                                        } else {
                                            Log.d("LOCATION", "Could not retrieve city");
                                        }

                                    } else {
                                        Log.i("LOCATION", "User has already provided location. Skipping Geo API call.");
                                    }
                                    if (userInfo.size() < 1 ) {
                                        Log.i("USERINFO", "No new values to add to users account:" + userInfo.size());
                                        return;
                                    } else {
                                        Log.i("USERINFO", userInfo.size() + " new values to add to DB");
                                        try {
                                            currentUserDb.updateChildren(userInfo, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    Log.d("FIREBASE", "Username successfully added to firebase");
                                                }
                                            });
                                        } catch (Exception e) {
                                            Log.e("FIREBASE", "Registering Google user's name in firebase failed: " + e);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            }); /* Profile Data check Ends */
                            Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();


                        } else {
                            Log.w("FIREBASE", "Sign In With Google Credentials: failure. Exception: ", task.getException());
                            Toast.makeText(ChooseLoginRegistrationActivity.this, "Firebase Sign in has failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    } // Firebase auth with google ends here

    /* Facebook/Google pic API*/
    private void getRandomCatpicURL(@Nullable final String userId) {
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
                            if (userId == null) {
                                getCatPic(picUrl, null);
                            } else {
                                getCatPic(picUrl, userId);

                            }
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

    private void getCatPic(String picUrl, @Nullable final String userID) {
        RequestQueue queue = Volley.newRequestQueue(this);

        ImageRequest imageRequest = new ImageRequest(picUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                if (response != null) {
                    Log.d("BITMAP","Bitmap response: " + response);
                        final String userId;
                        if (userID == null) {
                            userId = user.getUid();
                        } else {
                            userId = userID;
                        }
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

    private String getLocation() {
        Log.d("LOCATION", "Method called");
        String city = null;
        try{
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            Log.d("LOCATIONMANAGER", locationManager.toString());
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);


            Log.d("LOCATION", "RETRIEVING PROVIDERS:");
            List<String> providers = locationManager.getProviders(false);
            for(int i=0; i<providers.size(); i++){
                Log.d("PROVIDER", + i + ": " + providers.get(i));
            }

            String provider = locationManager.getBestProvider(criteria, false);
            if (provider == null) {
                Log.e("PROVIDER", " NULL");
                return city;
            }
            Log.d("PROVIDER", provider.toString());
            System.out.println("LOCATION BEST PROVIDER: " + provider);
            if (ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("LOCATION", "Permissions not granted");
                // Permission is not granted
                // Request permission
                ActivityCompat.requestPermissions(
                        this,
                        new String [] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                        ACCESS_COARSE_LOCATION);
            } else {
                Log.d("LOCATION", "Permissions Granted");
            }

            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null) {
                Log.e("LOCATION", "Could not retrieve Location");
                return city;
            }
            Log.d("LOCATION RETRIEVED", location.toString());

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d("LOCATION", "latitude:" + latitude);
            Log.d("LOCATION", "longitude:" + longitude);

            Geocoder geoCoder = new Geocoder(ChooseLoginRegistrationActivity.this, Locale.getDefault());
            StringBuilder builder = new StringBuilder();
            try {
               List <Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
               String addressLine = address.get(0).getAddressLine(0);
               Log.d("LOCATION", "address: " + address);
               Log.d("LOCATION", "address.get(0): " + address.get(0));
               Log.d("LOCATION", "address.get(0).getAddressLine(0): " + address.get(0).getAddressLine(0));
                List<String> elephantList = Arrays.asList(addressLine.split(","));
                city = elephantList.get(2);

               Log.d("LOCATION", "City: " + city);
            } catch (IOException e) {
                Log.e("LOCATION", "IOException: " + e.toString());
            }
            catch (NullPointerException e) {
                Log.e("LOCATION", "NullPointerException: " + e.toString());
            }
        } catch (Exception e) {
            Log.e("LOCATION", "Exception: " + e.toString());
            e.printStackTrace();
        }
        return city.trim();
    }

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
