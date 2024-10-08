package com.example.ipmaps;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GeoLocationAPI {
    @GET("json/{ip}")
    Call<RespostaGeo> getLocation(@Path("ip") String ip);
}

