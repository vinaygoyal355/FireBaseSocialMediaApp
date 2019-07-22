package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;
    private Button btnCreatePost;
    private ImageView postImageView;
    private EditText edtDes;
    private ListView usersListView;
    private Bitmap receivedImageBitmap;
    private String imageIdentifier;
    private ArrayList<String> userNames;
    private ArrayAdapter adapter;
    private ArrayList<String> uids;
    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        mAuth = FirebaseAuth.getInstance();

        btnCreatePost=findViewById(R.id.btnCreatePost);
        edtDes=findViewById(R.id.edtDes);
        postImageView=findViewById(R.id.postImageView);
        usersListView=findViewById(R.id.usersListView);

        uids=new ArrayList<>();

        userNames=new ArrayList<>();
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,userNames);
        usersListView.setAdapter(adapter);

        usersListView.setOnItemClickListener(this);

        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadImageToServer();

            }
        });

        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1000 && resultCode== Activity.RESULT_OK){

            Uri selectedImage=data.getData();

            try{

                receivedImageBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                postImageView.setImageBitmap(receivedImageBitmap);

            }
            catch (Exception e){
                e.getStackTrace();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.logOutItem){

            mAuth.signOut();
            transitionToSignUpActivity();

        }

        else if(item.getItemId()==R.id.viewPost){

            Intent A=new Intent(SocialMediaActivity.this,ViewSentPostActivity.class);
            startActivity(A);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.my_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        transitionToSignUpActivity();
    }

    public void transitionToSignUpActivity(){
        Intent A=new Intent(SocialMediaActivity.this,MainActivity.class);
        startActivity(A);
        finish();
    }

    public void selectImage(){

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(SocialMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1000);
        }

        else if(Build.VERSION.SDK_INT < 23){
            Intent A=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(A,1000);
        }

        else{
            Intent A=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(A,1000);
        }

    }

    private void uploadImageToServer(){

        if(receivedImageBitmap != null) {

            // Get the data from an ImageView as bytes
            postImageView.setDrawingCacheEnabled(true);
            postImageView.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            receivedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // for new image id
            imageIdentifier = UUID.randomUUID() + ".png";

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads

                    Toast.makeText(SocialMediaActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...

                    Toast.makeText(SocialMediaActivity.this, "Upload Process is successful", Toast.LENGTH_SHORT).show();
                    edtDes.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("My_Users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            uids.add(dataSnapshot.getKey());
                            String username= (String) dataSnapshot.child("username").getValue();
                            userNames.add(username);
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {



                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if(task.isSuccessful()){

                                imageDownloadLink=task.getResult().toString();

                            }
                            else{
                                Toast.makeText(SocialMediaActivity.this,"Error in getting image url",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            });
        }
        else{

            Toast.makeText(SocialMediaActivity.this,"Image Bitmap is empty: Please select an image",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if(edtDes.getText().toString().equals("")){
            Toast.makeText(SocialMediaActivity.this,"Please enter a description first",Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String,String> dataMap=new HashMap<>();

        dataMap.put("fromWhom",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier",imageIdentifier);
        dataMap.put("imageLink",imageDownloadLink);
        dataMap.put("des",edtDes.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("My_Users").child(uids.get(i))
                .child("Received_Post").push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(SocialMediaActivity.this,"Data is sent",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SocialMediaActivity.this,"Error in sending data",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
