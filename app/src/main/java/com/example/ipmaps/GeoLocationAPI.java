package com.example.ipmaps;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

//vai ser definido o contrato para chamadas da api usando o retrofit

//getlocation vai fazer uma requisisão httpget para a api de geolocalização
//passando o ip como parametro de busca no caminho da url

public interface GeoLocationAPI {
    @GET("json/{ip}")
    Call<RespostaGeo> getLocation(@Path("ip") String ip);
}

