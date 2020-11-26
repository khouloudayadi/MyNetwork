package com.example.mynetwork;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.daimajia.swipe.SwipeLayout;
import com.developer.kalert.KAlertDialog;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.DataBase.cellDataBase;
import com.example.mynetwork.DataBase.cellDataSource;
import com.example.mynetwork.DataBase.cellItem;
import com.example.mynetwork.DataBase.localCellDataSource;
import com.example.mynetwork.Retrofit.INetworkAPI;
import com.example.mynetwork.Retrofit.RetrofitClient;
import com.example.mynetwork.TestDebit.GetSpeedTestHostsHandler;
import com.example.mynetwork.TestDebit.HttpDownloadTest;
import com.example.mynetwork.TestDebit.HttpUploadTest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.karumi.dexter.BuildConfig;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.O;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static java.lang.Thread.sleep;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private PhoneStateListener ConnectionStateListener;
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    final DecimalFormat dec = new DecimalFormat("#.##");
    //List
    private List<CellInfo> cellInfoList = null;
    //Tag
    private Boolean img_signal_tag = false;
    private Boolean img_speech_tag = false;
    // Alert
    private android.app.AlertDialog dialog;
    private KAlertDialog alert_no_conn;
    private KAlertDialog alert_wifi;
    private AlertDialog alert_gps;

    //mapBox
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private LocationManager locationManager;    //search place
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    Marker markerConnx=null;
    Marker markerSConnx=null;
    Icon icon;
    //get route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    //refresh
    private Handler handler;
    private Runnable runnable;
    private TextView txt_name_operateur;
    //Text to speech
    private TextToSpeech TTS;
    private String txt_distance, txt_temps;
    //enable GPS
    boolean GpsStatus;
    //var api rest
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private INetworkAPI myNetworkAPI;
    //Variable
    private int rssi,rsrp,rsrq;
    private double start_lat, start_lon, end_lat, end_lon, vitesse;
    private String network, timeStamp, carrierName;
    private int cid, mcc, mnc, area, range;
    private String radio;
    double lat_cell, lon_cell;
    boolean error;
    int cptOnMapClick = 0;
    private boolean listen=false;

    //room
    private cellDataSource cellDataSource;

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.img_type_network) ImageView img_type_network;
    @BindView(R.id.txt_nom_operateur) TextView txt_nom_operateur;
    @BindView(R.id.txt_sub_type_network) TextView txt_sub_type_network;
    @BindView(R.id.img_drop_down) ImageView img_drop_down;
    @BindView(R.id.txt_descr_signal) TextView txt_descr_signal;
    @BindView(R.id.txt_signal) TextView txt_signal;
    @BindView(R.id.layout_signal) LinearLayout layout_signal;
    @BindView(R.id.cardView_Time) SwipeLayout cardView_time;
    @BindView(R.id.txt_distance_connectivite) TextView txt_distance_connectivite;
    @BindView(R.id.txt_time_connectivite) TextView txt_time_connectivite;
    @BindView(R.id.img_speech) ImageView img_speech;
    @BindView(R.id.fab_location_search) FloatingActionButton fab_location_search;
    @BindView(R.id.coordinator_layout_time) CoordinatorLayout coordinator_layout_time;
    @BindView(R.id.layout_absence_connx) LinearLayout layout_absence_connx;
    @BindView(R.id.txt_distance_to_cell) TextView txt_distance_to_cell;
    @BindView(R.id.fab_info_cell) FloatingActionButton fab_info_cell;
    @BindView(R.id.img_info_connx) ImageView img_info_connx;


    Toolbar toolbar;
    ImageView img_close_info_cell;
    TextView txt_radio,txt_mnc,txt_mcc,txt_area,txt_cid,txt_lat,txt_lon,txt_range,txt_volume_ul,txt_volume_dl,txtSpeedDl,txtSpeedUl;

    @RequiresApi(api = O)
    @OnClick(R.id.fab_my_location)
    void getMyLocation() {
        map.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });
    }

    @OnClick(R.id.img_drop_down)
    void drop_down_signal() {
        if (!img_signal_tag) {
            img_signal_tag = true;
            txt_descr_signal.setVisibility(View.VISIBLE);
            img_drop_down.setImageResource(R.drawable.ic_arrow_drop_up_24);
        } else {
            img_signal_tag = false;
            txt_descr_signal.setVisibility(View.GONE);
            img_drop_down.setImageResource(R.drawable.ic_arrow_drop_down_24);
        }
    }

    @OnClick(R.id.fab_location_search)
    void searchDestination() {
        searchLocation();
    }

    @OnClick(R.id.img_close_time)
    void clearTime(){
        if(markerConnx != null) {
            markerConnx.remove();
        }

        if(compositeDisposable != null){
            compositeDisposable.clear();
        }

        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        }

        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(start_lat, start_lon))
                        .zoom(8)
                        .build()));

        cardView_time.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_home);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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


        //mapview
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        LayoutInflater inflater = getLayoutInflater();
        View view_info_conn = inflater.inflate(R.layout.layout_volume_data, null);
        txt_volume_ul=(TextView) view_info_conn.findViewById(R.id.txtVolumeUl);
        txt_volume_dl=(TextView) view_info_conn.findViewById(R.id.txtVolumeDl);
        txtSpeedUl=(TextView) view_info_conn.findViewById(R.id.txtSpeedUl);
        txtSpeedDl=(TextView) view_info_conn.findViewById(R.id.txtSpeedDl);

        AlertDialog.Builder alert_info_connx = new AlertDialog.Builder(HomeActivity.this);
        alert_info_connx.setView(view_info_conn);
        alert_info_connx.setCancelable(true);
        AlertDialog dialog_info_connx = alert_info_connx.create();
        img_info_connx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_info_connx.show();
            }
        });



        View view_info_cell = inflater.inflate(R.layout.layout_info_cell, null);
        txt_radio=(TextView) view_info_cell.findViewById(R.id.txt_radio);
        txt_mcc=(TextView) view_info_cell.findViewById(R.id.txt_mcc);
        txt_mnc=(TextView) view_info_cell.findViewById(R.id.txt_mnc);
        txt_area=(TextView) view_info_cell.findViewById(R.id.txt_area);
        txt_cid=(TextView) view_info_cell.findViewById(R.id.txt_cid);
        txt_lat=(TextView) view_info_cell.findViewById(R.id.txt_lat);
        txt_lon=(TextView) view_info_cell.findViewById(R.id.txt_lon);
        txt_range=(TextView) view_info_cell.findViewById(R.id.txt_range);
        img_close_info_cell = (ImageView) view_info_cell.findViewById(R.id.img_close_cell);
        AlertDialog.Builder alert_info_cell = new AlertDialog.Builder(HomeActivity.this);
        alert_info_cell.setView(view_info_cell);
        alert_info_cell.setCancelable(false);
        AlertDialog dialog_info_cell = alert_info_cell.create();

        fab_info_cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_info_cell.show();
            }
        });

        img_close_info_cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_info_cell.dismiss();
            }
        });

        txt_name_operateur = (TextView) headerView.findViewById(R.id.txt_nom_operateur);
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
                    int result = TTS.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.e("TTS", "MISSING_DATA");
                    }
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        img_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!img_speech_tag) {
                    img_speech_tag = true;
                    //String Txt_speech = String.format("il vous reste %s et  %s kilometre pour entrer dans une zone non couverte",txt_temps,txt_distance);
                    String Txt_speech = String.format(getResources().getString(R.string.tts), txt_temps, txt_distance);
                    Log.d("txt", Txt_speech);
                    img_speech.setImageResource(R.drawable.ic_speech_up_24);
                    TTS.speak(Txt_speech, TextToSpeech.QUEUE_FLUSH, null);
                } else {
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
        cellDataSource = new localCellDataSource(cellDataBase.getInstance(this).cellDAO());

        telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        carrierName = telephonyManager.getNetworkOperatorName();

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
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
        else if (id == R.id.nav_test_debit) {
            startActivity(new Intent(HomeActivity.this,testDebitActivity.class) );
        }
        else if (id == R.id.nav_share) {
            share();
        }
        else if (id == R.id.nav_apropos) {
            startActivity(new Intent(HomeActivity.this,aboutUsActivity.class) );
        }
        else if (id == R.id.nav_carte_couverture) {
            startActivity(new Intent(HomeActivity.this,mapCoverageActivity.class) );
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mon Réseau");
            String shareMessage = "\n Mon Réseau \nEst une application mobile permettant de déterminer le temps de disponibilité de la connexion réseau pendant un trajet ainsi que la qualité de la bande passante" ;
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            e.toString();
        }
    }

    private void network(){}

    @RequiresApi(api = JELLY_BEAN_MR1)
    @SuppressLint("MissingPermission")
    private void checkNetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null ){
            alert_wifi.dismiss();
            Common.cpt_wifi=0;
            layout_signal.setVisibility(View.GONE);
            coordinator_layout_time.setVisibility(View.GONE);
            layout_absence_connx.setVisibility(View.GONE);
            Picasso.get().load(R.drawable.ic_no_internet).into(img_type_network);
            txt_nom_operateur.setText(carrierName);
            txt_sub_type_network.setText(R.string.non_connecte);
            if(!alert_no_conn.isShowing()) {
                Common.cpt_no_conn +=1;
                if (Common.cpt_no_conn == 1){
                    alert_no_conn.setContentText(getResources().getString(R.string.check_connection));
                    alert_no_conn.setContentTextSize(18);
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
           //searchBestCell();

        }
        else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Common.cpt_wifi=0;
            Common.cpt_no_conn=0;
            if(alert_wifi.isShowing()){
                alert_wifi.dismiss();
            }
            if(alert_no_conn.isShowing()){
                alert_no_conn.dismiss();
            }
            txt_nom_operateur.setText(carrierName);
            coordinator_layout_time.setVisibility(View.VISIBLE);
            txt_sub_type_network.setVisibility(View.VISIBLE);
            displayTypeNetwork();
        }
        else {
            alert_no_conn.dismiss();
            Common.cpt_no_conn = 0;

            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            Picasso.get().load(R.drawable.ic_network_wifi).into(img_type_network);
            txt_nom_operateur.setText(ssid.substring(1,ssid.length()-1));
            txt_sub_type_network.setText(R.string.connecetd_wifi);
            layout_signal.setVisibility(View.GONE);
            layout_absence_connx.setVisibility(View.GONE);
            coordinator_layout_time.setVisibility(View.GONE);
            if(!alert_wifi.isShowing()){
                Common.cpt_wifi +=1;
                if(Common.cpt_wifi == 1){
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

    public void refresh(int milliseconds){
        handler = new Handler();
        runnable = new Runnable() {
            @RequiresApi(api = JELLY_BEAN_MR1)
            @Override
            public void run() {
                checkNetwork();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        this.map.setMinZoomPreference(11);
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                map.addOnMapClickListener(HomeActivity.this::onMapClick);
                LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
                try {
                    localizationPlugin.matchMapLanguageWithDeviceDefault();
                } catch (RuntimeException exception) {
                    Log.d(TAG, exception.toString());
                }

            }
        });
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

    private void searchLocation() {
        @SuppressLint("Range") Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder().backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(15)
                        .build(PlaceOptions.MODE_CARDS))
                .build(HomeActivity.this);
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
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
                    dialog.show();
                    Point destinationPoint = Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(),
                            ((Point) selectedCarmenFeature.geometry()).latitude());
                    end_lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                    end_lon = ((Point) selectedCarmenFeature.geometry()).longitude();

                    Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                            locationComponent.getLastKnownLocation().getLatitude());

                    start_lat = locationComponent.getLastKnownLocation().getLatitude();
                    start_lon = locationComponent.getLastKnownLocation().getLongitude();
                    vitesse = locationComponent.getLastKnownLocation().getSpeed();
                    timeStamp = String.valueOf(locationComponent.getLastKnownLocation().getTime());

                    reverseGeocode(Point.fromLngLat(end_lon,end_lat));
                    getRoute(originPoint, destinationPoint);
                    getTimeConnectivite(timeStamp,start_lat,start_lon,end_lat,end_lon,vitesse);
                    //getTimeConnectivite("1604439592000",35.8629398,10.6000928,36.249384637790186,10.424755221131107,vitesse);

                }
            }
        }
    }
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if(cptOnMapClick == 1){
            dialog.show();
            Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            end_lat = point.getLatitude();
            end_lon = point.getLongitude();

            Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                    locationComponent.getLastKnownLocation().getLatitude());
            start_lat = locationComponent.getLastKnownLocation().getLatitude();
            start_lon = locationComponent.getLastKnownLocation().getLongitude();
            vitesse = locationComponent.getLastKnownLocation().getSpeed();
            timeStamp = String.valueOf(locationComponent.getLastKnownLocation().getTime());
            reverseGeocode(Point.fromLngLat(end_lon, end_lat));
            getRoute(originPoint, destinationPoint);
            getTimeConnectivite(timeStamp,start_lat,start_lon,end_lat,end_lon,vitesse);
            return true;
        }
        return false;
    }

    public void reverseGeocode (final Point point){
        try {
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.access_token))
                    .query(Point.fromLngLat(point.longitude(),point.latitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                    .build();
            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            CarmenFeature feature = results.get(0);
                            // Move map camera to the selected location
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder()
                                            .target(new LatLng(point.latitude(), point.longitude()))
                                            .zoom(8)
                                            .build()));

                            // Create an Icon object for the marker to use
                            IconFactory iconFactory = IconFactory.getInstance(HomeActivity.this);
                            Icon icon = iconFactory.fromResource(R.drawable.map_marker_dark);

                            // Add the marker to the map
                            if(markerConnx != null) {
                                markerConnx.remove();
                            }

                            markerConnx = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(point.latitude(), point.longitude()))
                                    .title(feature.placeName())
                                    .icon(icon));
                        }
                    }
                }
                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    //Timber.e("Geocoding Failure: %s", throwable.getMessage());
                    Log.d("address", throwable.getMessage());
                }
            });
        }
        catch (ServicesException servicesException) {
            Timber.e("Error geocoding: %s", servicesException.toString());
            servicesException.printStackTrace();
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

    private void getTimeConnectivite(String timeStamp, double start_lat, double start_lon, double end_lat, double end_lon, double vitesse) {
        Log.d("Time_lat1",String.valueOf(start_lat));
        Log.d("Time_lon1",String.valueOf(start_lon));
        Log.d("Time_lat2",String.valueOf(end_lat));
        Log.d("Time_lon2",String.valueOf(end_lon));
        Log.d("Time_speed",String.valueOf(vitesse));
        Log.d("Time_timeStamp",timeStamp);
        Log.d("Time_network",network);

        JsonObject predicTime = new JsonObject();
        predicTime.addProperty("start_lat",start_lat);
        predicTime.addProperty("start_lon",start_lon);
        predicTime.addProperty("end_lat",end_lat);
        predicTime.addProperty("end_lon",end_lon);
        predicTime.addProperty("vitesse",vitesse);
        predicTime.addProperty("timeStamp",timeStamp);
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
                                double time_sec = Double.parseDouble(predict.getResult());
                                testDebit(start_lat,start_lon,time_sec);
                                cardView_time.setVisibility(View.VISIBLE);

                                double dist = Double.parseDouble(predict.getDistance());
                                txt_distance_connectivite.setText(String.format("( %s km )", new DecimalFormat("#.##").format(dist / 1000)));
                                txt_distance = String.valueOf(new DecimalFormat("#.##").format(dist / 1000));//KM


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
                            //dialog.dismiss();
                        },
                        throwable -> {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this,R.string.errorServer,Toast.LENGTH_SHORT).show();
                        }
                )
        );

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
    private void displayTypeNetwork() {
        listen = true;
        ConnectionStateListener = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                super.onDataConnectionStateChanged(state, networkType);
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        searchBestCell();
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " GPRS");
                        network = "2G";
                        showView();
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " EDGE");
                        network = "2G";
                        showView();
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " CDMA");
                        network = "2G";
                        showView();
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " UMTS");
                        network = "3G";
                        showView();
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA: //3G+ h
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:// est un complément de HSDPA
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " HSPA");
                        network = "3G";
                        showView();
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA://HSPA+   h+   3g++
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " HSPA+");
                        network = "3G";
                        showView();
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        Picasso.get().load(R.drawable.lte).into(img_type_network);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " LTE");
                        network = "4G";
                        showView();
                        break;
                    default:
                        Picasso.get().load(R.drawable.rx_5g).into(img_type_network);
                        network = "5G";
                        showView();
                        break;
                }
            }
        };
        telephonyManager.listen(ConnectionStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    public void showView(){
        cptOnMapClick = 1;
        layout_signal.setVisibility(View.VISIBLE);
        fab_location_search.setVisibility(View.VISIBLE);
        layout_absence_connx.setVisibility(View.GONE);
        fab_info_cell.setVisibility(View.GONE);
        if(markerSConnx != null) {
            markerSConnx.remove();
        }
        infoCell();
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @SuppressLint("MissingPermission")
    private void infoCell() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            Log.d("infoCell","The base station list is null");
        }
        else if (cellInfoList.size() == 0) {
            Log.d("infoCell","The base station list is empty");
        }
        else{
            CellInfo cellInfo = cellInfoList.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (cellInfo instanceof CellInfoWcdma) {
                    //3G
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    radio = "UMTS";
                    cid = cellIdentityWcdma.getCid();
                    area = cellIdentityWcdma.getLac();
                    mcc = cellIdentityWcdma.getMcc();
                    mnc = cellIdentityWcdma.getMnc();

                    if (cellInfoWcdma.getCellSignalStrength() != null) {
                        rssi=cellInfoWcdma.getCellSignalStrength().getDbm();//Get the signal strength as dBm
                    }
                }
                else if (cellInfo instanceof CellInfoLte) {
                    //4G
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    radio="LTE" ;
                    cid=cellIdentityLte.getCi();
                    mnc=cellIdentityLte.getMnc();
                    mcc=cellIdentityLte.getMcc();
                    area=cellIdentityLte.getTac();
                    if (cellInfoLte.getCellSignalStrength() != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            rssi=cellInfoLte.getCellSignalStrength().getRssi();
                            rsrp=cellInfoLte.getCellSignalStrength().getRsrp();
                            rsrq=cellInfoLte.getCellSignalStrength().getRsrq();
                        }
                    }
                }
                else if (cellInfo instanceof CellInfoGsm) {
                    //2G
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();

                    radio="GSM";
                    cid=cellIdentityGsm.getCid();
                    area=cellIdentityGsm.getLac();
                    mcc= cellIdentityGsm.getMcc();
                    mnc=cellIdentityGsm.getMnc();
                    if (cellInfoGsm.getCellSignalStrength() != null) {
                        rssi=cellInfoGsm.getCellSignalStrength().getDbm();
                    }
                }
                else {
                    Log.e("TAG", "CDMA CellInfo");
                }
            }
            showSignal(rssi,radio,rsrp,rsrq);
            addCellTower(radio,mcc,mnc,cid,area);
        }
    }

    public void showSignal(int rssi,String radio,int rsrp,int rsrq){
        if(radio.equals("GSM") || radio.equals("UMTS")){
            if(rssi >= -70){
                layout_signal.setBackgroundResource(R.color.signal_excellent);
                txt_signal.setText(R.string.signal_strength_excellent);
                txt_descr_signal.setText(R.string.desc_signal_excellent);
            }
            else if((rssi >= -85) && (rssi < -70)){
                layout_signal.setBackgroundResource(R.color.signal_good);
                txt_signal.setText(R.string.signal_strength_good);
                txt_descr_signal.setText(R.string.desc_signal_good);
            }
            else if((rssi >= -100) && (rssi <= -86)){
                layout_signal.setBackgroundResource(R.color.signal_fair);
                txt_signal.setText(R.string.signal_strength_fair);
                txt_descr_signal.setText(R.string.desc_signal_fair);
            }
            else if(rssi < -100){
                layout_signal.setBackgroundResource(R.color.signal_Poor);
                txt_signal.setText(R.string.signal_strength_poor);
                txt_descr_signal.setText(R.string.desc_signal_poor);
            }
            else if(rssi == -110){
                layout_signal.setBackgroundResource(R.color.no_signal);
                txt_signal.setText(R.string.no_signal);
                txt_descr_signal.setText(R.string.no_signal);
            }
        }
        else if(radio.equals("LTE")){
            if((rssi > -65)||(rsrp >= -80) || (rsrq >= -10)){
                layout_signal.setBackgroundResource(R.color.signal_excellent);
                txt_signal.setText(R.string.signal_strength_excellent);
                txt_descr_signal.setText(R.string.desc_signal_excellent);
            }
            else if(((rssi > -75) && (rssi <= -65))||((rsrp < -80) && (rsrp > -90))||((rsrq < -10) && (rsrq > -15))){
                layout_signal.setBackgroundResource(R.color.signal_good);
                txt_signal.setText(R.string.signal_strength_good);
                txt_descr_signal.setText(R.string.desc_signal_good);
            }
            else if(((rssi > -85) && (rssi <= -75))||((rsrq <= -15) && (rsrq > -20))||((rsrp <= -90) && (rsrp > -100))){
                layout_signal.setBackgroundResource(R.color.signal_fair);
                txt_signal.setText(R.string.signal_strength_fair);
                txt_descr_signal.setText(R.string.desc_signal_fair);
            }
            else if((rssi <= -85)&&(rssi > -95) ){
                layout_signal.setBackgroundResource(R.color.signal_Poor);
                txt_signal.setText(R.string.signal_strength_poor);
                txt_descr_signal.setText(R.string.desc_signal_poor);
            }
            else if((rssi <= -95)||(rsrq <= -20)||(rsrp <= -100)){
                layout_signal.setBackgroundResource(R.color.no_signal);
                txt_signal.setText(R.string.no_signal);
                txt_descr_signal.setText(R.string.no_signal);
            }
        }
        else {
            Log.e("TAG", "CDMA CellInfo");
        }
    }

    @SuppressLint("LogNotTimber")
    private void addCellTower(String radio, int mcc, int mnc, int cid, int area) {
        Log.d("addCellTower", String.valueOf(cid));
        Log.d("addCellTower", String.valueOf(mcc));
        Log.d("addCellTower", String.valueOf(mnc));
        Log.d("addCellTower", String.valueOf(area));

        try {
            GetOpenCellID(mcc,mnc,cid,area);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!error) {
            Log.d("addCellTower", String.valueOf(lat_cell));
            Log.d("addCellTower", String.valueOf(lon_cell));
            Log.d("addCellTower", String.valueOf(range));

            JsonObject cellTower = new JsonObject();
            cellTower.addProperty("radio",radio);
            cellTower.addProperty("mcc",mcc);
            cellTower.addProperty("mnc",mnc);
            cellTower.addProperty("area",area);
            cellTower.addProperty("cid",cid);
            cellTower.addProperty("lon",lon_cell);
            cellTower.addProperty("lat",lat_cell);
            cellTower.addProperty("range",range);

            compositeDisposable.add(myNetworkAPI.addCell(cellTower)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(addCellModel -> {
                                Log.d("addCellTower",addCellModel.getMessage());
                            },
                            throwable -> {
                                Log.d("addCellTower",throwable.getMessage());
                            })
            );
        }
        else {
            Log.d("addCellTower", "err opencellid");
        }
    }

    public void GetOpenCellID(int mcc,int mnc,int cid,int area) throws IOException {
        String strURLSent =
                "http://www.opencellid.org/cell/get?key=ec89397cee46f6&mcc=" + mcc
                        + "&mnc=" + mnc
                        + "&cellid=" + cid
                        + "&lac=" + area
                        + "&format=json";

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet(strURLSent));
        String GetOpenCellID_fullresult = EntityUtils.toString(response.getEntity());
        if (GetOpenCellID_fullresult.equalsIgnoreCase("err")) {
            error = true;
        }
        else {
            String[] tResult = GetOpenCellID_fullresult.split(",");
            if(tResult[0].length() >= 25){
                error = true;
                Log.d("openCellID", "Cell not found");
            }
            else{
                error = false;
                lat_cell = Double.parseDouble(tResult[0].substring(7));
                lon_cell = Double.parseDouble(tResult[1].substring(6));
                range = Integer.parseInt(tResult[7].substring(8));
            }
        }
    }

    public void searchBestCell(){
        cptOnMapClick = 0;
        //pour tester pas +
        txt_nom_operateur.setText(carrierName);
        coordinator_layout_time.setVisibility(View.VISIBLE);
        txt_sub_type_network.setVisibility(View.VISIBLE);

        Picasso.get().load(R.drawable.ic_network_disable_signal).into(img_type_network);
        txt_sub_type_network.setText(R.string.non_connecte);
        layout_signal.setVisibility(View.GONE);
        layout_absence_connx.setVisibility(View.VISIBLE);
        fab_location_search.setVisibility(View.GONE);
        fab_info_cell.setVisibility(View.VISIBLE);

        if(compositeDisposable != null){
            compositeDisposable.clear();
            cardView_time.setVisibility(View.GONE);
        }

        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        }

        if(markerConnx != null){
            markerConnx.remove();
        }

        compositeDisposable.add(cellDataSource.getAllCell()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cellItems -> {
                    if(cellItems.isEmpty()){
                        Log.i("searchcell","cell Empty");
                    }
                    else{
                        double distance_to_cell=0;
                        double distance_min = 19349458;
                        cellItem bestCell = null;
                        Location myLocation = new Location("myLocation");
                        myLocation.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
                        myLocation.setLongitude(locationComponent.getLastKnownLocation().getLongitude());

                        Location cellLocation = new Location("cellLocation");

                        for (cellItem cell : cellItems){
                            cellLocation.setLatitude(cell.getLat());
                            cellLocation.setLongitude(cell.getLon());
                            //Distance between user and cell
                            distance_to_cell=Math.round(myLocation.distanceTo(cellLocation));
                            if(distance_to_cell < distance_min){
                                distance_min = distance_to_cell;
                                bestCell = cell;
                            }
                        }
                        Log.i("distance_min", String.valueOf(distance_min));
                        Log.i("bestCell", bestCell.toString());

                        txt_distance_to_cell.setText(String.valueOf(distance_min));

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(bestCell.getLat(), bestCell.getLon()),15));
                        // Create an Icon object for the marker to use
                        IconFactory iconFactory = IconFactory.getInstance(HomeActivity.this);
                        if(bestCell.getRadio().equals("GSM")) {
                            icon  = iconFactory.fromResource(R.drawable.location_2g);
                        }
                        else if(bestCell.getRadio().equals("UMTS")){
                            icon = iconFactory.fromResource(R.drawable.location_3g);
                        }
                        else{
                            icon = iconFactory.fromResource(R.drawable.location_4g);
                        }

                        if(markerSConnx != null) {
                            markerSConnx.remove();
                        }

                        markerSConnx = map.addMarker(new MarkerOptions()
                                .position(new LatLng(bestCell.getLat(), bestCell.getLon()))
                                .title(bestCell.getRadio())
                                .icon(icon));

                        txt_radio.setText(bestCell.getRadio());
                        txt_area.setText(String.valueOf(bestCell.getArea()));
                        txt_cid.setText(String.valueOf(bestCell.getCid()));
                        txt_mcc.setText(String.valueOf(bestCell.getMcc()));
                        txt_mnc.setText(String.valueOf(bestCell.getMnc()));
                        txt_lat.setText(String.valueOf(bestCell.getLat()));
                        txt_lon.setText(String.valueOf(bestCell.getLon()));
                        txt_range.setText(String.valueOf(bestCell.getRange())+ " mètre");
                    }
                },throwable -> { Log.i("searchcell",throwable.getMessage());})
        );
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        checkNetwork();
        mapView.onStart();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onPause() {
        super.onPause();
        TTS.stop();
        Common.cpt_no_conn=0;
        Common.cpt_wifi=0;
        compositeDisposable.clear();
        mapView.onPause();

    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
        handler.removeCallbacks(runnable);
        if(listen){
            telephonyManager.listen(ConnectionStateListener, PhoneStateListener.LISTEN_NONE);
        }
        Common.cpt_no_conn = 0;
        Common.cpt_wifi = 0;
        mapView.onStop();

    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.cpt_no_conn=0;
        Common.cpt_wifi=0;
        compositeDisposable.clear();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void testDebit(double lat_user,double lon_user, double timeConn) {

        Log.d("getDebitLat", String.valueOf(lat_user));
        Log.d("getDebitLon", String.valueOf(lon_user));
        Log.d("getDebitTime", String.valueOf(timeConn));
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.run();
        new Thread(new Runnable() {
            public void run(){

                //Get hosts
                int timeCount = 6; //1min
                while (!getSpeedTestHostsHandler.isFinished()) {
                    timeCount--;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    if (timeCount <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("getDebitHost","No Connection...");
                            }

                        });
                        getSpeedTestHostsHandler = null;
                        return;
                    }
                }

                //Trouver le serveur le plus proche
                HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                double tmp = 19349458; //plus grand distance entre serveur et user
                double dist = 0.0;
                int findServerIndex = 0;

                for (int index : mapKey.keySet()) {
                    Location source = new Location("Source");
                    source.setLatitude(lat_user);
                    source.setLongitude(lon_user);

                    List<String> ls = mapValue.get(index);
                    Location dest = new Location("Dest");
                    dest.setLatitude(Double.parseDouble(ls.get(0)));
                    dest.setLongitude(Double.parseDouble(ls.get(1)));

                    double distance = source.distanceTo(dest);
                    if (tmp > distance) {
                        tmp = distance;
                        dist = distance;
                        findServerIndex = index;
                    }
                }
                Log.d("getDebitfindServerIndex", String.valueOf(findServerIndex));
                String testAddr = mapKey.get(findServerIndex).replace("http://", "https://");
                final List<String> info = mapValue.get(findServerIndex);
                final double distance = dist;

                if (info == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("getDebitHost","There was a problem in getting Host Location. Try again later");
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("getDebitDist",String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                    }
                });

                //Reset value
                Boolean downloadTestStarted = false;
                Boolean downloadTestFinished = false;
                Boolean uploadTestStarted = false;
                Boolean uploadTestFinished = false;

                //Init Test
                final HttpDownloadTest downloadTest = new HttpDownloadTest(testAddr.replace(testAddr.split("/")[testAddr.split("/").length - 1], ""));
                final HttpUploadTest uploadTest = new HttpUploadTest(testAddr);


                //Tests
                while (true) {
                    if (!uploadTestStarted) {
                        uploadTest.run();
                        uploadTestStarted = true;
                    }
                    if (uploadTestFinished && !downloadTestStarted) {
                        downloadTest.run();
                        downloadTestStarted = true;
                    }

                    //Upload Test
                    if (uploadTestFinished) {
                        //Failure
                        if (uploadTest.getFinalUploadRate() == 0) {
                            Log.d("getDebitUL","Error Upload");
                            txt_volume_ul.setText("Error Upload");
                            txtSpeedUl.setText("Error Upload");
                        } else {
                            //Success
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_volume_ul.setText(dec.format((uploadTest.getFinalUploadRate() * timeConn)/1000.0)+" Gbits");
                                    txtSpeedUl.setText(dec.format(uploadTest.getFinalUploadRate())+" Mbps");
                                    Log.d("getDebitUL",dec.format(uploadTest.getFinalUploadRate()));//Mbps
                                    Log.d("gettimeUL",dec.format(timeConn));//seconde
                                    Log.d("getVolumeUL",dec.format(uploadTest.getFinalUploadRate() * timeConn));//Gbits
                                }
                            });
                        }
                    }

                    //Download Test
                    if (uploadTestFinished) {
                        if (downloadTestFinished) {
                            //Failure
                            if (downloadTest.getFinalDownloadRate() == 0) {
                                Log.d("getDebitDL","Error Download");
                                txt_volume_dl.setText("Error Download");
                                txtSpeedDl.setText("Error Download");
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txt_volume_dl.setText(dec.format((downloadTest.getFinalDownloadRate() * timeConn)/1000.0)+" Gbits");
                                        txtSpeedDl.setText(dec.format(downloadTest.getFinalDownloadRate())+" Mbps");
                                        Log.d("getDebitDL",dec.format(downloadTest.getFinalDownloadRate()));//Mbps
                                        Log.d("gettimeDL",dec.format(timeConn));//seconde
                                        Log.d("getVolumeDL",dec.format(downloadTest.getFinalDownloadRate() * timeConn));//Mbits
                                    }
                                });
                            }
                        }
                    }

                    //Test terminé
                    if (downloadTestFinished && uploadTest.isFinished()) {
                        dialog.dismiss();
                        break;
                    }

                    if (downloadTest.isFinished()) {
                        downloadTestFinished = true;
                    }
                    if (uploadTest.isFinished()) {
                        uploadTestFinished = true;
                    }
                    if (uploadTestStarted && !uploadTestFinished) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                    }
                    else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }
        }).start();
        Log.d("testDebit","finished");
    }

}










