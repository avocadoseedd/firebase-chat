package com.example.chat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.MessageActivity;
import com.example.chat.Models.Chat;
import com.example.chat.Models.User;
import com.example.chat.NewProfileActivity;
import com.example.chat.R;
import com.example.chat.UsersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    //direct message için kullanıcıların listelendiği recylerviewin adaptörü
    private Context context;//Message adapterda belirttiğim gibi bu adaptörün hangi aktivitede kullanıldığını belli eden içerik contextidir activiti classı tutar.
    private List<User> mUsers;//Kullanıcıların tutulduğu liste
    private boolean ischat;// çevrimiçi çevrimdışı durumu için kullandığımız bileşen

    String theLastMessage;

    public UserAdapter(Context context, List<User> mUsers,boolean ischat) {
        // bu constructor tüm adaptör sınıflarında olur activitiden gelen bilgiyi adaptör içinde kullanmanı sağlar
        this.context = context;
        this.mUsers = mUsers;
        this.ischat = ischat;

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //recylerviewiin her bir elemanını temsil eden layoutu veriyoruz yani user_item
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Chat kısmındaki gibi user sınıfını kullanarak textviewları ve imageviewi dolduruyoruz
        User user=mUsers.get(position);//Adaptöre gönderidğimiz tüm kullanıcıları sırasına göre listeden alıp User classından oluşturduğumuz user nesnesine atıyoruz.
        holder.username.setText(user.getUsername());//Username textviewiına user nesnesine atadığımız kullanıcının kullanıcı adını yazdırıyoruz.
        if (user.getImageUrl().equals("default")){//burası şartlı şekilde dolduruluyor fotoğraf eğer defaulta eşitse mipmapden iclauncher fotoğarafını koyuyoruz
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(context).load(user.getImageUrl()).into(holder.profile_image);// eğer dolu olarak gelirse bu değer Glide sınıfını kullanarak kullanıcının ımageurl i ile profile image
            // Imageviewının içine profil resmini yerleştiriyoruz
        }

        if (ischat){
            //aşağıdaki son mesaj methodunu çağırıyoruz ve ekranda ismin altında son mesajı gösteriyoruz
            lastMessage(user.getId(),holder.last_msg);
        }else{
            //kişi online değilse son mesajı göstertmiyoruz view gone methoduyla kaldırıyoruz o kısmı
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){//Kullanıcının o anki statüsünü(çevrimiçi durumunu) getStatus ile çekiyoruz eğer online a eşit ise yeşil yuvarlar fotoğrafımızı görünür hale getirip griyi götürüyoruz
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else{// yukarıdaki işlemin tam tersini uyguluyoruz
                holder.img_off.setVisibility(View.VISIBLE);
                holder.img_on.setVisibility(View.GONE);
            }
        }else{
            holder.img_off.setVisibility(View.VISIBLE);
            holder.img_on.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User ismine tıkladığımızda messaj sayfasını açan kod
                // intent sayfa geçişlerinde ve sayfadan sayfaya veri aktarma sırasında kullanılır
                Intent intent=new Intent(context, NewProfileActivity.class);
                //mesajı atıcağımız kişinin kim olduğunu bilmek için id'sini çekip mesaj sayfasına gönderiyoruz
                intent.putExtra("userid",user.getId());
                // intente bayrak ekliyoruz ki activity ağacında en yüksek dal o olsun
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // aktiviteden aktiviteye geçişi startlıyoruz böylece kullanıcıya mesaj attığımız sayfaya geçiyoruz
                context.startActivity(intent);

            }
        });
    }


    @Override
    public int getItemCount() {
        return mUsers.size();//Kullanıcı sayısı kadar loopa gir
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //Tanımlamalar

        public TextView username;
        public ImageView profile_image;
        public ImageView img_on;
        public ImageView img_off;
        public TextView last_msg;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //burda tanımlanacak pek bir şey yok layoutta olan herşeyi idsi ile eşleştiriyoruz bunlara değerler yazılar fotoğraflar atayabilmek için.
            username=itemView.findViewById(R.id.username);
            profile_image=itemView.findViewById(R.id.profile_image);
            img_on=itemView.findViewById(R.id.img_on);
            img_off=itemView.findViewById(R.id.img_off);
            last_msg=itemView.findViewById(R.id.last_msg);
        }
    }

    //check for last message
    private void lastMessage(String userid,TextView lastmsg){
        theLastMessage="default";
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");//Chat ağacına bir referans oluşturuyoruz

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat =snapshot.getValue(Chat.class);//
                    // bu referansın oluşturduğumuz dinleyici sayesinde çocuklarına erişiyoruz yani mesajlara eğer mesajın alıcı bizim idmize eşitse(firebaseUser.getUid o anki kişinin uid'sini getirir)
                    // chat.getsender.equaluser id lastmessage a gelen mesagın göndericisinin id si eğer mesajı gönderen kişinin idsi ile eşitse veya bu durumun tam tersi durumunda gelen mesajın son mesaj olduğunu biliyoruz
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        //tüm mesajları çekip thelastmessageın içine atıyoruz ama the last message her gelen değerde değişiyor son gelen değer son mesaj olduğu için bu koşulu sağlayan son chat messajını ona atamış oluyoruz.
                        theLastMessage=chat.getMessage();
                    }
                }

                switch (theLastMessage){
                    case "default"://Lastmessageın değeri hiç değişmeyip default kaldıysa bu o konuşma içinde hiç mesaj atılmamış anlamına gelir ve lastmesage textimizi no message olarak değiştiririz.
                        lastmsg.setText("No Message");
                        break;

                    default:
                        lastmsg.setText(theLastMessage);// eğer içi doluysa textin içine String değerimizi yazarız
                        break;
                }
                theLastMessage = "default";// işlem bittikten sonra tekrar default yapıyoruz ki yeni gelen mesajlar için tekrar tekrar bu kontrol yapılsın ve lastmessage canlı bir şekilde değişebilsin
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}