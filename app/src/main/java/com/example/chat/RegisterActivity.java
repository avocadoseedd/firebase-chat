package com.example.chat;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //Kayıt ol sayfası

    private EditText registerUserName;
    private EditText registerPassword;
    private EditText registerEmail;
    private Button buttonRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private String userName;
    private String userPassword;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        registerUserName = (EditText)findViewById(R.id.registerUserName);
        registerPassword = (EditText)findViewById(R.id.registerPassword);
        registerEmail = (EditText)findViewById(R.id.registerUserEmail);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();

        // register buton tiklaninca
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tüm kullanıcı bilgilerini alıyoruz eğer boş değillerse veriabanına gönderiyoruz kullanıcı kaydı olmuş oluyor
                userName = registerUserName.getText().toString();
                userPassword = registerPassword.getText().toString();
                userEmail = registerEmail.getText().toString();
                if(userName.isEmpty() || userPassword.isEmpty() || userEmail.isEmpty()){

                    Toast.makeText(getApplicationContext(),"Lütfen gerekli alanları doldurunuz!",Toast.LENGTH_SHORT).show();

                }else{

                    register();
                }

            }
        });

    }

    private void register() {//Girilen bilgileri veritabanına gönderdiğimiz kısım

        mAuth.createUserWithEmailAndPassword(userEmail,userPassword)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser firebaseUser=mAuth.getCurrentUser();
                            String userid=firebaseUser.getUid();

                            reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap= new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username",userName);
                            hashMap.put("imageUrl","default");
                            hashMap.put("status","offline");
                            hashMap.put("register",userName.toLowerCase());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent i = new Intent(RegisterActivity.this,MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            });



                        }
                        else{
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }

                    }


                });

    }
}