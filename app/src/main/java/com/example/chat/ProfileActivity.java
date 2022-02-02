package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    CircleImageView image_profile;
    TextView username;
    TextView status;
    TextView email;
    String usernameUpdate;
    DatabaseReference reference,referenceUpdate;
    FirebaseUser fuser;
    Button btn_update;
    private FirebaseAuth fAuth;
    String userid;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST=1;
    private Uri imageUri;
    private StorageTask uploadTask;
    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fAuth = FirebaseAuth.getInstance();
        userid = FirebaseAuth.getInstance().getUid();
        image_profile=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        status=findViewById(R.id.status);
        email=findViewById(R.id.email);
        btn_update=findViewById(R.id.btn_update);

        storageReference= FirebaseStorage.getInstance().getReference("uploads");

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        try{
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    email.setText(fAuth.getCurrentUser().getEmail());
                    User user=dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    status.setText(user.getStatus());
                    if(user.getImageUrl().equals("default")){
                        image_profile.setImageResource(R.mipmap.ic_launcher);
                    }else{
                        Glide.with(getApplicationContext()).load(user.getImageUrl()).into(image_profile);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception e){

        }


        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                alertDialog.setTitle("Username");
                alertDialog.setMessage("Change Username");

                final EditText input = new EditText(ProfileActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setIcon(R.drawable.user_img);

                alertDialog.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(input.getText().toString().length()>0){
                                    usernameUpdate = input.getText().toString();
                                    if(!usernameUpdate.isEmpty() ){
                                        referenceUpdate= FirebaseDatabase.getInstance().getReference("Users/"+userid);
                                        referenceUpdate.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                // inside the method of on Data change we are setting
                                                // our object class to our database reference.
                                                // data base reference will sends data to firebase.
                                                HashMap<String, Object> result = new HashMap<>();
                                                result.put("username",usernameUpdate);
                                                referenceUpdate.updateChildren(result);
                                                // after adding this data we are showing toast message.
                                                Toast.makeText(ProfileActivity.this, "data added", Toast.LENGTH_SHORT).show();

                                                referenceUpdate.removeEventListener(this);

                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // if the data is not added or it is cancelled then
                                                // we are displaying a failure toast message.
                                                Toast.makeText(ProfileActivity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }else{
                                    Toast.makeText(ProfileActivity.this, "Lütfen username inputunu doldurunuz", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                alertDialog.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();


            }
        });
    }



    private void openImage() {
        Intent intent=new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);

        /*if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ProfileActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }

         */
    }

    private String getFileExtension(Uri uri){
        //Fotoğrafı Storea koyabilmek için dönüştürme yaptığımız ve uzantısını belirledğimiz çektiğimiz kısım
        ContentResolver contentResolver=ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap =MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage(){
        //Fotoğrafı seçtiğimiz ve Gönderdiğimiz kısım
        final ProgressDialog pd=new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null){
            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return  fileReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        String mUri=downloadUri.toString();

                        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map=new HashMap<>();
                        map.put("imageUrl",mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    }else {
                        Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else{
            Toast.makeText(this, "Fotoğraf seçilmedi!", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Bu kısım olmazsa olmaz galerine erişmek için izin aldığımız kısım
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Burası Galeriyi açtığın ve fotoğrafı aldığın kısım
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data !=null && data.getData() != null){
            imageUri=data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Fotoğraf yükleniyor...", Toast.LENGTH_SHORT).show();
            }else{
                uploadImage();
            }
        }
    }

    private  void status(String status){
        // Durumunu çevrimiçi çevrimdışı gösteren kısım
        reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //durumunu sayfayı açık tuttuğun süre boyunca çevrimiçi yapan kısım
        status("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // program durduğunda durumunu çevrim dışıya çeviren kısım
            reference.removeEventListener(seenListener);
            status("offline");
        }catch (Exception e){

        }


    }
}