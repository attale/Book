package com.jx.retrofitlearn;


import com.jx.retrofitlearn.converter.gson.GsonConverterFactory;
import com.jx.retrofitlearn.converter.string.StringConverterFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by jiang.xu on 2015/11/7.
 */
public class ServiceGenerator {
    public static final String API_BASE_URL = "http://192.168.0.106/";
    public static final String API_BASE_URL2 = "http://192.168.6.134/";
    public static OkHttpClient sOkHttpClient = new OkHttpClient();
    private static Retrofit.Builder mBuilder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit.Builder mStringBuilder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL2)
            .addConverterFactory(StringConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = mBuilder.client(sOkHttpClient).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService2(Class<S> serviceClass) {
        Retrofit retrofit = mStringBuilder.client(sOkHttpClient).build();
        return retrofit.create(serviceClass);
    }


}
