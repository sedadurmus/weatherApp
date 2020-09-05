package com.sedadurmus.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sedadurmus.weatherapp.Retrofit.IOpenWeatherMap;
import com.sedadurmus.weatherapp.adapter.WeatherForecastAdapter;
import com.sedadurmus.weatherapp.common.Common;
import com.sedadurmus.weatherapp.model.Users;
import com.sedadurmus.weatherapp.model.WeatherForecastResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    public static FirebaseUser mevcutKullanici;
    String mail, sifre;
    Button logout;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;
    RecyclerView recycler_forecast;

    ImageView img_weather;
    LinearLayout weather_panel;
    ProgressBar loading;
    TextView wdata, txt_city, txt_humidity, txt_city_name,txt_sunrise, txt_sunset, txt_pressure, txt_temperature, txt_description, txt_wind, txt_date;

    SearchView cityName;
    Switch switchCompat;
    SharedPreferences sharedPreferences;
    GoogleMap map;
    Location currentLocation;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    private static final int REQUEST_CODE = 101;
    private static final String MyPREFERENCES = "nightModePrefs";
    private static final String KEY_ISNIGHTMODE = "isNightMode";




    static MainActivity instance;
    public static MainActivity getInstance(){
        if (instance == null)
            instance= new MainActivity();
        return instance;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }


    class Weather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... address) {

            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                int data = isr.read();
                String content = "";
                char ch;
                while (data != -1) {
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
                return content;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.logoutBtn);
//        wdata = findViewById(R.id.weeklyData);
        cityName = findViewById(R.id.cityName);
        mAuth = FirebaseAuth.getInstance();
        mevcutKullanici = mAuth.getCurrentUser();
        switchCompat = findViewById(R.id.switchCompat);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


//        compositeDisposable = new CompositeDisposable();
//        Retrofit retrofit = Retrofitclient.getInstance();
//        mService = retrofit.create(IOpenWeatherMap.class);

        img_weather= findViewById(R.id.img_weather);
        txt_date= findViewById(R.id.txt_date);
        txt_city_name = findViewById(R.id.txt_city_name);
        txt_description= findViewById(R.id.txt_description);
        txt_temperature= findViewById(R.id.txt_temperature);
        txt_wind = findViewById(R.id.txt_wind);

        recycler_forecast = findViewById(R.id.recycler_forecast);
        recycler_forecast.setHasFixedSize(true);
        recycler_forecast.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLastLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }


        fetchLastLocation();
        checkNightModeActivated();

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveNightModeState(true);
                    recreate();

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveNightModeState(false);
                    recreate();

                }
            }
        });

        cityName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = cityName.getQuery().toString();
                List<Address> addressList = null;
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng).title(location));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }

                return false;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onQueryTextChange(String s) {
                String content;
                Weather weather = new Weather();
//  "http://api.openweathermap.org/data/2.5/weather?q=" +
//                            cName +"&units=metric&appid=f2ba2780a30904b917fc29a7957e24ba"

                String cName = s;
                try {
                    content = weather.execute(  "http://api.openweathermap.org/data/2.5/weather?q=" +
                            cName +"&units=metric&appid=f2ba2780a30904b917fc29a7957e24ba").get();
                    Log.i("content", content);

                    JSONObject jsonObject = new JSONObject(content);
                    String weatherData = jsonObject.getString("weather");
                    String mainTemperature = jsonObject.getString("main");
                    JSONArray array = new JSONArray(weatherData);
                    String main = "";
                    String description = "";
                    String temperature = "";
//                    String wind = "";

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject weatherPart = array.getJSONObject(i);
                        main = weatherPart.getString("main");
                        description = weatherPart.getString("description");
//                        wind = weatherPart.getString("wind");
                    }
                    JSONObject mainPart = new JSONObject(mainTemperature);
                    temperature = mainPart.getString("temp");
                    Log.i("temperature", temperature);

//                    wdata.setText( temperature + " °C" + "\n" + main + "\n" + description + "\n" );

                    txt_city_name.setText(main + " ");
                    txt_description.setText(description + " ");
                    txt_temperature.setText(temperature + " °C" );
//                    txt_wind.setText(wind + " ")
                    getForecastWeatherInformation(cityName.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        supportMapFragment.getMapAsync(this);
        if (mevcutKullanici != null) {

            final DatabaseReference yolGiris = FirebaseDatabase.getInstance().getReference().child("Kullanıcılar")
                    .child(mAuth.getCurrentUser().getUid());
            yolGiris.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    LoginActivity.users = dataSnapshot.getValue(Users.class);
                    if (LoginActivity.users == null) return;
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Lütfen giriş yapın veya kaydolun", Toast.LENGTH_SHORT).show();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Çıkış Yap")
                        .setMessage("Çıkış yapmak istediğinize emin misiniz?")
                        .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(logoutIntent);
                                finish();
                            }
                        }).setNegativeButton("Hayır", null).show();
            }
        });

    }

    private void getForecastWeatherInformation(String cityName) {
        compositeDisposable.add(mService.getWeatherByCityName(cityName,
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                        displayForecastWeather(weatherForecastResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d("ERROR", ""+ throwable.getMessage());

                    }
                })

        );
    }

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {
        txt_city.setText(new StringBuilder(weatherForecastResult.city.name));
        WeatherForecastAdapter weatherForecastAdapter = new WeatherForecastAdapter(getApplicationContext(), weatherForecastResult);
        recycler_forecast.setAdapter(weatherForecastAdapter);
    }


    private void saveNightModeState(boolean nightMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(KEY_ISNIGHTMODE, nightMode);
    editor.apply();
    }

    private void checkNightModeActivated(){
        if (sharedPreferences.getBoolean(KEY_ISNIGHTMODE, false)){
            switchCompat.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            switchCompat.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    private void fetchLastLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
//                    currentLocation = location;
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
//                                map = googleMap;
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("buradayım");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                            googleMap.addMarker(markerOptions);
                        }
                    });

                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fetchLastLocation();
            }
        }
    }
}