
//Created by Natalia 18.7

package com.tindercatapp.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private EditText mNameField, mLocationField, mAgeField, mBioField;
    private RadioGroup mSexField;
    private RadioButton radioButtonM, radioButtonF;
    private ImageView mProfileImage;
    private String userId, name, location, sex, age, bio, profileImageUrl;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        mNameField = (EditText) findViewById(R.id.name);
        mLocationField = (EditText) findViewById(R.id.location);
        mSexField = (RadioGroup) findViewById(R.id.sex);
        mAgeField = (EditText) findViewById(R.id.age);
        mBioField = (EditText) findViewById(R.id.bio);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        radioButtonM = (RadioButton) findViewById(R.id.SexRadioMale);
        radioButtonF = (RadioButton) findViewById(R.id.SexRadioFemale);

        // database references
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Cats").child(userId);

        setUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);


            if (resultUri != null) {
                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data2 = baos.toByteArray();

                UploadTask uploadTask = filepath.putBytes(data2);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();
                    }


                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map userInfo = new HashMap();
                                userInfo.put("profileImageUrl", uri.toString());
                                databaseReference.updateChildren(userInfo);
                            }
                        });
                    }
                });
            }
            else{
                finish();
            }


        }
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
                        if(sex.contains("Male")){
                            radioButtonM.setChecked(true);
                        }
                        else{
                            radioButtonF.setChecked(true);
                        }



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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void saveUserInformation() {
        name = mNameField.getText().toString();
        location = mLocationField.getText().toString();

        int checkedSex = mSexField.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) mSexField.findViewById(checkedSex);
        sex = radioButton.getText().toString();

        age = mAgeField.getText().toString();
        bio = mBioField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("location", location);
        userInfo.put("sex", sex);
        userInfo.put("age", age);
        userInfo.put("bio", bio);
        databaseReference.updateChildren(userInfo);

    }

    public void saveProfileEdit(View view) {
        saveUserInformation();
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void navProfilePage(View view) {
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}


