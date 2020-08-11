package com.example.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class Login extends AppCompatActivity {

    EditText mEmail,mPassword;
    Button mloginBtn;
    TextView mCreateBtn;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    TextView ForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail=findViewById(R.id.editTextTextEmailAddress);
        mPassword=findViewById(R.id.editTextTextPassword);
        progressBar=findViewById(R.id.progressBar2);
        mloginBtn=findViewById(R.id.LoginBtn);
        mCreateBtn=findViewById(R.id.createText);
        fAuth=FirebaseAuth.getInstance();
        ForgotPassword=findViewById(R.id.textView7);

        mloginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=mEmail.getText().toString().trim();
                String password=mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Error is Required");
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

                //authenticate the user

                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(Login.this,"User Successfully Logged In",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
                        else{
                            Toast.makeText(Login.this,"Some Error Occured !Maybe "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });
            }
        });

           mCreateBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   startActivity(new Intent(getApplicationContext(),Register.class));
               }
           });


           ForgotPassword.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                   final EditText resetMail=new EditText(view.getContext());
                   AlertDialog.Builder passwordResetDialog=new AlertDialog.Builder(view.getContext());
                   passwordResetDialog.setMessage("Enter Your Email to receive reset link");
                   passwordResetDialog.setView(resetMail);



                   passwordResetDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           //Extract Email
                           String mail=resetMail.getText().toString();
                           fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   Toast.makeText(Login.this,"Reset link sent to your Email",Toast.LENGTH_SHORT).show();
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {

                                   Toast.makeText(Login.this,"Some Error occured "+e.getMessage(),Toast.LENGTH_SHORT).show();

                               }
                           });

                       }
                   });

                   passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           //Closing Dialog
                       }
                   });

                   passwordResetDialog.create().show();

               }
           });


    }
}