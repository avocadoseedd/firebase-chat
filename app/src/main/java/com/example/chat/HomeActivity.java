package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    //Tanımlamalar

    private ListView listView;
    private FirebaseAuth fAuth;
    private ArrayList<String> subjectLists = new ArrayList<>();
    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Tanımlamalara referans vermek veya layoutdaki id ile eşlemek
        fAuth = FirebaseAuth.getInstance();

        listView = (ListView)findViewById(R.id.listViewSubjects);

        db = FirebaseDatabase.getInstance();
        //Veritabanından çekilecek başlıkların referansını vermek
        dbRef = db.getReference("ChatSubjects");//database referenceına Chatsubjects yolunu veriyoruz

        adapter = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1,subjectLists);
        //Çekline konuları adaptör yardımıyla listviewa yazıyoruz
        listView.setAdapter(adapter);


        //Konuşma group konularını veritabanından çekmek
        dbRef.addValueEventListener(new ValueEventListener() {// bu her yerde olan database referensından gelen verileri çekip listelediğimiz kod
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                subjectLists.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    //Çektiğimiz konuları bir listeye ekliyoruz
                    subjectLists.add(ds.getKey());//burda başlıkların içeriklerini değil kendilerini istediğimiz için direkt olarak getkey ile başlıklarını alıyoruz diğer datasnapshotlardan farkı budur
                    Log.d("LOGVALUE",ds.getKey());
                }
                //Recylerviewdaki anlık değişimi alıyor bu notifidatasetchanged
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Konulara tıklandığında chatactivitiye geçiş kodu
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                //Chat activitye intent içerisinde başlığın ne olduğunu subject stringiyle gönderiyoruz chatactivity classında da bundle ile çekiyoruz
                intent.putExtra("subject",subjectLists.get(position));
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Sağ üstteki menü
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit)
        {
            //Logout kodu
            Intent intent=new Intent(HomeActivity.this,MainActivity.class);
            startActivity(intent);
            fAuth.signOut();
            finish();
        }
        //Direct mesaj kişilerine geçtiğimiz kod
        if (item.getItemId() == R.id.users){
            Intent intent=new Intent(HomeActivity.this,UsersActivity.class);
            startActivity(intent);
        }
        //profile sayfasına geçtiğimiz kod
        if (item.getItemId() == R.id.profile){
            Intent intent=new Intent(HomeActivity.this,ProfileActivity.class);
            startActivity(intent);
        }
        //Mesajlarım sayfasına geçtiğimiz kod
         if (item.getItemId() == R.id.mymessages){
            Intent intent=new Intent(HomeActivity.this,MyChats.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}