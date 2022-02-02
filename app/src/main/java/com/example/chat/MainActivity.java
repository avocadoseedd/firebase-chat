package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //Login sayfası pek anlatılacak bir şeyi yok diğerlerinden ayrı olarak

    private EditText editTextUserName;
    private EditText editTextUserPassword;
    private Button buttonLogin;
    private TextView txtRegister;
    private TextView txtReset;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String userName;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUserName = (EditText)findViewById(R.id.editTextUserName);
        editTextUserPassword = (EditText)findViewById(R.id.editTextUserPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        txtReset=findViewById(R.id.forgot_password);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser(); // authenticate olan kullaniciyi aliyoruz eger var ise


        //Authenticate olan kullanıcı varsa daha önce giriş yapılmış demektir oyuzden login sayfasını açmadan direkt olarak uyuglamaya giriş veriyoruz
        if(firebaseUser != null){

            Intent i = new Intent(MainActivity.this,HomeActivity.class);
            startActivity(i);
            finish();
        }

        txtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ResetPasswordActivity.class);
                startActivity(i);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userName = editTextUserName.getText().toString();//yazdığımız login emailini çekiyoruz gettext ile
                userPassword = editTextUserPassword.getText().toString();// yazdığımız passwordu çekiyoruz gettext ile
                if(userName.isEmpty() || userPassword.isEmpty()){// is empty komutuyla verilerin boş olup olmadığını kontrol ediyoruz

                    Toast.makeText(getApplicationContext(),"Lütfen gerekli alanları doldurunuz!",Toast.LENGTH_SHORT).show();

                }else{

                    login();
                }
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    private void login() {
        // mAuth nesnemiz signinwithemailpaswword ile böyle bir kullanıcı var mı yok mu kontrol ediyor hazır bir koddur
        mAuth.signInWithEmailAndPassword(userName,userPassword).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){// dönen sonuç basarılı ise

                            //ChoosePage sayfamıza yönlendirme yapıyoruz
                            Intent i = new Intent(MainActivity.this,HomeActivity.class);
                            startActivity(i);
                            finish();

                        }
                        else{
                            // hata mesajını ekrana vuran kod
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }
}