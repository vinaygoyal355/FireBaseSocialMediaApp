package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail,edtUserName,edtPassword;
    private Button btnSignUp,btnSignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail=findViewById(R.id.edtEmail);
        edtPassword=findViewById(R.id.edtPassword);
        edtUserName=findViewById(R.id.edtUserName);

        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignUp=findViewById(R.id.btnSignUp);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SignIn();

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edtPassword.getText().toString().equals("") || edtUserName.getText().toString().equals("") || edtEmail.getText().toString().equals("")) {

                    Toast.makeText(MainActivity.this,"Username, Email and Password are Required",Toast.LENGTH_SHORT).show();
                    return;
                }
                SignUp();

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            // Transition to next activity
        }
        else{
        }

    }

    private void SignUp(){

        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(),
                edtPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(MainActivity.this,"Signing Up Successful",Toast.LENGTH_SHORT).show();
                    // Transition to next Activity
                    transitionToSocialMediaActivity();
                }
                else{
                    Toast.makeText(MainActivity.this,"Signing Up failed",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SignIn(){

        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"SignIn Successful",Toast.LENGTH_SHORT).show();
                            transitionToSocialMediaActivity();
                        }
                        else{
                            Toast.makeText(MainActivity.this,"SignIn Failed",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
    public void transitionToSocialMediaActivity(){

        Intent A=new Intent(MainActivity.this,SocialMediaActivity.class);
        startActivity(A);

    }
}
