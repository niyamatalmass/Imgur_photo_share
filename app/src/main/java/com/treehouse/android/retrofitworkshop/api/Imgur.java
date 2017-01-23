package com.treehouse.android.retrofitworkshop.api;

import com.treehouse.android.retrofitworkshop.model.Basic;
import com.treehouse.android.retrofitworkshop.model.Image;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface Imgur {
    String IMGUR_CLIENT_ID = "39831c43134fe6d";
    String IMGUR_BASE_URL = "https://api.imgur.com";
    String AUTHORIZATION_URL = "https://api.imgur.com/oauth2/authorize?client_id=" + IMGUR_CLIENT_ID + "&response_type=token";
    String REDIRECT_URL = "https://niyamatalmass.github.io:88";

    interface Auth {
        @Multipart
        @POST("3/upload")
        Call<Basic<Image>> uploadImage(@Part("image") RequestBody image);

        @GET("3/account/{username}/images/{page}")
        Call<Basic<ArrayList<Image>>> images(@Path("username") String username,
                                             @Path("page") int page);


    }

    interface Anon {
        @Multipart
        @POST("3/upload")
        Call<Basic<Image>> uploadImage(@Part("image") RequestBody image);
    }


}
