package com.example.mynetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.DataBase.cellDataBase;
import com.example.mynetwork.DataBase.localCellDataSource;
import com.example.mynetwork.Model.Cell;
import com.example.mynetwork.Model.mapCoverageModel;
import com.example.mynetwork.Retrofit.INetworkAPI;
import com.example.mynetwork.Retrofit.RetrofitClient;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;


public class mapCoverageActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {
    List<mapCoverageModel> cellsCoverage = new ArrayList<>();
    double distance_to_cell=0;
    URL url = null;
    ConnectivityManager connectivityManager;
    //refresh
    private Handler handler;
    private Runnable runnable;

    static private double lat;
    static private double lon;

    boolean GpsStatus;
    private LocationManager locationManager;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    INetworkAPI myNetworkAPI;

    private PermissionsManager permissionsManager;
    private MapboxMap map;
    private MapView mapView;
    private LocationEngine locationEngine;
    LocationComponentActivationOptions locationComponentActivationOptions;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private mapCoverageActivityActivityLocationCallback callback =
            new mapCoverageActivityActivityLocationCallback(this);

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.layout_sans_conx) LinearLayout layout_sans_conx;
    @BindView(R.id.card_legend) CardView card_legend;

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_map_coverage);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        init();
        initView();
        checkConnexion();

        //mapview
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    private void checkConnexion() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null ) {
            progressBar.setVisibility(View.GONE);
            card_legend.setVisibility(View.GONE);
            layout_sans_conx.setVisibility(View.VISIBLE);
        }
        else {
            try {
                url = new URL("http://clients3.google.com/generate_204");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestProperty("User-Agent", "android");
                httpUrlConnection.setRequestProperty("Connection", "close");
                httpUrlConnection.setConnectTimeout(1500); // Timeout is in seconds
                httpUrlConnection.connect();
                if (httpUrlConnection.getResponseCode() == 204 && httpUrlConnection.getContentLength() == 0) {
                    layout_sans_conx.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    card_legend.setVisibility(View.GONE);
                    layout_sans_conx.setVisibility(View.VISIBLE);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
                checkConnexion();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    private void init() {
        myNetworkAPI = RetrofitClient.getInstance(Common.baseUrl).create(INetworkAPI.class);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
    }

    private void initView() {
        //init View
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.carte_couverture);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater search_food = getMenuInflater();
        search_food.inflate(R.menu.menu_search,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        MenuItem menuItemLocation = menu.findItem(R.id.action_myLocation);

        SearchManager searchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //EVENT
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                progressBar.setVisibility(View.VISIBLE);
                startSearchPlace(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        });

        menuItemLocation.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(mapCoverageActivity.this,"hhh",Toast.LENGTH_LONG).show();
                map.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
                return true;
            }
        });
        return true;
    }

    private void startSearchPlace(String query_name_place) {
        if (TextUtils.isEmpty(query_name_place)) {
            Toast.makeText(mapCoverageActivity.this,R.string.error_adresse,Toast.LENGTH_SHORT).show();
        }
        else{
            getAddressFromLocation(query_name_place, getApplicationContext());
        }

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        this.map.setMinZoomPreference(14);
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                progressBar.setVisibility(View.VISIBLE);
                GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if(GpsStatus == true) {
                    enableLocationComponent(style);
                    getcarteCouverture();
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(mapCoverageActivity.this,R.string.msg_alert_gps,Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void getcarteCouverture(){
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(compositeDisposable != null){
            compositeDisposable.clear();
        }
        if(activeNetworkInfo != null ) {
            try {
                url = new URL("http://clients3.google.com/generate_204");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestProperty("User-Agent", "android");
                httpUrlConnection.setRequestProperty("Connection", "close");
                httpUrlConnection.setConnectTimeout(1500); // Timeout is in seconds
                httpUrlConnection.connect();
                if (httpUrlConnection.getResponseCode() == 204 && httpUrlConnection.getContentLength() == 0) {
                    Location LocationUser = new Location("LocationUser");
                    LocationUser.setLatitude(lat);
                    LocationUser.setLongitude(lon);
                    Location cellLocation = new Location("cellLocation");
                    compositeDisposable.add(myNetworkAPI.getCell()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(cellModel -> {
                                        if (cellModel.isSuccess()) {
                                            Log.d("sizeCell1", String.valueOf(cellModel.getResult().size()));
                                            for (Cell cell : cellModel.getResult()) {
                                                cellLocation.setLatitude(cell.getLat());
                                                cellLocation.setLongitude(cell.getLon());
                                                distance_to_cell = Math.round(LocationUser.distanceTo(cellLocation));
                                                mapCoverageModel mapCoveragItem = new mapCoverageModel(cell.getRadio(), cell.getCid(), cell.getArea(), cell.getRange(), cell.getLat(), cell.getLon(), distance_to_cell);
                                                cellsCoverage.add(mapCoveragItem);
                                            }
                                            Log.d("sizeCell2", String.valueOf(cellsCoverage.size()));
                                            Collections.sort(cellsCoverage);
                                            //System.out.println(cellsCoverage.toString());
                                            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                                    new CameraPosition.Builder()
                                                            .target(new LatLng(lat, lon))
                                                            .zoom(14)
                                                            .build()));

                                            // Create an Icon object for the marker to use
                                            IconFactory iconFactory = IconFactory.getInstance(mapCoverageActivity.this);
                                            Icon iconGSM = iconFactory.fromResource(R.drawable.icongsm);
                                            Icon iconUMTS = iconFactory.fromResource(R.drawable.iconumts);
                                            Icon iconLTE = iconFactory.fromResource(R.drawable.iconlte);
                                            int i = 0;
                                            while (i < 15577) {
                                                System.out.println(cellsCoverage.get(i).toString());
                                                if (cellsCoverage.get(i).getRadio().equals("GSM")) {
                                                    map.addMarker(new MarkerOptions()
                                                            .position(new LatLng(cellsCoverage.get(i).getLat(), cellsCoverage.get(i).getLon()))
                                                            .title("Radio : GSM \n"+ getString(R.string.range) +cellsCoverage.get(i).getRange() + " mètre")
                                                            .icon(iconGSM)
                                                            .snippet("2G"));
                                                } else if (cellsCoverage.get(i).getRadio().equals("UMTS")) {
                                                    map.addMarker(new MarkerOptions()
                                                            .position(new LatLng(cellsCoverage.get(i).getLat(), cellsCoverage.get(i).getLon()))
                                                            .title("Radio : UMTS \n"+ getString(R.string.range)+cellsCoverage.get(i).getRange() + " mètre")
                                                            .icon(iconUMTS)
                                                            .snippet("3G"));
                                                } else {
                                                    map.addMarker(new MarkerOptions()
                                                            .position(new LatLng(cellsCoverage.get(i).getLat(), cellsCoverage.get(i).getLon()))
                                                            .title("Radio : LTE \n"+ getString(R.string.range) +cellsCoverage.get(i).getRange() + " mètre")
                                                            .icon(iconLTE)
                                                            .snippet("4G"));
                                                }
                                                i++;
                                            }
                                            card_legend.setVisibility(View.VISIBLE);
                                        } else {
                                            Log.d("getCellDB", cellModel.getMessage());
                                        }
                                        progressBar.setVisibility(View.GONE);
                                    },
                                    throwable -> {
                                        progressBar.setVisibility(View.GONE);
                                        Log.d("getCellDB", throwable.getMessage());
                                        Toast.makeText(mapCoverageActivity.this, R.string.errorServer, Toast.LENGTH_LONG).show();
                                    }
                            )
                    );
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
                // Get an instance of the component
                LocationComponent locationComponent = map.getLocationComponent();
                // Set the LocationComponent activation options
                locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, style)
                        .useDefaultLocationEngine(false)
                        .build();
                // Activate with the LocationComponentActivationOptions object
                locationComponent.activateLocationComponent(locationComponentActivationOptions);
                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);
                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);
                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);
                initLocationEngine();
            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

    private static class mapCoverageActivityActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<mapCoverageActivity> activityWeakReference;

        mapCoverageActivityActivityLocationCallback(mapCoverageActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            mapCoverageActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }
                lat = result.getLastLocation().getLatitude();
                lon = result.getLastLocation().getLongitude();
                if (activity.map != null && result.getLastLocation() != null) {
                    activity.map.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            mapCoverageActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getAddressFromLocation(final String locationAddress, final Context context) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List addressList = geocoder.getFromLocationName(locationAddress, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = (Address) addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        lat=address.getLatitude();
                        lon=address.getLongitude();
                        getcarteCouverture();
                    }
                } catch (IOException e) {
                    Log.e("error_geocode", "Unable to connect to Geocoder", e);
                }
            }
        };
        thread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
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
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}