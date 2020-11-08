package com.example.mynetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.DataBase.cellItem;
import com.example.mynetwork.Model.Cell;
import com.example.mynetwork.Model.mapCoverageModel;
import com.example.mynetwork.Retrofit.INetworkAPI;
import com.example.mynetwork.Retrofit.RetrofitClient;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;


public class mapCoverageActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private MapboxMap map;
    private MapView mapView;
    List<mapCoverageModel> cellsCoverage = new ArrayList<>();
    double distance_to_cell=0;
    URL url = null;
    ConnectivityManager connectivityManager;
    //refresh
    private Handler handler;
    private Runnable runnable;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    INetworkAPI myNetworkAPI;

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

    private void initView() {
          //init View
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.carte_couverture);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myNetworkAPI = RetrofitClient.getInstance(Common.baseUrl).create(INetworkAPI.class);
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

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        this.map.setMinZoomPreference(14);
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                //Log.d("ya rabi", String.valueOf(locationComponent.getLastKnownLocation().getLatitude()));
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            //progressBar.setVisibility(View.VISIBLE);
            locationComponent = map.getLocationComponent();
            locationComponent.activateLocationComponent(this, style);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
        else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

    }

    private void startSearchPlace(String query_name_place) {
        Toast.makeText(mapCoverageActivity.this,"[search place]"+query_name_place,Toast.LENGTH_SHORT).show();
        getcarteCouverture(locationComponent.getLastKnownLocation().getLatitude(),locationComponent.getLastKnownLocation().getLongitude());
    }

    private void getcarteCouverture(double lat,double lon){
        //Log.d("lat2", String.valueOf(locationComponent.getLastKnownLocation().getLatitude()));
        if(compositeDisposable != null){
            compositeDisposable.clear();
        }
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
                                    if(cellsCoverage.get(i).getRadio().equals("GSM")) {
                                        map.addMarker(new MarkerOptions()
                                                .position(new LatLng(cellsCoverage.get(i).getLat(), cellsCoverage.get(i).getLon()))
                                                .title(cellsCoverage.get(i).getRadio())
                                                .icon(iconGSM));
                                    }
                                    else if(cellsCoverage.get(i).getRadio().equals("UMTS")) {
                                        map.addMarker(new MarkerOptions()
                                                .position(new LatLng(cellsCoverage.get(i).getLat(), cellsCoverage.get(i).getLon()))
                                                .title(cellsCoverage.get(i).getRadio())
                                                .icon(iconUMTS));
                                    }
                                    else {
                                        map.addMarker(new MarkerOptions()
                                                .position(new LatLng(cellsCoverage.get(i).getLat(), cellsCoverage.get(i).getLon()))
                                                .title(cellsCoverage.get(i).getRadio())
                                                .icon(iconLTE));
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
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}