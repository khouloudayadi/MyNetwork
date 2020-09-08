package com.example.mynetwork;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.mynetwork.Common.Common;
import com.example.mynetwork.Retrofit.INetworkAPI;
import com.example.mynetwork.Retrofit.RetrofitClient;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;


public class timeConnectiviteActivity extends AppCompatActivity implements OnMapReadyCallback,PermissionsListener, MapboxMap.OnMapClickListener {
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    LocationComponent locationComponent;
    LocationManager locationManager;

    Boolean img_speech_tag = false;

    //enable GPS
    boolean GpsStatus;

    //search place
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonPlaceLayerId = "geojsonPlaceLayerId";

    //get route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    //var api rest
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    INetworkAPI myNetworkAPI;
    double start_lat,start_lon,end_lat,end_lon,vitesse;

    android.app.AlertDialog dialog;
    AlertDialog alertDialog;
    private TextToSpeech TTS;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.cardView_Time)
    CardView cardView_time;
    @BindView(R.id.txt_distance_connectivite)
    TextView txt_distance_connectivite ;
    @BindView(R.id.txt_time_connectivite)
    TextView txt_time_connectivite ;
    @BindView(R.id.img_speech)
    ImageView img_speech;
    @BindView(R.id.img_navigation)
    ImageView img_navigation;


    String txt_distance,txt_temps;

    @OnClick(R.id.img_close)
    void close_interface(){
        cardView_time.setVisibility(View.GONE);
    }

    @OnClick(R.id.img_navigation)
    void NAV_interface(){

        startActivity(new Intent(this, MarkerFollowingRouteActivity.class));
        finish();
    }

    @OnClick(R.id.fab_my_location)
    void getMyLocation(){
        map.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_time_connectivite);

        initView();
        init();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        TTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    printOutSupportedLanguages();
                    TTS.setSpeechRate((float) 0.8);
                    //int result= TTS.setLanguage(new Locale("fr", "FR"));
                    int result= TTS.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA){
                        Log.e("TTS", "MISSING_DATA");
                    }
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                }
                else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        img_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!img_speech_tag) {
                    img_speech_tag = true;
                    //String Txt_speech = String.format("il vous reste %s et  %s kilometre pour entrer dans une zone non couverte",txt_temps,txt_distance);
                    String Txt_speech = String.format("il vous reste %s et %s kilometre",txt_temps,txt_distance);
                    Log.d("txt",Txt_speech);
                    img_speech.setImageResource(R.drawable.ic_speech_up_24);
                    TTS.speak(Txt_speech, TextToSpeech.QUEUE_FLUSH, null);
                }
                else{
                    img_speech_tag = false;
                    TTS.stop();
                    img_speech.setImageResource(R.drawable.ic_speech_off_24);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void printOutSupportedLanguages()  {
        // Supported Languages
        Set<Locale> supportedLanguages = TTS.getAvailableLanguages();
        if(supportedLanguages!= null) {
            for (Locale lang : supportedLanguages) {
                Log.e("TTS", "Supported Language: " + supportedLanguages.size() + lang);
            }
        }
        Log.e("tts", String.valueOf(Locale.getAvailableLocales()));

    }

    private void initView() {
        //init View
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.time);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void init() {
        myNetworkAPI = RetrofitClient.getInstance(Common.baseUrl).create(INetworkAPI.class);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        alertDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.msg_alert_gps)
                .setPositiveButton(R.string.ok, (dialog, i) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();


    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        this.map.setMinZoomPreference(9);
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);

                mapboxMap.addOnMapClickListener(timeConnectiviteActivity.this);

                initSearchFab();

                addPlaceIconSymbolLayer(style);

            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        dialog.show();

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        end_lat = point.getLatitude();
        end_lon = point.getLongitude();

        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());
        start_lat = locationComponent.getLastKnownLocation().getLatitude();
        start_lon = locationComponent.getLastKnownLocation().getLongitude();
        vitesse = locationComponent.getLastKnownLocation().getSpeed();

        GeoJsonSource source = map.getStyle().getSourceAs(geojsonPlaceLayerId);
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        //getRoute(originPoint, destinationPoint);
        getTimeConnectivite(start_lat,start_lon,end_lat,end_lon,vitesse);
        return true;
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        //check if gps is enabled or not and then request user to enable it
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(GpsStatus == true) {

            Log.d("gps", "GPS Is Enabled");
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                locationComponent = map.getLocationComponent();
                locationComponent.activateLocationComponent(this, style);
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.setRenderMode(RenderMode.GPS);
            }
            else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
        }
        else {
            alertDialog.show();
        }
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(timeConnectiviteActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if (map != null) {
                Style style = map.getStyle();
                if (style != null) {
                    GeoJsonSource place = style.getSourceAs(geojsonPlaceLayerId);
                    if (place != null) {
                        place.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }
                    dialog.show();
                    //get current location and search location
                    Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                            locationComponent.getLastKnownLocation().getLatitude());
                    start_lat = locationComponent.getLastKnownLocation().getLatitude();
                    start_lon = locationComponent.getLastKnownLocation().getLongitude();
                    vitesse = locationComponent.getLastKnownLocation().getSpeed();

                    Point destinationPoint = Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(),
                            ((Point) selectedCarmenFeature.geometry()).latitude());
                    end_lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                    end_lon = ((Point) selectedCarmenFeature.geometry()).longitude();

                    // Move map camera to the selected location
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(8)
                                    .build()));

                    getRoute(originPoint, destinationPoint);

                    getTimeConnectivite(start_lat,start_lon,end_lat,end_lon,vitesse);
                }
            }
        }
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        }
                        else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        }
                        else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, map, R.style.NavigationMapRoute);
                        }

                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    private void getTimeConnectivite(double start_lat, double start_lon, double end_lat, double end_lon, double vitesse) {
        Log.d("start_lat",String.valueOf(start_lat));
        Log.d("start_lon",String.valueOf(start_lon));
        Log.d("end_lat",String.valueOf(end_lat));
        Log.d("end_lon",String.valueOf(end_lon));
        Log.d("speed",String.valueOf(vitesse));

        JsonObject predicTime = new JsonObject();
        predicTime.addProperty("start_lat",start_lat);
        predicTime.addProperty("start_lon",start_lon);
        predicTime.addProperty("end_lat",end_lat);
        predicTime.addProperty("end_lon",end_lon);
        predicTime.addProperty("vitesse",vitesse);


        if(compositeDisposable != null){
            compositeDisposable.clear();
            cardView_time.setVisibility(View.GONE);
        }

        compositeDisposable.add(myNetworkAPI.getPredictTime(predicTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(predict -> {
                            if(predict.isSuccess()){
                                cardView_time.setVisibility(View.VISIBLE);

                                double dist = Double.parseDouble(predict.getDistance());
                               /* BigDecimal bd = BigDecimal.valueOf(dist / 1000.0);
                               bd = bd.setScale(0, RoundingMode.HALF_UP);
                               txt_distance_connectivite.setText(new StringBuilder(String.valueOf(bd.intValue())).append(" Km"));*/
                                txt_distance_connectivite.setText(String.format(" %s km", new DecimalFormat("#.##").format(dist / 1000)));
                                txt_distance = String.valueOf(new DecimalFormat("#.##").format(dist / 1000));//KM

                                double time_sec = Double.parseDouble(predict.getResult());
                                long time_min = TimeUnit.SECONDS.toMinutes((long) time_sec);

                                if(time_sec < 60){
                                    txt_time_connectivite.setText(new StringBuilder(String.valueOf((long) time_sec)).append(" sec"));
                                    txt_temps= String.valueOf(new StringBuilder(String.valueOf((long) time_sec)).append(" seconde"));
                                }
                                else{
                                    txt_time_connectivite.setText(new StringBuilder(String.valueOf(time_min)).append(" min"));
                                    txt_temps= String.valueOf(new StringBuilder(String.valueOf(time_min)).append(" minute"));
                                }
                            }
                            else{
                                dialog.dismiss();
                                Toast.makeText(timeConnectiviteActivity.this,predict.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        },
                        throwable -> {
                            dialog.dismiss();
                            Toast.makeText(timeConnectiviteActivity.this,"[Get Temps de Connectivité]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                )
        );

    }

    private void addPlaceIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("symbolIconId", BitmapFactory.decodeResource(
                timeConnectiviteActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));

        loadedMapStyle.addSource(new GeoJsonSource(geojsonPlaceLayerId));

        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonPlaceLayerId).withProperties(
                iconImage("symbolIconId"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        TTS.stop();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        img_speech_tag=false;
        TTS.shutdown();
        compositeDisposable.clear();
        mapView.onDestroy();
    }

}





