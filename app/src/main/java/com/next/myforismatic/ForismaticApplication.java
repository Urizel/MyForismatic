package com.next.myforismatic;

import android.app.Application;

import com.next.myforismatic.api.ForismaticService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Next on 07.04.2016.
 */
public class ForismaticApplication extends Application {

    private ForismaticService service;

    @Override
    public void onCreate() {
        super.onCreate();
        initRetrofit();
    }

    private void initRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.forismatic.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        service = retrofit.create(ForismaticService.class);
    }

    public ForismaticService getForismaticService() {
        return service;
    }

}