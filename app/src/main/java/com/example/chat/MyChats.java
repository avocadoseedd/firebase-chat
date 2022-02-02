package com.example.chat;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.Adapters.UserAdapter;
import com.example.chat.Models.Chat;
import com.example.chat.Models.Chatlist;
import com.example.chat.Models.User;
import com.example.chat.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

public class MyChats extends AppCompatActivity {

    RecyclerView rc_users2;
    TextView tv_unread;

    private UserAdapter userAdapter;

    private List<User> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference,reference2,reference3;

    private List<Chatlist> usersList;
    private List<Chatlist> usersList1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);

        tv_unread=findViewById(R.id.unread);
        rc_users2=findViewById(R.id.rc_users2);
        rc_users2.setHasFixedSize(true);
        rc_users2.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //giriş yapmış kullanıcı ve bilgilerini aldığımız referans
        fuser= FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();
        usersList1 = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);

                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //mesaj attığımız kişileri konuşmalarım kısmına çektiğimiz kod bloğu
        reference3 = FirebaseDatabase.getInstance().getReference("friends").child(fuser.getUid());
        reference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList1.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "tasksnapshot: " + snapshot.getKey());

                    Chatlist chatlist = new Chatlist(snapshot.getKey());
                    usersList1.add(chatlist);

                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //okunmamış mesajları saydığımız kod bloğu
        reference2=FirebaseDatabase.getInstance().getReference("Chats");
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread=0;// okunmamış mesajsayısını tutan integer değer
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){// bu yapıya artık fazlasıyla alıştık
                    Chat chat=snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(fuser.getUid())&& !chat.isIsseen()){// bunada öyle sonundaki !chat.isseen() chat eğer okunmamışsa unreadı++ arttırıyoruz demek
                        unread++;
                    }
                }

                if (unread==0){
                    tv_unread.setVisibility(View.GONE);

                }else {
                    tv_unread.setVisibility(View.VISIBLE);
                    tv_unread.setText("Okunmamış mesaj("+unread+")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());// bu kısımlar notifications kapsamında
    }

    //bildirim gönderebilmek adına bilet oluşturduğumuz yer(izin)
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");// veritabanında izin için bir path oluşturup bildirimlerimizi buraya kaydediyoruz
        Token token1=new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void chatList() {
        mUsers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");// kullanıcıalrın pathini databaese reference olarak veriyoruz
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                //eğer chatlist in idsi benim id'me eşit ise sayfama ekle yani konuşmalarımı sayfama ekle
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {// bildiğimiz bu yapıyla hepsini çekiyoruz
                    User user=snapshot.getValue(User.class);
                    Log.d(TAG, "tasksnapshot: " + snapshot.getValue());
                    for (Chatlist chatlist:usersList1){
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }

                userAdapter=new UserAdapter(getApplicationContext(),mUsers,true);// user adaptöre recylerviewı hangi verilerle doldurması gerektiğini burada veriyoruz
                rc_users2.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}