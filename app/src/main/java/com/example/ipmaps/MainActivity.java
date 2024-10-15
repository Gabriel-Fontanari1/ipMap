package com.example.ipmaps;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //atributos
    EditText editTextIP;
    GoogleMap mMap;
    ImageButton imageButtonSearch;
    GeoLocationAPI geoLocationAPI;
    TextView textViewCountry;
    TextView textViewRegion;
    TextView textViewCity;
    TextView textViewISP;
    TextView textViewConection;
    ImageView imageViewDadMoveis;
    ImageView imageViewConectado;
    ImageView imageViewDesconectado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //fragment do mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //cria o Retrofit para acessar a API de geolocalização
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ip-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geoLocationAPI = retrofit.create(GeoLocationAPI.class);

        imageButtonSearch = findViewById(R.id.imageButtonSearch);
        editTextIP = findViewById(R.id.editTextIP);

        //listener para detectar o envio no teclado (quando o usr clica em "ok")
        editTextIP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    imageButtonSearch.performClick();
                    return true;
                }
                return false;
            }
        });

        imageButtonSearch.setOnClickListener(v -> {
            String ip = editTextIP.getText().toString();
            procuraIP(ip);
            fecharTeclado();
        });

        //verificar a conexão inicial
        testarConexao();

        //monitoramento da mudança de rede
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull android.net.Network network) {
                    runOnUiThread(() -> testarConexao()); //atualiza a ui qunado a conexão é formada
                }

                @Override
                public void onLost(@NonNull android.net.Network network) {
                    runOnUiThread(() -> testarConexao());//atualiza quando a conexão for perdida
                }

                @Override
                public void onCapabilitiesChanged(@NonNull android.net.Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    runOnUiThread(() -> testarConexao()); //atualiza se o tipo de rede mudar
                }
            });
        }
    }


    //função para fazer a requisição pra a API e buscar a localização pelo ip
    private void procuraIP(String ip) {
        textViewCountry = findViewById(R.id.textViewCountry);
        textViewRegion = findViewById(R.id.textViewRegion);
        textViewCity = findViewById(R.id.textViewCity);
        textViewISP = findViewById(R.id.textViewISP);
        Call<RespostaGeo> call = geoLocationAPI.getLocation(ip);

        //faz o request pra pra buscar a localização do ip
        call.enqueue(new Callback<RespostaGeo>() {
            @Override
            public void onResponse(Call<RespostaGeo> call, Response<RespostaGeo> response) {
                if (response.isSuccessful()) {
                    RespostaGeo location = response.body();
                    //identifica se o ip é valido, se ele não tiver algum dado como country vinculado, significa que não é valido
                    if (location != null && location.getCountry() != null && location.getIsp() != null) {
                        double latitude = location.getLat();
                        double longitude = location.getLon();
                        //atualiza os campos com os dados do ip
                        textViewCountry.setText(location.getCountry());
                        textViewRegion.setText(location.getRegionName());
                        textViewCity.setText(location.getCity());
                        textViewISP.setText(location.getIsp());

                        //define a posição no mapa, puxando a latitude e longitude
                        LatLng local = new LatLng(latitude,longitude);
                        mMap.addMarker(new MarkerOptions().position(local).title("Localização IP: " + ip));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 10));
                    } else {
                        Toast.makeText(MainActivity.this, "Digite um ip válido!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Resposta não foi bem sucedida", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespostaGeo> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Verifique a sua conexão à internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //mapa
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //localização inicial do mapa, que no caso é Santa Cruz
        LatLng santaCruz = new LatLng(-29.6890566,-52.4558563);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(santaCruz));
        testarConexao();
    }

    //função pra fechar o teclado
    private void fecharTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(editTextIP.getWindowToken(), 0);
        }
    }

    public void testarConexao() {
        textViewConection = findViewById(R.id.textViewConection);
        imageViewConectado = findViewById(R.id.imageViewConectado);
        imageViewDesconectado = findViewById(R.id.imageViewDesconectado);
        imageViewDadMoveis = findViewById(R.id.imageViewDadMoveis);

        //verifica o tipo de conexão
        int connectionStatus = getConnectionType(this);

        //vai atualizar o layout conforme o tipo de conexão
        if (connectionStatus == 1) {
            //conectado ao wifi
            textViewConection.setText("Conectado à Internet (Wi-Fi)");
            imageViewConectado.setVisibility(View.VISIBLE);
            imageViewDesconectado.setVisibility(View.INVISIBLE);
            imageViewDadMoveis.setVisibility(View.INVISIBLE);
        } else if (connectionStatus == 2) {
            //conectado aos dados móveis
            textViewConection.setText("Conectado aos dados móveis");
            imageViewConectado.setVisibility(View.INVISIBLE);
            imageViewDesconectado.setVisibility(View.INVISIBLE);
            imageViewDadMoveis.setVisibility(View.VISIBLE);
        } else {
            //sem conexão a internet
            textViewConection.setText("Sem conexão à Internet!");
            imageViewConectado.setVisibility(View.INVISIBLE);
            imageViewDesconectado.setVisibility(View.VISIBLE);
            imageViewDadMoveis.setVisibility(View.INVISIBLE);
        }
    }

    //verificar o tipo de conexão
    private int getConnectionType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return 1; //se for wifi, vai retornar 1
                        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return 2;//se for dados móveis vai retornar 2
                        }
                    }
                }
            } else {
                android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                //verificação para as apis mais antigas do android
                if (activeNetwork != null) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        return 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        return 2;
                    }
                }
            }
        }
        return 0;
    }
}