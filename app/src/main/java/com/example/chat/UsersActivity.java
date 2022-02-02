package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.chat.Adapters.UserAdapter;
import com.example.chat.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
//Kullanıcıları listelediğimiz sayfa (direct mesaj için)

    RecyclerView recyclerView;

    UserAdapter userAdapter;

    List<User> mUsers;

    EditText search_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        search_users=findViewById(R.id.search_users);
        recyclerView=findViewById(R.id.rc_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mUsers=new ArrayList<>();

        readUsers();

        //search kutusundan textini almak (değiştiği süre boyunca)
        search_users.addTextChangedListener(new TextWatcher() {// textwatcher text içinde arama yapmak için kullanılan bir hazır method
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());// text değiştikce içinde searhusers methodunu çağırıyourz buna göre verdiğimiz queryde arama yapıyor query aşağıdadır
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }
    //kullanıcılar arasında searchtextimize yazdığımız isimle eşleşen isimler var mı kontrol ettiğimiz methodd
    private void searchUsers(String s){

        FirebaseUser fuser= FirebaseAuth.getInstance().getCurrentUser();
        Query query= FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")// queryinin users başlığına gidip altındaki çocukların search kısımlarını okuyum
                // gelen string ile başlayan veya biten kullanıcıların değerlerini bize getiriyor
                .startAt(s)
                .endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert fuser != null;// hata almamak için aradığımız kelimeye asit user olamayacağını belli ediyoruz.

                    if (!user.getId().equals(fuser.getUid())) {
                        mUsers.add(user);
                    }
                }

                //eşleşenleri tekrar listeye ekleyip aramayı gerlekleştirmiş olduğumuz 2 kod
                userAdapter = new UserAdapter(getApplicationContext(), mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readUsers(){

        //veri tabanında Users referansındaki her şeyi çekiyor
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search_users.getText().toString().equals("")) {
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);


                        assert user != null;
                        assert firebaseUser != null;

                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(getApplicationContext(), mUsers, true);
                    recyclerView.setAdapter(userAdapter);

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}