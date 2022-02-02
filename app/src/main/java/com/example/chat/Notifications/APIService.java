package com.example.chat.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAb7rIm6I:APA91bFiPU7ldwST78p_cfUJ9nLTkqxMx5NQyS_dbTUX-y9M1T65IDjSslcz_HGZeMN6u38LK6ZduVVlz6AxuDvsKlwALWtKaqD0scSrFWL2Voy49q0dKcajESeUpR1meLPby7nvdrnF"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> SendNotification(@Body Sender body);
}
