package com.example.chat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.Models.Chat;
import com.example.chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private Context context;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private List<Chat> mChats;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context context, List<Chat> mChats, String imageurl) {
        // bu constructor tüm adaptör sınıflarında olur activitiden gelen bilgiyi adaptör içinde kullanmanı sağlar
        this.context = context; // bu context'in anlamı bu adaptör hangi layouttaki recylerviewa ait bunu belirler yanı bu contextin anlamı geldiği activitynin ismidir.
        this.mChats = mChats; //list içine chat sınıfından değerler alır ve tutar
        this.imageurl = imageurl;// profil fotoğraflarının yollarını tutar
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Messaj'ı kimin attığına bağlı olarak sağda ve solda gözükeceğini seçiyoruz aşağıdaki layoutlarla

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        //chat classından recylerviewın positionına göre gelen verileri ekrana basıyoruz

        Chat chat = mChats.get(position);
        //Mesaj textvieiwine chat'deki mesajı koy
        holder.show_message.setText(chat.getMessage());
        //Zaman textviewine chat'deki zamanı koy
        holder.txt_time.setText(chat.getTime());

        //Fotoğrafa default olarak ic_launcher simgesini koy
        if (imageurl.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(imageurl).into(holder.profile_image);
        }

        if (position == mChats.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Görüldü");
            } else {
                holder.txt_seen.setText("Gönderildi");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return mChats.size();//Mesaj sayısı kadar loop'a gir
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Tanımlamalar

        public TextView show_message;
        public TextView txt_time;
        public ImageView profile_image;

        public TextView txt_seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //id ye göre tanımlama
            //chat_item_right ve chat_item_left layoutlarının içerisindeki bileşenlerin tanımı hepsi textview profile image hariç
            show_message = itemView.findViewById(R.id.show_message);
            txt_time = itemView.findViewById(R.id.txtTime);
            profile_image = itemView.findViewById(R.id.profile_image);//bu imageview
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //mesajı atan biz isek sağ tarafa
        //Firebasuser clasından oluşturduğumuz fuser nesnesine şuanda uygulamaya login olmuş kullanıcının değerlerini gönderiyoruz ordan gelen değer ile mChats listesinden gelen mesaj içeriğinin
        //Sender Uid si eşit ise bu demek oluyorki bu mesajı sen göndermişsin ozaman mesaj sağda gözükücek aksi halde solkda gözükücek
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
            //değilsek sol tarafa
        } else {
            return MSG_TYPE_LEFT;

        }
    }
}