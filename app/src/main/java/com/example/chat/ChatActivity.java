package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chat.Adapters.CustomAdapter;
import com.example.chat.Models.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    //Group Chatlerdeki mesajlaşma sayfalarının tümü burda gösteriliyor

    //Tanımlamalar

    private FirebaseDatabase db;// Database e ulaşmamızı sağlayan sınıftan nesne oluşturduk
    private DatabaseReference dbRef; // databasede hangi path e ulaşacağımızı belirlediğimiz sınıftan nesne oluşturduk
    private FirebaseUser fUser; // firebasein kullanıcı sınıfından bir nesne oluşturduk current user değerlerini alabilmek için
    private ArrayList<Message> chatLists = new ArrayList<>();//messageları listeleyebileceğimiz bir liste oluşturduk içine alacağı değerlerin message classına ait olucağını belirttik
    private CustomAdapter customAdapter;//custom adapter clasımıza bir refereans oluşturduk.
    private String subject;//referansın içine yazabilmek için konuyu tutacağımız string
    private ListView listView;
    private FloatingActionButton floatingActionButton;
    private EditText inputChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //İd ye göre tanımlamalar

        listView = (ListView)findViewById(R.id.chatListView);
        inputChat = (EditText)findViewById(R.id.inputChat);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);

        db = FirebaseDatabase.getInstance();// veritabanına erişim sağladık
        fUser = FirebaseAuth.getInstance().getCurrentUser(); // şuanki kullanıcının verilerini firebaseuser classının nesnesinin içine attık


        customAdapter = new CustomAdapter(getApplicationContext(),chatLists,fUser);// adaptör classımıza neler ile çalışması gerektiğini bildirdik
        listView.setAdapter(customAdapter);// listviewimizı hangi adaptörün şekillendireceğini belirledik

        //Önceki activityden gönderilen bir string bir nesne var mı diye kontrol ediyor varsa alıp veritabanına referans olarak veriyoruz
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            //Group chatin konusunu database e reference verip oraya mesaj atabileceğimiz ve alabileceğimiz referansı oluşturuyoruz
            subject = bundle.getString("subject");// önceki aktividede seçilen konuyu getirip burada referansa eklemek için tutuyoruz
            dbRef = db.getReference("ChatSubjects/"+subject+"/mesaj");// hangi konuya mesaj göndereceğimizi reference pathi ile bildiriyoruz
            setTitle(subject);
        }else{
            subject="Futbol";
            dbRef = db.getReference("ChatSubjects/"+subject+"/mesaj");// hangi konuya mesaj göndereceğimizi reference pathi ile bildiriyoruz
            setTitle(subject);
        }

        //veritabanı referansından gelen mesajları okuyoruz burada ve listeye yazdırıyoruz
        dbRef.addValueEventListener(new ValueEventListener() {// bu yapıyı her yerde kullanıyoruz-----------------------------
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatLists.clear();// chat listesini sayfayı her açtığımızda üst üste birikme olmasın diye sıfırlıyoruz
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    // message sınıfından nesne oluşturup databasein getirdiği veriyi bu classın nesnesine atıyoruz
                    chatLists.add(message);// nesneyi adaptöre göndermek için chatlist listesinde saklıyoruz
                    //Log.d("VALUE",ds.getValue(Message.class).getMesajText());
                }
                customAdapter.notifyDataSetChanged();// adaptörü notifiDataSetChanged kodu ile yeniliyoruz yeni gelen itemları alabilmesi için örneğin mesaj ve kullanıcı
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Mesajı gönderdiğimiz kısım
        floatingActionButton.setOnClickListener(new View.OnClickListener() {// butona basıldığı anda yapılacaklar
            @Override
            public void onClick(View v) {
                //mesajı girdiğimiz edittextin textini alıp uzunluğunun 6 karakterden uzun olup olmadığının kontro lediyoruz
                if(inputChat.getText().length()>=6){

                    // System.Currenttime kodu ile anlık zamanı alıyoruz ama bu bizim istediğimiz formatta değil oyüzden
                    long msTime = System.currentTimeMillis();
                    // aldığımız zamanı date sınıfı yardımı ile önce date clasına aktarıyoruz
                    Date curDateTime = new Date(msTime);
                    // çünkü date clasını formatlayabilidğimiz simpledateformat adında bir clasımız var bunda bir nesne oluşturup dateformat patternimizi veriyoruz
                    SimpleDateFormat formatter = new SimpleDateFormat("dd'/'MM'/'y hh:mm");
                    // son olarak string bir datetime oluşturup formatlanmış tarih ve saati ona aktarıyoruz
                    String dateTime = formatter.format(curDateTime);
                    //Mesaj için gerekli her kısmı doldurduktan sonra gönderdiğimiz kısım
                    Message message = new Message(inputChat.getText().toString(),fUser.getEmail(),dateTime);
                    // ardından bu bilgilerin hepsini message model classınndan nesne oluşturup içine atıyoruz ki
                    dbRef.push().setValue(message);// burada database reference sayesinde veritabanına bu mesajı yazabilelim
                    inputChat.setText("");// ardından mesaj kutumuzu boşaltıyoruz

                }else{

                    Toast.makeText(getApplicationContext(),"Gönderilecek mesaj uzunluğu en az 6 karakter olmalıdır!",Toast.LENGTH_SHORT).show();
                }


            }
        });

    }
}