package com.example.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

     EditText mFullname,mEmail,mPassword,mPhone;
     Button mRegisterBtn;
     TextView mLoginBtn;
     ProgressBar progressBar;
     FirebaseAuth fAuth;
     FirebaseFirestore fStore;
     String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullname=findViewById(R.id.editTextTextPersonName);
        mEmail=findViewById(R.id.editTextTextEmailAddress);
        mPassword=findViewById(R.id.editTextTextPassword);
        mPhone=findViewById(R.id.editTextPhone);
        mRegisterBtn=findViewById(R.id.LoginBtn);
        mLoginBtn=findViewById(R.id.createText);


        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        progressBar=findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();

        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email=mEmail.getText().toString().trim();
                String password=mPassword.getText().toString().trim();
                final String fullname=mFullname.getText().toString();
                final String phone=mPhone.getText().toString();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty((password))){
                    mPassword.setError("Password is reqired");
                    return;
                }
                if(password.length()<=6){
                    mPassword.setError("Password mush be >= 6 characters");
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            //sent verification link

                            FirebaseUser fuser=fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(Register.this,"Verification Email Sent",Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.d("OnFailure","Email not sent"+e.getMessage());
                                }
                            });


                            Toast.makeText(Register.this,"User Successfully Created",Toast.LENGTH_SHORT).show();
                            userId=fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference=fStore.collection("users").document(userId);
                            Map<String,Object> user=new HashMap<>();
                            user.put("fName",fullname);
                            user.put("email",email);
                            user.put("phone",phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG","onSuccess: User Profile is created for:"+userId);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                      Log.d("onFailure",e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
                        else{

                            Toast.makeText(Register.this,"Some Error Occured"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });

            }
        });


mLoginBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(),Login.class));

    }
});






    }
}