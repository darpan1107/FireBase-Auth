package com.example.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    TextView fullname,email,phone;
    FirebaseFirestore fStore;
    String userId;
    FirebaseAuth fAuth;
    Button resend;
    TextView verifymssg;
    Button resetPassLocal;
    FirebaseUser user;
    Button changeProfile;
    ImageView profileImage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fullname=findViewById(R.id.textView4);
        email=findViewById(R.id.textView5);
        phone=findViewById(R.id.textView6);


        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        StorageReference profileRef=storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get().load(uri).into(profileImage);
            }
        });

        resend=findViewById(R.id.reVerify);
        verifymssg=findViewById(R.id.textView8);
        user=fAuth.getCurrentUser();
        resetPassLocal=findViewById(R.id.resetpassword);
        changeProfile=findViewById(R.id.button3);
        profileImage=findViewById(R.id.imageView);

        if(!user.isEmailVerified()){
            resend.setVisibility(View.VISIBLE);
            verifymssg.setVisibility(View.VISIBLE);

            resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(MainActivity.this,"Verification Email Sent",Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d("tag","Email not sent"+e.getMessage());
                        }
                    });
                }
            });
        }

        userId=fAuth.getCurrentUser().getUid();

        DocumentReference documentReference= fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                phone.setText(documentSnapshot.getString("phone"));
                fullname.setText(documentSnapshot.getString("fName"));
                email.setText(documentSnapshot.getString("email"));

            }
        });


         resetPassLocal.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 final EditText resetPassword=new EditText(view.getContext());
                 final AlertDialog.Builder passwordResetDialog=new AlertDialog.Builder(view.getContext());
                 passwordResetDialog.setTitle("Reset Password");
                 passwordResetDialog.setMessage("Enter Your new password");
                 passwordResetDialog.setView(resetPassword);



                 passwordResetDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         //Extract new password
                         String newPassword=resetPassword.getText().toString();
                         user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                               Toast.makeText(MainActivity.this,"Password Reset Successfully.",Toast.LENGTH_SHORT).show();

                             }
                         }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 Toast.makeText(MainActivity.this,"Some Problem Occured "+e.getMessage(),Toast.LENGTH_SHORT).show();

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


         changeProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //open gallary
                 Intent openGalleryIntent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 startActivityForResult(openGalleryIntent,1000);
             }
         });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000){
            if(resultCode== Activity.RESULT_OK){
                Uri imageUri =data.getData();
             //   profileImage.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);

            }
        }
    }


      public void uploadImageToFirebase(Uri imageUri){

        //uploadImage to firebase storage
          final StorageReference fileRef=storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
          fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 // Toast.makeText(MainActivity.this,"ImageUploaded",Toast.LENGTH_SHORT).show();
                  fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                      @Override
                      public void onSuccess(Uri uri) {
                          Picasso.get().load(uri).into(profileImage);
                      }
                  });
              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  Toast.makeText(MainActivity.this,"Some Error Occured "+e.getMessage(),Toast.LENGTH_SHORT);
              }
          });

      }

    public void Logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();


    }
}