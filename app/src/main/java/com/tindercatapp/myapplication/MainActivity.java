package com.tindercatapp.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.media.MediaPlayer; // added by Natalia 17.7
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.tindercatapp.myapplication.Matches.MatchesActivity;
import com.tindercatapp.myapplication.Utils.PulsatorLayout;
import com.tindercatapp.myapplication.arrayAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private arrayAdapter arrayAdapter;
    private FirebaseAuth mAuth;
    private String currentUid;
    static MediaPlayer catHissSound, catMeowSound;  // added by Natalia 17.7
    private DatabaseReference usersDb;

    FrameLayout cardFrame, moreFrame;
    SwipeFlingAdapterView flingContainer;
    List<cards> rowItems;


    static boolean isSoundMute;
    static float volume;
    static String happySoundDB ="happy1";
    static String nopeSoundDB ="nope1";

    public static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    public static final String CHANNEL_NAME = "Notification Channel";
    int importance = NotificationManager.IMPORTANCE_DEFAULT;

    // public static final int NOTIFICATION_ID = 101;

    NotificationChannel notificationChannel;
    public static NotificationManager notificationManager;
    private DatabaseReference mMatchReference;
    private ChildEventListener mMatchReferenceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // added by Natalia
        //String userSex=getIntent().getExtras().getString("userSex");
       // catMeowSound = MediaPlayer.create(this, R.raw.cat_meow); // added by Natalia 17.7
        //catHissSound = MediaPlayer.create(this, R.raw.cat_hissing);


        //Added by Amal
        cardFrame = findViewById(R.id.card_frame);
        moreFrame = findViewById(R.id.more_frame);

        // start pulsator
        PulsatorLayout mPulsator = findViewById(R.id.pulsator);
        mPulsator.start();

        usersDb = FirebaseDatabase.getInstance().getReference().child("Cats");
        mAuth = FirebaseAuth.getInstance();

        /* Added By Janis - data verification */
        /* Missing UID check. Will sign user out back to chooseloginregister page */
        try {
            currentUid = mAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            Log.e("MAINACTIVITY", "Access to Application without valid ID.");
            ProfileTracker profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    currentUid = mAuth.getCurrentUser().getUid();
                    Log.d("MAINACTIVITY", "Profile Tracker has retrieved a valid Facebook Profile. mAuth ID:" + currentUid);
                }
            };

            try {
                Log.d("THREAD", "Sleeping for 500ms to retrieve FB profile");
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Log.d("THREAD", ex.toString());
            }

            if (currentUid == null) {
                Log.d("INTENT", "Couldn't retrieve profile. Going back.");
                Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        Log.d("MAINACTIVITY", "mAuth user ID:" + currentUid);

        DatabaseReference userDb = usersDb.child(currentUid);

        if(notificationManager!=null){
            notificationManager.cancelAll();
        }

        mMatchReference =  usersDb.child(currentUid).child("connections").child("matches");

        final AtomicInteger count = new AtomicInteger();

        //Notification for new match
        mMatchReferenceListener=mMatchReference.addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                        int newCount = count.incrementAndGet();
                        System.out.println("First Time >>>>>>>>>"+newCount);
                        //sendNotification("Congratulations","You have a new match",MatchesActivity.class);
                        //Toast.makeText(MainActivity.this,"Notification on new Match", Toast.LENGTH_SHORT).show();



                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}


                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }


                });


        mMatchReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numChildren = dataSnapshot.getChildrenCount();
                System.out.println(">>>>>>>>>" +count.get() + " == " + numChildren+">>>>>>>>>>>>");
                if(count.get()<numChildren){
                    Toast.makeText(MainActivity.this,"Notification on new Match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        /* GoogleSignIn preparation */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        /* Missing profile data check */
        try {
            final DatabaseReference currentUserDb = usersDb.child(currentUid);
            currentUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.v("TAG", "Current datasnapshot: " + dataSnapshot.getValue());
                    if (!dataSnapshot.hasChild("sex") || !dataSnapshot.hasChild("profileImageUrl")
                            || !dataSnapshot.hasChild("age") || !dataSnapshot.hasChild("name")
                            || !dataSnapshot.hasChild("location")) {
                        Log.w("TAG", "User has missing information. Proceed with prompting to add info.");
                        Toast.makeText(MainActivity.this,"You have data missing. Please Update your profile.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i("TAG", "Current profile ready for Swiping. Enjoy!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (NullPointerException e) {
            Log.e("TAG", "Access to Application without valid ID.");
            Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
            startActivity(intent);
            finish();
        }
        ///////////* Missing profile data check ends *//////////////////

        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("settings").child("mute").getValue() != null) {
                        isSoundMute = Boolean.parseBoolean(dataSnapshot.child("settings").child("mute").getValue().toString());
                    }
                    if (dataSnapshot.child("settings").child("level").getValue() != null) {
                        volume = Float.parseFloat(dataSnapshot.child("settings").child("level").getValue().toString())*0.01f;
                    }

                    if (dataSnapshot.child("settings").child("happysound").getValue() != null) {
                        happySoundDB =dataSnapshot.child("settings").child("happysound").getValue().toString();
                    }

                    if (dataSnapshot.child("settings").child("nopesound").getValue() != null) {
                        nopeSoundDB =dataSnapshot.child("settings").child("nopesound").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        switch (happySoundDB){
            case "happy1": catHissSound = MediaPlayer.create(this, R.raw.cat_purr_1);  break;
            case "happy2": catHissSound = MediaPlayer.create(this, R.raw.cat_purr_2); break;
            case "happy3": catHissSound = MediaPlayer.create(this, R.raw.cat_purr_3); break;
        }
        switch (nopeSoundDB){
            case "nope1": catMeowSound = MediaPlayer.create(this, R.raw.cat_meow_1);  break;
            case "nope2": catMeowSound = MediaPlayer.create(this, R.raw.cat_meow_2); break;
            case "nope3": catMeowSound = MediaPlayer.create(this, R.raw.cat_meow_3); break;
        }



        rowItems = new ArrayList<cards>();

        checkUserSex();

        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems);

        flingContainer  = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);

        checkRowItem();
        updateSwipeCard();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void sendNotification(String title, String context, Class intent){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setVibrationPattern(new long[] {500, 500, 500, 500, 500});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        //Notification Channel ID passed as a parameter here will be ignored for all the Android versions below 8.0
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);////
        builder.setContentTitle(title);
        builder.setContentText(context);
        builder.setSmallIcon(R.drawable.ic_launcher_web);


       // Intent resultIntent = new Intent(this, intent);
       // TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
       // stackBuilder.addParentStack(intent);

        // Adds the Intent that starts the Activity to the top of the stack
       // stackBuilder.addNextIntent(resultIntent);
       // PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //builder.setContentIntent();
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +getPackageName() + "/" + R.raw.cat_meow_1);
        builder.setSound(sound);

        // builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);///
        notificationManagerCompat.notify(new Random().nextInt(100), notification);
    }


    private void  updateSwipeCard(){

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {


            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

                //Added by Amal

                checkRowItem();

                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUid).setValue(true);

                if(!isSoundMute){
                    catHissSound.setVolume(volume,volume);
                    catHissSound.start(); // added by Natalia 17.7
                }else{
                    catHissSound.setVolume(0,0);
                }

            }

            @Override
            public void onRightCardExit(Object dataObject) {

                checkRowItem();

                //Added by Amal
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUid).setValue(true);
                isConnectionMatch(userId);

                if(!isSoundMute){
                    catMeowSound.setVolume(volume,volume);
                    catMeowSound.start(); // added by Natalia 17.7
                }else{
                    catMeowSound.setVolume(0,0);
                }

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });



// Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                Intent intent = new Intent(MainActivity.this, BioActivity.class);
                intent.putExtra("source", "main");

                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                intent.putExtra("UID", userId);

                startActivity(intent);
                finish();

            }
        });



    }

// Added by Amal
    private String userSex;
    private String oppositeUserSex;
    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("sex").getValue() != null){
                        userSex = dataSnapshot.child("sex").getValue().toString();
                        switch (userSex){
                            case "Male":
                                oppositeUserSex = "Female";
                                break;
                            case "Female":
                                oppositeUserSex = "Male";
                                break;
                        }
                        getOppositeSexUsers();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //Added by Amal
    public void getOppositeSexUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("sex").getValue() != null) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUid) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUid) && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {
                        String profileImageUrl = "default";
                        int age =0;
                        String location="";

                        if(dataSnapshot.hasChild("profileImageUrl")) {
                            if (dataSnapshot.child("profileImageUrl").getValue().toString().contains("firebase")) {
                                profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                            }
                        }

                        if(dataSnapshot.hasChild("location")){
                            if(dataSnapshot.child("location").getValue()!=null){
                                location=dataSnapshot.child("location").getValue().toString();
                                if(location.length()>2) {
                                    location = location.toUpperCase().charAt(0) + location.substring(1, location.length());
                                }
                            }}


                        if(dataSnapshot.hasChild("age")){
                            if(dataSnapshot.child("age").getValue()!=null){
                                age = Integer.valueOf(dataSnapshot.child("age").getValue().toString());
                            }
                        }

                        cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl,age,location);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                        checkRowItem();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    public void navProfilePage(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    //Added Amal
    public void goToMatches(View view){
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
    }

    //Added Amal
    public void goToMainPage(View view){
        ///Intent intent = getIntent();
        //finish();
        //startActivity(intent);
    }

    //Added Amal
    private void isConnectionMatch(String userId){
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUid).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this,"new connection",Toast.LENGTH_LONG).show();

                    //Newly added by Amal
                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    //usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUid).setValue(true);
                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUid).child("ChatId").setValue(key);

                    // usersDb.child(currentUid).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                    usersDb.child(currentUid).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void navBioPage (View view){

        cards currentcard = rowItems.get(0);
        Intent intent = new Intent(MainActivity.this, BioActivity.class);
        intent.putExtra("UID", currentcard.getUserId());
        intent.putExtra("source", "main");
        startActivity(intent);
        finish();

    }


    //Added by Amal
    private void checkRowItem() {
        if (rowItems.isEmpty()) {
            moreFrame.setVisibility(View.VISIBLE);
            cardFrame.setVisibility(View.GONE);
        }else{
            moreFrame.setVisibility(View.GONE);
            cardFrame.setVisibility(View.VISIBLE);
        }
    }

}
