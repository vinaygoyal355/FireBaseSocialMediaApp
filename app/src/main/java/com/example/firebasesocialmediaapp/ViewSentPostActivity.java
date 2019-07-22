package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewSentPostActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView postListView;
    private ArrayList<String> userNames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ImageView sentPostImageView;
    private TextView txtDescription;
    private ArrayList<DataSnapshot> dataSnapshots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sent_post);

        sentPostImageView=findViewById(R.id.sentPostImageView);
        txtDescription=findViewById(R.id.txtDescription);

        postListView=findViewById(R.id.postListView);
        postListView.setOnItemClickListener(this);
        postListView.setOnItemLongClickListener(this);
        postListView.setOnItemLongClickListener(this);

        userNames=new ArrayList<>();
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,userNames);
        postListView.setAdapter(adapter);

        dataSnapshots=new ArrayList<>();

        firebaseAuth=FirebaseAuth.getInstance();

        FirebaseDatabase.getInstance().getReference().child("My_Users").child(firebaseAuth.getCurrentUser().getUid()).child("Received_Post").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                dataSnapshots.add(dataSnapshot);

                String fromWhomPostReceived= (String) dataSnapshot.child("fromWhom").getValue();
                userNames.add(fromWhomPostReceived);
                adapter.notifyDataSetChanged();
                
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i=0;

                for(DataSnapshot snapshot: dataSnapshots){

                    if(snapshot.getKey().equals(dataSnapshot.getKey())){

                        dataSnapshots.remove(i);
                        userNames.remove(i);

                    }

                    i++;
                }
                adapter.notifyDataSetChanged();
                sentPostImageView.setImageResource(R.drawable.placeholder);
                txtDescription.setText("");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        DataSnapshot myDataSnapshots=dataSnapshots.get(i);
        String downloadLink=(String)myDataSnapshots.child("imageLink").getValue();

        Picasso.with(this).load(downloadLink).into(sentPostImageView);
        txtDescription.setText((String)myDataSnapshots.child("des").getValue());

    }


    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ViewSentPostActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ViewSentPostActivity.this);
        }
        builder.setTitle("Delete Entry")
                .setMessage("Are you sure you wanna delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete

                        FirebaseStorage.getInstance().getReference().child("my_images/")
                                .child( dataSnapshots.get(position).getKey())
                                 .child("imageIdentifier")
                                .delete();

                        FirebaseDatabase.getInstance().getReference().child("My_Users")
                                .child(firebaseAuth.getCurrentUser().getUid()).child("Received_Post")
                                .child(dataSnapshots.get(position).getKey()).removeValue();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();

        return false;
    }
}
