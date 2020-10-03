package com.example.mynetwork;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.kalert.KAlertDialog;
import com.example.mynetwork.Adapter.usesAdapter;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.DataBase.cellDataBase;
import com.example.mynetwork.DataBase.cellDataSource;
import com.example.mynetwork.DataBase.cellItem;
import com.example.mynetwork.DataBase.localCellDataSource;
import com.example.mynetwork.Model.BaseStation;
import com.example.mynetwork.Model.uses;
import com.example.mynetwork.Retrofit.INetworkAPI;
import com.example.mynetwork.Retrofit.RetrofitClient;
import com.example.mynetwork.Utile.NotificationHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.karumi.dexter.BuildConfig;
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
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okio.Utf8;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener  {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private PhoneStateListener ConnectionStateListener;

    //room
    cellDataSource cellDataSource;

    //Tag
    Boolean img_signal_tag = false;
    Boolean img_speech_tag = false;

    // Alert
    android.app.AlertDialog dialog;
    KAlertDialog alert_no_conn;
    KAlertDialog alert_wifi;
    AlertDialog alert_gps;


    String carrierName;

    private List<CellInfo> cellInfoList = null;

    //refresh
    private Handler handler;
    private Runnable runnable;
    TextView txt_name_operateur;

    //mapBox
    MapboxMap map;
    PermissionsManager permissionsManager;
    LocationComponent locationComponent;
    LocationManager locationManager;
    //search place
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonPlaceLayerId = "geojsonPlaceLayerId";

    //get route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    //Text to speech
    TextToSpeech TTS;
    String txt_distance,txt_temps;

    //enable GPS
    boolean GpsStatus;

    //var api rest
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    INetworkAPI myNetworkAPI;

    double start_lat,start_lon,end_lat,end_lon,vitesse;
    String network,datetime;

    Toolbar toolbar;
    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.img_type_network) ImageView img_type_network;
    @BindView(R.id.txt_nom_operateur) TextView txt_nom_operateur;
    @BindView(R.id.txt_sub_type_network) TextView txt_sub_type_network;
    @BindView(R.id.img_drop_down) ImageView img_drop_down;
    @BindView(R.id.txt_descr_signal) TextView txt_descr_signal;
    @BindView(R.id.txt_signal) TextView txt_signal;
    @BindView(R.id.layout_signal) LinearLayout layout_signal;
    @BindView(R.id.cardView_Time) CardView cardView_time;
    @BindView(R.id.txt_distance_connectivite) TextView txt_distance_connectivite ;
    @BindView(R.id.txt_time_connectivite) TextView txt_time_connectivite ;
    @BindView(R.id.img_speech) ImageView img_speech;
    @BindView(R.id.img_navigation) ImageView img_navigation;
    @BindView(R.id.coordinator_layout_time) CoordinatorLayout coordinator_layout_time;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @OnClick(R.id.fab_my_location)
    void getMyLocation(){
      /* map.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });*/
        //addBts();
        searchBestBts();
        //readData();
    }
    /*
    @OnClick(R.id.img_close)
    void close_interface(){
        cardView_time.setVisibility(View.GONE);
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        }
        compositeDisposable.clear();
    }
*/
    @OnClick(R.id.img_drop_down)
    void drop_down_signal(){
        if(!img_signal_tag) {
            img_signal_tag = true;
            txt_descr_signal.setVisibility(View.VISIBLE);
            img_drop_down.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
        }
        else{
            img_signal_tag = false;
            txt_descr_signal.setVisibility(View.GONE);
            img_drop_down.setImageResource(R.drawable.ic_arrow_drop_down_24);
        }
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle(getString(R.string.network));
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        init();
        initView();
        checknetwork();

        //mapview
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        txt_name_operateur = (TextView)headerView.findViewById(R.id.txt_nom_operateur);
        txt_name_operateur.setText(carrierName);

        //Txt to Speech
        TTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //printOutSupportedLanguages();
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

    private void initView() {
        ButterKnife.bind(this);
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        alert_no_conn = new KAlertDialog(this);
        alert_wifi = new KAlertDialog(this);

        alert_gps = new AlertDialog.Builder(this)
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

    @RequiresApi(api = JELLY_BEAN_MR1)
    private void init() {
        myNetworkAPI = RetrofitClient.getInstance(Common.baseUrl).create(INetworkAPI.class);

        telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        carrierName = telephonyManager.getNetworkOperatorName();

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        cellDataSource = new localCellDataSource(cellDataBase.getInstance(this).cellDAO());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_network) {
            network();
        }
         /*else if (id == R.id.nav_temps_connectivite) {
            startActivity(new Intent(HomeActivity.this,timeConnectiviteActivity.class) );
        } */
        else if (id == R.id.nav_test_debit) {
            startActivity(new Intent(HomeActivity.this,testDebitActivity.class) );
        } else if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.nav_apropos) {
            apropos();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        this.map.setMinZoomPreference(10);
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);

                mapboxMap.addOnMapClickListener(HomeActivity.this);

                 initSearchFab();

                addPlaceIconSymbolLayer(style);

                LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);

                try {
                    localizationPlugin.matchMapLanguageWithDeviceDefault();
                } catch (RuntimeException exception) {
                    Log.d(TAG, exception.toString());
                }

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
        datetime = String.valueOf(locationComponent.getLastKnownLocation().getTime());
        /*Date date = new Date(locationComponent.getLastKnownLocation().getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        datetime = dateFormat.format(date);*/

        GeoJsonSource source = map.getStyle().getSourceAs(geojsonPlaceLayerId);
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        getTimeConnectivite(datetime,start_lat,start_lon,end_lat,end_lon,vitesse);
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
            alert_gps.show();
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
                        .build(HomeActivity.this);
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
                    datetime = String.valueOf(locationComponent.getLastKnownLocation().getTime());

                    Point destinationPoint = Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(),
                            ((Point) selectedCarmenFeature.geometry()).latitude());
                    end_lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                    end_lon = ((Point) selectedCarmenFeature.geometry()).longitude();

                    // Move map camera to the selected location
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(10)
                                    .build()));

                    getRoute(originPoint, destinationPoint);
                    getTimeConnectivite(datetime,start_lat,start_lon,end_lat,end_lon,vitesse);
                }
            }
        }
    }

    private void getTimeConnectivite(String datetime,double start_lat, double start_lon, double end_lat, double end_lon, double vitesse) {
        Log.d("start_lat",String.valueOf(start_lat));
        Log.d("start_lon",String.valueOf(start_lon));
        Log.d("end_lat",String.valueOf(end_lat));
        Log.d("end_lon",String.valueOf(end_lon));
        Log.d("speed",String.valueOf(vitesse));

        JsonObject predicTime = new JsonObject();
        predicTime.addProperty("datetime",datetime);
        predicTime.addProperty("start_lat",start_lat);
        predicTime.addProperty("start_lon",start_lon);
        predicTime.addProperty("end_lat",end_lat);
        predicTime.addProperty("end_lon",end_lon);
        predicTime.addProperty("vitesse",vitesse);
        predicTime.addProperty("network",network);


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
                                Toast.makeText(HomeActivity.this,predict.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        },
                        throwable -> {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this,"Faild to connect with server"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                )
        );

    }

    private void addPlaceIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("symbolIconId", BitmapFactory.decodeResource(
                HomeActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));

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

    @RequiresApi(api = JELLY_BEAN_MR1)
    @SuppressLint("MissingPermission")
    private void checknetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if(activeNetworkInfo == null ){
            Common.cpt_wifi=0;
            alert_wifi.dismiss();

            Picasso.get().load(R.drawable.ic_no_internet).into(img_type_network);
            txt_nom_operateur.setText(carrierName);
            txt_sub_type_network.setText(R.string.non_connecte);
            layout_signal.setVisibility(View.GONE);
            coordinator_layout_time.setVisibility(View.GONE);

            if(!alert_no_conn.isShowing()) {
                Common.cpt_no_internet +=1;
                if (Common.cpt_no_internet < 2){
                    alert_no_conn.setContentText(getResources().getString(R.string.check_connection));
                    alert_no_conn.setContentTextSize(20);
                    alert_no_conn.setConfirmText("OK");
                    alert_no_conn.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            alert_no_conn.dismiss();
                        }
                    });
                    alert_no_conn.show();
                }
            }
        }
        else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            if(alert_wifi.isShowing()){
                alert_wifi.dismiss();
            }
            if(alert_no_conn.isShowing()){
                alert_no_conn.dismiss();
            }
            Common.cpt_wifi=0;
            Common.cpt_no_internet=0;
            displayTypeNetwork();
            getSignal();
            coordinator_layout_time.setVisibility(View.VISIBLE);
        }
        else {
            Common.cpt_no_internet = 0;
            alert_no_conn.dismiss();

            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            Picasso.get().load(R.drawable.ic_network_wifi).into(img_type_network);
            txt_nom_operateur.setText(ssid.substring(1,ssid.length()-1));
            txt_sub_type_network.setText(R.string.connecetd_wifi);
            layout_signal.setVisibility(View.GONE);
            coordinator_layout_time.setVisibility(View.GONE);

            if(!alert_wifi.isShowing()){
                Common.cpt_wifi +=1;
                if(Common.cpt_wifi < 2){
                    alert_wifi.setTitleText(getResources().getString(R.string.alert_wifi_title));
                    alert_wifi.setContentText(getResources().getString(R.string.alert_wifi_text));
                    alert_wifi.setConfirmText("OK");
                    alert_wifi.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            alert_wifi.dismiss();
                        }
                    });
                    alert_wifi.show();
                }
            }
        }
        refresh(1000);
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    private void displayTypeNetwork() {
        txt_nom_operateur.setText(carrierName);
        ConnectionStateListener = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                super.onDataConnectionStateChanged(state, networkType);
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        Picasso.get().load(R.drawable.ic_network_disable_signal).into(img_type_network);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " GPRS");
                        network = "2G";
                        //displayListeQuality("GPRS");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " EDGE");
                        network = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " CDMA");
                        network = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " UMTS");
                        network = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA: //3G+ h
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:// est un complément de HSDPA
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " HSPA");
                        network = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA://HSPA+   h+   3g++
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " HSPA+");
                        network = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        Picasso.get().load(R.drawable.lte).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " LTE");
                        network = "4G";
                        break;
                    default:
                        Picasso.get().load(R.drawable.rx_5g).into(img_type_network);
                        network = "5G";
                        break;
                }
            }

        };
        telephonyManager.listen(ConnectionStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @SuppressLint("MissingPermission")
    private void getSignal() {
        layout_signal.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            Log.d("bs","The base station list is null");
        }
        else if (cellInfoList.size() == 0) {
            Log.d("bs","The base station list is empty");
        }
        else{
            BaseStation main_BS = bindData(cellInfoList.get(0));
            Log.i("cell",main_BS.toString());
            if(main_BS.getType() == "WCDMA" || main_BS.getType() == "GSM" ){
                if(main_BS.getRssi() >= -70){
                    layout_signal.setBackgroundResource(R.color.signal_excellent);
                    txt_signal.setText(R.string.signal_strength_excellent);
                    txt_descr_signal.setText(R.string.desc_signal_excellent);
                }
                else if((main_BS.getRssi() >= -85) && (main_BS.getRssi() < -70)){
                    layout_signal.setBackgroundResource(R.color.signal_good);
                    txt_signal.setText(R.string.signal_strength_good);
                    txt_descr_signal.setText(R.string.desc_signal_good);
                }
                else if((main_BS.getRssi() >= -100) && (main_BS.getRssi() <= -86)){
                    layout_signal.setBackgroundResource(R.color.signal_fair);
                    txt_signal.setText(R.string.signal_strength_fair);
                    txt_descr_signal.setText(R.string.desc_signal_fair);
                }
                else if(main_BS.getRssi() < -100){
                    layout_signal.setBackgroundResource(R.color.signal_Poor);
                    txt_signal.setText(R.string.signal_strength_poor);
                    txt_descr_signal.setText(R.string.desc_signal_poor);
                }
                else if(main_BS.getRssi() == -110){
                    layout_signal.setBackgroundResource(R.color.no_signal);
                    txt_signal.setText(R.string.no_signal);
                    txt_descr_signal.setText(R.string.no_signal);
                }
            }
            else if(main_BS.getType() == "LTE"){

                if(main_BS.getRssi() > -65){
                    layout_signal.setBackgroundResource(R.color.signal_excellent);
                    txt_signal.setText(R.string.signal_strength_excellent);
                    txt_descr_signal.setText(R.string.desc_signal_excellent);
                }
                else if((main_BS.getRssi() > -75) && (main_BS.getRssi() <= -65)){
                    layout_signal.setBackgroundResource(R.color.signal_good);
                    txt_signal.setText(R.string.signal_strength_good);
                    txt_descr_signal.setText(R.string.desc_signal_good);
                }
                else if((main_BS.getRssi() > -85) && (main_BS.getRssi() <= -75)){
                    layout_signal.setBackgroundResource(R.color.signal_fair);
                    txt_signal.setText(R.string.signal_strength_fair);
                    txt_descr_signal.setText(R.string.desc_signal_fair);
                }
                else if((main_BS.getRssi() <= -85)&&(main_BS.getRssi() > -95) ){
                    layout_signal.setBackgroundResource(R.color.signal_Poor);
                    txt_signal.setText(R.string.signal_strength_poor);
                    txt_descr_signal.setText(R.string.desc_signal_poor);
                }
                else if(main_BS.getRssi() <= -95){
                    layout_signal.setBackgroundResource(R.color.no_signal);
                    txt_signal.setText(R.string.no_signal);
                    txt_descr_signal.setText(R.string.no_signal);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private BaseStation bindData(CellInfo cellInfo) {
        BaseStation baseStation = new BaseStation();

        // La station de base a différents types de signaux: 2G, 3G, 4G
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (cellInfo instanceof CellInfoWcdma) {
                //3G
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                baseStation = new BaseStation();
                if (cellInfoWcdma.getCellSignalStrength() != null) {
                    baseStation.setType("WCDMA");
                    baseStation.setRssi(cellInfoWcdma.getCellSignalStrength().getDbm()); //Get the signal strength as dBm
                }
            }
            else if (cellInfo instanceof CellInfoLte) {
                //4G
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                if (cellInfoLte.getCellSignalStrength() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        baseStation.setType("LTE");
                        baseStation.setRssi(cellInfoLte.getCellSignalStrength().getRssi());
                    }
                }
            }
            else if (cellInfo instanceof CellInfoGsm) {
                //2G
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                if (cellInfoGsm.getCellSignalStrength() != null) {
                    baseStation.setType("GSM");
                    baseStation.setRssi(cellInfoGsm.getCellSignalStrength().getDbm());
                }
            }
            else {
                Log.e("TAG", "CDMA CellInfo................................................");
            }
        }
        return baseStation;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendNotification(){
        NotificationHelper notificationHelper = new NotificationHelper(HomeActivity.this);
        notificationHelper.notify(1, false, "My title", "My content" );
    }


    public void refresh(int milliseconds){
        handler = new Handler();
        runnable = new Runnable() {
            @RequiresApi(api = JELLY_BEAN_MR1)
            @Override
            public void run() {
                checknetwork();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Restaurant");
            String shareMessage = "\n My Restaurant \n Est une application mobile conçues pour faciliter\n" + "la commande de plats dans divers restaurants..." ;
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            e.toString();
        }
    }

    private void apropos(){}

    private void network(){}

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        handler.removeCallbacks(runnable);
        telephonyManager.listen(ConnectionStateListener, PhoneStateListener.LISTEN_NONE);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        TTS.stop();
        super.onPause();
    }
    public void  searchBestBts(){
       compositeDisposable.add(cellDataSource.getAllCell(1234)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(cellItems -> {
                   if(cellItems.isEmpty()){
                       Log.i("cellEmpty","cell Empty");
                   }
                   else{
                       Log.i("cellEmpty",cellItems.get(0).getRadio());
                   }

                       },throwable -> { Log.i("cell",throwable.getMessage());})
       );
   }
    public void  addBts(){
       cellItem cellItem = new cellItem();
       cellItem.setRadio("GSM");
       cellItem.setCid("1");
       cellItem.setArea(1);
       cellItem.setMcc(1);
       cellItem.setMnc(1);
       cellItem.setLat(1.1);
       cellItem.setLon(1.1);
       cellItem.setRange(1000);
       compositeDisposable.add(cellDataSource.insertorReplaceAll(cellItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( ()-> {
                    Toast.makeText(this,"Cell Added ",Toast.LENGTH_SHORT).show();
                },
                    throwable -> { Log.i("cell",throwable.getMessage());
                })
        );
    }
    public void readData(){
        List<cellItem> cells = new ArrayList<>();
        cellItem cellItem = new cellItem();
        InputStream is = getResources().openRawResource(R.raw.dataset);
        BufferedReader reader= new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line="";
        try {
            while((line = reader.readLine()) != null){
                Log.d("line",line);
                //split by ","
                String[] tokens = line.split(",");
                //read row
                cellItem.setRadio(tokens[0]);
                cellItem.setCid(tokens[4]);
                cellItem.setArea(Integer.parseInt(tokens[3]));
                cellItem.setMcc(Integer.parseInt(tokens[1]));
                cellItem.setMnc(Integer.parseInt(tokens[2]));
                cellItem.setLat(Double.parseDouble(tokens[6]));
                cellItem.setLon(Double.parseDouble(tokens[5]));
                cellItem.setRange(Double.parseDouble(tokens[7]));

                cells.add(cellItem);

                Log.d("Cell Tower:",cellItem.toString());
            }
        } catch (IOException e) {
            Log.wtf("datset","error reading data file in line"+ line, e);
            e.printStackTrace();
        }

    }

}

