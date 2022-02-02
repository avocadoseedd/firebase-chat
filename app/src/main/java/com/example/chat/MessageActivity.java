package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.Adapters.MessageAdapter;
import com.example.chat.Models.Chat;
import com.example.chat.Models.User;
import com.example.chat.Notifications.APIService;
import com.example.chat.Notifications.Client;
import com.example.chat.Notifications.Data;
import com.example.chat.Notifications.MyResponse;
import com.example.chat.Notifications.Sender;
import com.example.chat.Notifications.Token;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;//circle imageview kütüphanesini gradle a ekledik bu normal imageviewdan farklı olarak yuvarlak bir imagview
    TextView username;


    FirebaseUser fuser;//Firebase kullanıcı sınıfndan oluşturudğumuz nesne getcurrent userı almak için
    DatabaseReference reference; // veritabanında erişeceğimiz yerin yolunu vereceğimiz reference verileri hem işlememizi hem çekmemizi sağlar

    FloatingActionButton btn_send;// mesaj gönderdiğimiz button
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView rc_direct;

    Intent intent;
    String userid;

    ValueEventListener seenListener;// görüldü için kullanacağımız listener sınıfı

    APIService apiService;

    boolean notify=false;

    private static final int SELECT_PICTURE=1;

    LinearLayout linear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        rc_direct=findViewById(R.id.rc_direct);
        rc_direct.setHasFixedSize(true);
        rc_direct.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        btn_send=findViewById(R.id.btn_send);
        text_send=findViewById(R.id.text_send);
        linear = (LinearLayout) findViewById(R.id.chat_linearLayout);
        //bildirim göndermek için bağlandığımız API
        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        intent=getIntent();
        userid=intent.getStringExtra("userid");


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// Chatactivity ile aynı mesaj gönderme şekli orada açıklamalr var burda ekstra boş göndermemek için !mshequal("") eklendi
                notify=true;
                long msTime = System.currentTimeMillis();
                Date curDateTime = new Date(msTime);
                SimpleDateFormat formatter = new SimpleDateFormat("dd'/'MM'/'y hh:mm");
                String dateTime = formatter.format(curDateTime);
                String msg=text_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg,dateTime);
                }else {
                    Toast.makeText(MessageActivity.this, "Boş mesaj gönderemezsiniz", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");



            }
        });

        fuser= FirebaseAuth.getInstance().getCurrentUser();// o anki kullanıcı bilgileri alındı
        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);//az önce alınan kullanıcı değerlerinden user id kısmına reference verildi


        reference.addValueEventListener(new ValueEventListener() {// o referencedan database e listener oluşturup  gelen mesajları datasnapshottan alıyoruz
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());// sol üstteki user textiviewine datasnapshottan geler user name i yazıyoruz


                if(user.getImageUrl().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
                }// aynı şekilde fotoğrafada user nesnesine gelen urlyi verip fotoğrafı yerleştiriyoruz
                Chat chat=dataSnapshot.getValue(Chat.class);//veritabanından gelen mesaj verilerini chat clasından oluşan chat nesnemizde tuttuk
                readMessagges(fuser.getUid(),userid,user.getImageUrl(),chat.getTime());// eğer mesaj varsa bu mesajları recylerviewda listeleyen methodumuzu çağırdık method tanımı aşağıdadır.

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        seenMessage(userid);// message'ın görüdlü kısmını değiştiren methodu çağırdık






    }
    //görüldü ve gönderildi kısmını sağladığımız method
    private void seenMessage(String userid){
        reference=FirebaseDatabase.getInstance().getReference("Chats");// message referensını seen listenerımıza verdik ve dinlettik
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren() ){// gelen değerleri char nesnemizde tuttuk
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){// eğer gören kişinin Uid'si mesajın göndericisine eşitse seen durumunu bu reference üzerinden trueya çevirdik
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);// databaesein referansını updatelediğimiz kızım yani yaptığımız değişikliği veritabanına gönderen od bloğu
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Mesajı ve bildirimi gönderen method
    private void sendMessage(String sender,String receiver,String message,String time){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("time",time);
        hashMap.put("isseen",false);
        hashMap.put("request",false);

        reference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatRef =FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        //add user to chat fragment
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg=message;

        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                if (notify){
                    sendNotifiaction(receiver,user.getUsername(),msg);
                }

                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Bildirimler için oluşturduğumuz method, Class ve interface kısımları projede Notifications altında
    private void sendNotifiaction(String receiver, String username, String msg) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren() ){
                    Token token=snapshot.getValue(Token.class);
                    Data data=new Data(fuser.getUid(),R.mipmap.ic_launcher,username+": "+msg,"New Message",
                            userid);

                    Sender sender=new Sender(data,token.getTokens());

                    apiService.SendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code()==200){
                                        if (response.body().success==1){
                                            // Toast.makeText(MessageActivty.this, "Hata!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void readMessagges(String myid,String userid,String imageurl,String time){
        mchat=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //uygulamayı her açtığımızda tüm mesajlar tekrar tekrar yüklenmesin diye listemizi siliyoruz aşağıda tekrar dolduruyoruz.
                mchat.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);

                    if((chat.getReceiver().equals(userid) && chat.getSender().equals(myid))|| (chat.getReceiver().equals(myid) && chat.getSender().equals(userid))){
                        mchat.add(chat);
                    }
                    //gelen mesajları recylerview a doldurduğumuz yer
                    messageAdapter=new MessageAdapter(getApplicationContext(),mchat,imageurl);
                    messageAdapter.notifyDataSetChanged();
                    rc_direct.setAdapter(messageAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Çevrimiçi çevrimdışı ayarı yaptığımız method
    private  void status(String status){
        reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);

    }


    //uygulama devam ettiğinde
    @Override
    protected void onResume() {
        super.onResume();
        status("online");

    }
    //uygulama durduğunda
    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Sağ üstteki menü
        getMenuInflater().inflate(R.menu.menu_background,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //arkaplan için seceçeğimiz fotoğrafı almak için galeriyi açtığımız yer
        if (item.getItemId() == R.id.background)
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

        }

        return super.onOptionsItemSelected(item);
    }
    //galeriden aldığımız fotoğrafı arkaplana bastığımız kısım
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            LinearLayout linear = (LinearLayout) findViewById(R.id.chat_linearLayout);
            Drawable dr = new BitmapDrawable(bitmap);
            linear.setBackgroundDrawable(dr);
        }
    }
}