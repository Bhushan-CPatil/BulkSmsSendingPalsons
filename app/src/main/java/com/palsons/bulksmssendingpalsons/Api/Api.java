package com.palsons.bulksmssendingpalsons.Api;


import com.palsons.bulksmssendingpalsons.model.DefaultResponse;
import com.palsons.bulksmssendingpalsons.model.MainSmsResponse;
import com.palsons.bulksmssendingpalsons.model.TimeDelayResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {

    @FormUrlEncoded
    @POST("fetch_sms_from_server.php")
    Call<MainSmsResponse> fetchSms(
            @Field("DBPrefix") String DBPrefix
    );

    @GET("DateTimeAndDelay.php")
    Call<TimeDelayResponse> DateTimeAndDelay();

    @FormUrlEncoded
    @POST("update_sms_flag.php")
    Call<DefaultResponse> save_send_sms(
        @Field("curdate") String curdate,
        @Field("jsonarray") String jsonarray,
        @Field("DBPrefix") String DBPrefix
    );

    @FormUrlEncoded
    @POST("BulkSMSLoginCheck.php")
    Call<DefaultResponse> checkLogin(
        @Field("DBPrefix") String DBPrefix
    );
}
