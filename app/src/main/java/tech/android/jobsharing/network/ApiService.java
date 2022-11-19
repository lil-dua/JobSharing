package tech.android.jobsharing.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

/***
 * Created by HoangRyan aka LilDua on 11/15/2022.
 */
public interface ApiService {

    @POST("fcm/send")
    Call<String> sendMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String messageBody
    );
}
