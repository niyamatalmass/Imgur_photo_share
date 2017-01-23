package com.treehouse.android.retrofitworkshop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.treehouse.android.retrofitworkshop.api.Imgur;
import com.treehouse.android.retrofitworkshop.api.OAuthUtil;
import com.treehouse.android.retrofitworkshop.api.Service;
import com.treehouse.android.retrofitworkshop.model.Basic;
import com.treehouse.android.retrofitworkshop.model.Image;
import com.treehouse.android.retrofitworkshop.view.ImageAdapter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btn_sign_in)
    View signInBtn;
    @BindView(R.id.btn_upload_anon)
    View uploadAnon;

    @BindView(R.id.account_images_container)
    View accountImagesContainer;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.btn_upload)
    View upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        signInBtn.setOnClickListener(this);
        uploadAnon.setOnClickListener(this);
        upload.setOnClickListener(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ImageAdapter(this));

        if (OAuthUtil.isAuthorized()) {
            toolbar.setTitle(OAuthUtil.get(OAuthUtil.ACCOUNT_USERNAME));
            showAccountImages();
        } else {
            toolbar.setTitle("Login");
            showLoginOrAnon();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(Imgur.REDIRECT_URL)) {
            Uri temp = Uri.parse("https://niyamatalmass.github.io?" + uri.getFragment().trim());

            OAuthUtil.set(OAuthUtil.ACCESS_TOKEN, temp.getQueryParameter(OAuthUtil.ACCESS_TOKEN));
            OAuthUtil.set(OAuthUtil.EXPIRES_IN, System.currentTimeMillis() + (Long.parseLong(temp.getQueryParameter(OAuthUtil.EXPIRES_IN)) * 1000));
            OAuthUtil.set(OAuthUtil.TOKEN_TYPE, temp.getQueryParameter(OAuthUtil.TOKEN_TYPE));
            OAuthUtil.set(OAuthUtil.REFRESH_TOKEN, temp.getQueryParameter(OAuthUtil.REFRESH_TOKEN));
            OAuthUtil.set(OAuthUtil.ACCOUNT_USERNAME, temp.getQueryParameter(OAuthUtil.ACCOUNT_USERNAME));

            if (OAuthUtil.isAuthorized()) {
                toolbar.setTitle(OAuthUtil.get(OAuthUtil.ACCOUNT_USERNAME));
                showAccountImages();
            } else {
                toolbar.setTitle("Login");
                showLoginOrAnon();
            }
        }
    }

    private void showLoginOrAnon() {
        accountImagesContainer.setVisibility(View.GONE);
        signInBtn.setVisibility(View.VISIBLE);
        uploadAnon.setVisibility(View.VISIBLE);
    }

    private void showAccountImages() {
        fetchAccountImages();

        accountImagesContainer.setVisibility(View.VISIBLE);
        signInBtn.setVisibility(View.GONE);
        uploadAnon.setVisibility(View.GONE);
    }

    private void fetchAccountImages() {

        Snackbar.make(upload, "Getting Images for account ", Snackbar.LENGTH_SHORT).show();

        Service.getAuthedApi()
                .images(OAuthUtil.get(OAuthUtil.ACCOUNT_USERNAME), 0)
                .enqueue(new Callback<Basic<ArrayList<Image>>>() {
                    @Override
                    public void onResponse(Call<Basic<ArrayList<Image>>> call, Response<Basic<ArrayList<Image>>> response) {
                        Log.d(TAG, response.code() + " response code from Main Activity");
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            ((ImageAdapter) recyclerView.getAdapter()).swap(response.body().data);
                        } else {
                            Snackbar.make(upload, "Failed :(", Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Basic<ArrayList<Image>>> call, Throwable t) {
                        Snackbar.make(upload, "Failed :(", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload_anon:
                uploadAnon();
                break;
            case R.id.btn_upload:
                upload();
                break;
            case R.id.btn_sign_in:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Imgur.AUTHORIZATION_URL)));
                break;
        }
    }

    private void uploadAnon() {
        Snackbar.make(upload, "Uploading Anon Image", Snackbar.LENGTH_SHORT).show();
        BufferedSource img;
        try {
            img = Okio.buffer(Okio.source(getAssets().open("sample_image.jpg")));
            byte[] image = img.readByteArray();

            Service.getAnonApi()
                    .uploadImage(
                            RequestBody.create(MediaType.parse("image/jpeg"),
                                    image)
                    ).enqueue(new Callback<Basic<Image>>() {
                @Override
                public void onResponse(Call<Basic<Image>> call, Response<Basic<Image>> response) {
                    Log.d(TAG, "From anon status: " + response.code());
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().data.link)));
                    } else {
                        Snackbar.make(upload, "Failed :(", Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Basic<Image>> call, Throwable t) {
                    Snackbar.make(upload, "Failed :(", Snackbar.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upload() {
        Snackbar.make(upload, "Uploading Image", Snackbar.LENGTH_SHORT).show();
        BufferedSource img;
        try {
            img = Okio.buffer(Okio.source(getAssets().open("sample_image.jpg")));
            byte[] image = img.readByteArray();

        Service.getAuthedApi()
                .uploadImage(
                        RequestBody.create(MediaType.parse("image/jpeg"),
                                image)
                ).enqueue(new Callback<Basic<Image>>() {
            @Override
            public void onResponse(Call<Basic<Image>> call, Response<Basic<Image>> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    fetchAccountImages();
                } else {
                    Snackbar.make(upload, "Failed :(", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Basic<Image>> call, Throwable t) {
                Snackbar.make(upload, "Failed :(", Snackbar.LENGTH_SHORT).show();
            }
        });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
