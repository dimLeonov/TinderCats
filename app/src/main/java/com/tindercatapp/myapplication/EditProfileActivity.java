
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

public class EditProfileActivity extends AppCompatActivity
{
    private EditText mNameField,mPhoneField;
    private Button mBack, mConfirm;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mCostumerDatabase;
    private String userId, name, phone, profileImageUrl;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

     /*   mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);*/

        // database references
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCostumerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("male").child(userId);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);

            }
        });


    }

        public void saveUserInformation()
        {
            name = mNameField.getText().toString();
            phone = mPhoneField.getText().toString();

            Map userInfo = new HashMap();
            userInfo.put("name",name);
            userInfo.put("phone",phone);
            mCostumerDatabase.updateChildren(userInfo);

            if(resultUri!=null)
            {
                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
                Bitmap bitmap = null;
                try
                {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();}


                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map userInfo = new HashMap();
                                userInfo.put("profileImageUrl", uri.toString());
                                mCostumerDatabase.updateChildren(userInfo);
                                finish();
                                return;
                            }
                        });


                    }


                });
            }

            else
            {
                finish();
            }
        }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode,resultCode,data);
            if(requestCode==1 && resultCode == Activity.RESULT_OK)
            {
                final Uri imageUri = data.getData();
                resultUri = imageUri;
                mProfileImage.setImageURI(resultUri);
            }
        }
        private void getUserInfo()
        {
            mCostumerDatabase.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                    {
                        Map<String,Object> map = (Map<String,Object>)dataSnapshot.getValue();

                        if(map.get("name")!=null)
                        {
                            name = map.get("name").toString();
                            mNameField.setText(name);
                        }
                        if(map.get("phone")!=null)
                        {
                            phone = map.get("phone").toString();
                            mPhoneField.setText(phone);
                        }
                        if(map.get("profileImageUrl")!=null)
                        {
                            profileImageUrl = map.get("profileImageUrl").toString();
                            Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

   public void saveProfileEdit (View view){
        Toast.makeText(EditProfileActivity.this, "I don't work yet :(", Toast.LENGTH_SHORT).show();
       saveUserInformation();
        return;
    }
 public void navProfilePage (View view){
     Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
     startActivity(intent);
     return;
 }
    }

