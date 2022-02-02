package com.example.chat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.chat.Models.Message;
import com.example.chat.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Message> {
    //Bu adaptörde emaile erişim sağlayabilmek amacı ile FirebaseUser sınıfından bir nesne oluşturduk
    private FirebaseUser firebaseUser;

    public CustomAdapter(Context context, ArrayList<Message> chatList, FirebaseUser firebaseUser) {
        // bu constructor tüm adaptör sınıflarında olur activitiden gelen bilgiyi adaptör içinde kullanmanı sağlar
        super(context, 0, chatList);
        this.firebaseUser = firebaseUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Mesaj sınıfından mesajı çekiyoruz


        Message message = getItem(position);
        //Gönderici emaili benim email'im ile aynıysa sağda göster
        if (firebaseUser.getEmail().equalsIgnoreCase(message.getGonderici())){

            //burda view sınıfından üretilen convertView nesnesine hangi layoutu recylerviewin tek bir rowuna implement etmesi gerektiğini söylüyoruz
            // buna şu şekilde karar veriyoruz yukarıdaki ifdeki çektiğimiz email eğer mesajdaki e maile eşitese sağdaki right_item_layout u çağır bu şekilde sağda göstermiş ol mesajı
            // aksi halde aşağıdaki else kısmına girip solda göster diyoruz
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.right_item_layout, parent, false);

            // burda layout üzerindeki bileşenleri tanımlıyoruz id leri ile birlikte
            TextView txtUser = (TextView) convertView.findViewById(R.id.txtUserRight);
            TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessageRight);
            TextView txtTime = (TextView) convertView.findViewById(R.id.txtTimeRight);

            // burda tanımladığımız bileşenlerde neyin gösterilmesini istediğimizi giriyoruz
            txtUser.setText(message.getGonderici());
            txtMessage.setText(message.getMesajText());
            txtTime.setText(message.getZaman());

        }else{

            //Değilse solda göster(başkası atmış mesajı demek)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.left_item_layout, parent, false);

            //üst tarafın aynısı sol layoutun seçilmiş hali
            TextView txtUser = (TextView) convertView.findViewById(R.id.txtUserLeft);
            TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessageLeft);
            TextView txtTime = (TextView) convertView.findViewById(R.id.txtTimeLeft);

            txtUser.setText(message.getGonderici());
            txtMessage.setText(message.getMesajText());
            txtTime.setText(message.getZaman());

        }

        return convertView;
    }
}
