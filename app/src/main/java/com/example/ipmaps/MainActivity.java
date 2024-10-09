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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

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

        // Fragment do mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Criando retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ip-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geoLocationAPI = retrofit.create(GeoLocationAPI.class);

        // Ligando a função ao botão de busca
        imageButtonSearch = findViewById(R.id.imageButtonSearch);
        editTextIP = findViewById(R.id.editTextIP);

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
    }


    // Fazendo a requisição da API para buscar a localização por IP
    private void procuraIP(String ip) {
        textViewCountry = findViewById(R.id.textViewCountry);
        textViewRegion = findViewById(R.id.textViewRegion);
        textViewCity = findViewById(R.id.textViewCity);
        textViewISP = findViewById(R.id.textViewISP);
        Call<RespostaGeo> call = geoLocationAPI.getLocation(ip);

        call.enqueue(new Callback<RespostaGeo>() {
            @Override
            public void onResponse(Call<RespostaGeo> call, Response<RespostaGeo> response) {
                if (response.isSuccessful()) {
                    RespostaGeo location = response.body();
                    if (location != null && location.getCountry() != null && location.getIsp() != null) {
                        double latitude = location.getLat();
                        double longitude = location.getLon();

                        textViewCountry.setText(location.getCountry());
                        textViewRegion.setText(location.getRegionName());
                        textViewCity.setText(location.getCity());
                        textViewISP.setText(location.getIsp());

                        LatLng local = new LatLng(latitude,longitude);
                        mMap.addMarker(new MarkerOptions().position(local).title("Localização IP: " + ip));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 10));
                    } else {
                        Toast.makeText(MainActivity.this, "A resposta da API está vazia", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Resposta não foi bem-sucedida", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespostaGeo> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erro ao buscar IP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //mapa
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng santaCruz = new LatLng(-29.6890566,-52.4558563);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(santaCruz));
    }

    private void fecharTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(editTextIP.getWindowToken(), 0);
        }
    }

    public void testarConeccao(){
        textViewConection = findViewById(R.id.textViewConection);
        imageViewConectado = findViewById(R.id.imageViewConectado);
        imageViewDesconectado = findViewById(R.id.imageViewDesconectado);
        //se conectado

        imageViewConectado.setVisibility(View.VISIBLE);


        //se nao conectado

        //se dados moveis
    }
}