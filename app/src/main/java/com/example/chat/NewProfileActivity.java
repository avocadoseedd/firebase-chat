package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mprofileFriendCount;
    private Button mProfileSendReqButton,mProfileDeclineReqButton,mProfileMessage;

    ProgressDialog mProgressDialog;
    private String mCurrent_state;

    DatabaseReference mfriendReqReference;
    DatabaseReference mDatabaseReference;
    DatabaseReference mFriendDatabase;
    DatabaseReference mNotificationReference;
    DatabaseReference mRootReference;
    DatabaseReference getmDatabaseReference;

    FirebaseUser mFirebaseUser;
    String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);
        //---GETTING ID OF USER WHOSE PROFILE IS OPENED----
        user_id = getIntent().getStringExtra("userid");

        //---AFTER CLICKING NOTIFICATION , IF USER_ID IS NOT FETCHED
        if(user_id==null){
            Intent intent = new Intent(NewProfileActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

//        Log.e("user_id is",user_id);

        mProfileImage = (ImageView)findViewById(R.id.profileUserImage);
        mProfileName = (TextView)findViewById(R.id.profileUserName);
        mProfileStatus=(TextView)findViewById(R.id.profileUserStatus);
        mprofileFriendCount=(TextView)findViewById(R.id.profileUserFriends);
        mProfileSendReqButton=(Button)findViewById(R.id.profileSendReqButton);
        mProfileDeclineReqButton=(Button)findViewById(R.id.profileDeclineReqButton);
        mProfileMessage=(Button)findViewById(R.id.profileMessage);

        //----IT WILL BECOME VISIBLE ONLY WHEN WE GET THE FRIEND REQUEST FROM THAT PERSON-----
        mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
        mProfileDeclineReqButton.setEnabled(false);
        mProfileMessage.setVisibility(View.INVISIBLE);
        mProfileMessage.setEnabled(false);


        mfriendReqReference= FirebaseDatabase.getInstance().getReference().child("friend_request");
        mDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationReference=FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootReference=FirebaseDatabase.getInstance().getReference();
        mFirebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        //----fOR SETTING ONLINE---
        getmDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid());

        mProgressDialog=new ProgressDialog(NewProfileActivity.this);
        mProgressDialog.setTitle("Fetching Details");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mCurrent_state = "not_friends"; // 4 types--- "not_friends" , "req_sent"  , "req_received" & "friends"

        //----ADDING NAME , STATUS AND IMAGE OF USER----
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("username").getValue().toString();
                String display_status=dataSnapshot.child("status").getValue().toString();
                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);

                //----ADDING TOTAL  NO OF FRIENDS---
                mFriendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long len = dataSnapshot.getChildrenCount();
                        mprofileFriendCount.setText("Arkada?? Say??s?? : "+len);

                        //----SEEING THE FRIEND STATE OF THE USER---
                        //----ADDING THE TWO BUTTON-----
                        mfriendReqReference.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //----CHECKING IF FRIEND REQUEST IS SEND OR RECEIVED----
                                if(dataSnapshot.hasChild(user_id)){

                                    String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                                    //arkada??l??k iste??i g??ndermi?? kisi g??nderen ise e??er profildeki buttonun textini ??ste??i geri ??ek yap
                                    if(request_type.equals("sent")){

                                        mCurrent_state="req_sent";
                                        mProfileSendReqButton.setText("??ste??i geri ??ek");
                                        mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                                        mProfileDeclineReqButton.setEnabled(false);

                                    }
                                    //arkada??l??k iste??i al??nm????sa buttonun textini Arkada??l??k iste??ini kabul et olarak de??i??tir ve g??r??n??r yap
                                    else if(request_type.equals("received")){
                                        mCurrent_state="req_received";
                                        mProfileSendReqButton.setText("Arkada??l??k iste??ini kabul et");
                                        mProfileDeclineReqButton.setVisibility(View.VISIBLE);
                                        mProfileDeclineReqButton.setEnabled(true);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                //---USER IS FRIEND----
                                else{

                                    mFriendDatabase.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                                            mProfileDeclineReqButton.setEnabled(false);



                                            if(dataSnapshot.hasChild(user_id)){
                                                mCurrent_state="friends";
                                                mProfileSendReqButton.setText("Unfriend This Person");
                                                mProfileMessage.setVisibility(View.VISIBLE);
                                                mProfileMessage.setEnabled(true);
                                            }
                                            mProgressDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            mProgressDialog.dismiss();
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(NewProfileActivity.this, "Error fetching Friend request data", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });

        mProfileMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //User ismine t??klad??????m??zda messaj sayfas??n?? a??an kod
                // intent sayfa ge??i??lerinde ve sayfadan sayfaya veri aktarma s??ras??nda kullan??l??r
                Intent intent=new Intent(NewProfileActivity.this, MessageActivity.class);
                //mesaj?? at??ca????m??z ki??inin kim oldu??unu bilmek i??in id'sini ??ekip mesaj sayfas??na g??nderiyoruz
                intent.putExtra("userid",user_id);
                // intente bayrak ekliyoruz ki activity a??ac??nda en y??ksek dal o olsun
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // aktiviteden aktiviteye ge??i??i startl??yoruz b??ylece kullan??c??ya mesaj att??????m??z sayfaya ge??iyoruz
                startActivity(intent);
            }
        });

        //-------SEND REQUEST BUTTON IS PRESSED----
        mProfileSendReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mUserId= mFirebaseUser.getUid();

                if(mUserId.equals(user_id)){
                    Toast.makeText(NewProfileActivity.this, "Kendine istek atamazs??n", Toast.LENGTH_SHORT).show();
                    return ;
                }

                Log.e("m_current_state is : ",mCurrent_state);
                mProfileSendReqButton.setEnabled(false);


                //-------NOT FRIEND STATE--------
                if(mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotificationReference = mRootReference.child("notifications").child(user_id).push();

                    String newNotificationId = newNotificationReference.getKey();

                    HashMap<String,String> notificationData=new HashMap<String, String>();
                    notificationData.put("from",mFirebaseUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("friend_request/"+mFirebaseUser.getUid()+ "/"+user_id + "/request_type","sent");
                    requestMap.put("friend_request/"+user_id+"/"+mFirebaseUser.getUid()+"/request_type","received");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    //----FRIEND REQUEST IS SEND----
                    mRootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null){

                                Toast.makeText(NewProfileActivity.this, "Arkada??l??k iste??i g??nderildi", Toast.LENGTH_SHORT).show();

                                mProfileSendReqButton.setEnabled(true);
                                mCurrent_state= "req_sent";
                                mProfileSendReqButton.setText("Arkada??l??k iste??ini iptal et");

                            }
                            else{
                                mProfileSendReqButton.setEnabled(true);
                                Toast.makeText(NewProfileActivity.this, "Bir hata ile kar????la????ld??", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                //-------CANCEL--FRIEND--REQUEST-----

                if(mCurrent_state.equals("req_sent")){

                    Map valueMap=new HashMap();
                    valueMap.put("friend_request/"+mFirebaseUser.getUid()+"/"+user_id,null);
                    valueMap.put("friend_request/"+user_id+"/"+mFirebaseUser.getUid(),null);

                    //----FRIEND REQUEST IS CANCELLED----
                    mRootReference.updateChildren(valueMap, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mProfileSendReqButton.setText("Arkada??l??k iste??i g??nder");
                                mProfileSendReqButton.setEnabled(true);
                                mProfileMessage.setVisibility(View.INVISIBLE);
                                mProfileMessage.setEnabled(false);
                                Toast.makeText(NewProfileActivity.this, "Arkada??l??k iste??i reddedildi", Toast.LENGTH_SHORT).show();
                            }
                            else{

                                mProfileSendReqButton.setEnabled(true);
                                Toast.makeText(NewProfileActivity.this, "Arkada??l??k iste??i reddedilemiyor", Toast.LENGTH_SHORT).show();

                            }
                        }

                    });




                }

                //-------ACCEPT FRIEND REQUEST------

                if(mCurrent_state.equals("req_received")){
                    //-----GETTING DATE-----
                    Date current_date=new Date(System.currentTimeMillis());

                    //Log.e("----Date---",current_date.toString());
                    String date[]=current_date.toString().split(" ");
                    final String todays_date=(date[1] + " " + date[2] + "," + date[date.length-1]+" "+date[3]);

                    Map friendMap=new HashMap();
                    friendMap.put("friends/"+mFirebaseUser.getUid()+"/"+user_id+"/date",todays_date);
                    friendMap.put("friends/"+user_id+"/"+mFirebaseUser.getUid()+"/date",todays_date);

                    friendMap.put("friend_request/"+mFirebaseUser.getUid()+"/"+user_id,null);
                    friendMap.put("friend_request/"+user_id+"/"+mFirebaseUser.getUid(),null);

                    //-------BECAME FRIENDS------
                    mRootReference.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null){

                                mProfileSendReqButton.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqButton.setText("Unfriend this person");
                                mProfileDeclineReqButton.setEnabled(false);
                                mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                                mProfileMessage.setVisibility(View.VISIBLE);
                                mProfileMessage.setEnabled(true);

                            }
                            else{
                                mProfileSendReqButton.setEnabled(true);
                                Toast.makeText(NewProfileActivity.this, "Error is " +databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }


                //----UNFRIEND---THIS---PERSON----
                if(mCurrent_state.equals("friends")){

                    Map valueMap=new HashMap();
                    valueMap.put("friends/"+mFirebaseUser.getUid()+"/"+user_id,null);
                    valueMap.put("friends/"+user_id+"/"+mFirebaseUser.getUid(),null);

                    //----UNFRIENDED THE PERSON---
                    mRootReference.updateChildren(valueMap, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mCurrent_state = "not_friends";
                                mProfileSendReqButton.setText("Arkada??l??k iste??i g??nder");
                                mProfileSendReqButton.setEnabled(true);
                                mProfileMessage.setVisibility(View.INVISIBLE);
                                mProfileMessage.setEnabled(false);
                                Toast.makeText(NewProfileActivity.this, "Arkada??l??ktan ????kar??ld??...", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                mProfileSendReqButton.setEnabled(true);
                                Toast.makeText(NewProfileActivity.this, "Arkada??l??ktan ????kar??lam??yor...", Toast.LENGTH_SHORT).show();

                            }
                        }

                    });


                }

            }
        });


        //-----DECLING THE FRIEND REQUEST-----
        mProfileDeclineReqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map valueMap=new HashMap();
                valueMap.put("friend_request/"+mFirebaseUser.getUid()+"/"+user_id,null);
                valueMap.put("friend_request/"+user_id+"/"+mFirebaseUser.getUid(),null);

                mRootReference.updateChildren(valueMap, new DatabaseReference.CompletionListener() {

                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){

                            mCurrent_state = "not_friends";
                            mProfileSendReqButton.setText("Arkada??l??k iste??i g??nder");
                            mProfileSendReqButton.setEnabled(true);
                            Toast.makeText(NewProfileActivity.this, "Arkada??l??k iste??i reddedildi...", Toast.LENGTH_SHORT).show();
                            mProfileMessage.setVisibility(View.INVISIBLE);
                            mProfileMessage.setEnabled(false);
                            mProfileDeclineReqButton.setEnabled(false);
                            mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                        }
                        else{

                            mProfileSendReqButton.setEnabled(true);
                            Toast.makeText(NewProfileActivity.this, "Arkadaslik istegi cekilemez", Toast.LENGTH_SHORT).show();

                        }
                    }

                });


            }
        });

    }
}