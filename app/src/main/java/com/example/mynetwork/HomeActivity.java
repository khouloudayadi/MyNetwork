package com.example.mynetwork;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.kalert.KAlertDialog;
import com.example.mynetwork.Adapter.usesAdapter;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.Model.uses;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.BuildConfig;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TelephonyManager telephonyManager;


    private ConnectivityManager connectivityManager;
    private PhoneStateListener ConnectionStateListener;

    private List<uses> listeUses = new ArrayList<>();
    usesAdapter adapter;
    android.app.AlertDialog dialog;
    KAlertDialog alert_no_conn;
    KAlertDialog alert_wifi;


    String carrierName;

    private List<CellInfo> infos;

    //refresh
    private Handler handler;
    private Runnable runnable;
    TextView txt_name_operateur;

    @BindView(R.id.recycler_usage)
    RecyclerView recycler_usage;
    @BindView(R.id.img_type_network)
    ImageView img_type_network;
    @BindView(R.id.txt_nom_operateur)
    TextView txt_nom_operateur;
    @BindView(R.id.txt_sub_type_network)
    TextView txt_sub_type_network;
    @BindView(R.id.txt_couverture)
    TextView txt_couverture;
    @BindView(R.id.txt_title_interface)
    TextView txt_title_interface;

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);

        if(adapter != null){
            recycler_usage.setAdapter(null);
        }

        Common.cpt_wifi=0;
        Common.cpt_no_internet =0;
        //telephonyManager.listen(ConnectionStateListener, PhoneStateListener.LISTEN_NONE);
        super.onDestroy();
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
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

        txt_name_operateur = (TextView)headerView.findViewById(R.id.txt_nom_operateur);
        txt_name_operateur.setText(carrierName);

    }

    private void initView() {
        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);// init linear layout manager
        recycler_usage.setLayoutManager(layoutManager);//attach linear to recycleview
        recycler_usage.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        carrierName = telephonyManager.getNetworkOperatorName();
        alert_no_conn = new KAlertDialog(this);
        alert_wifi = new KAlertDialog(this);
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
        } else if (id == R.id.nav_temps_connectivite) {
            startActivity(new Intent(HomeActivity.this,timeConnectiviteActivity.class) );
        } else if (id == R.id.nav_test_debit) {
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

    @RequiresApi(api = JELLY_BEAN_MR1)
    @SuppressLint("MissingPermission")
    private void checknetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null ){
            Common.cpt_wifi=0;
            //Toast.makeText(getApplicationContext(), R.string.check_connection, Toast.LENGTH_LONG).show();
            Picasso.get().load(R.drawable.ic_no_internet).into(img_type_network);
            txt_couverture.setText(R.string.non_connecte);
            txt_nom_operateur.setText(carrierName);
            txt_title_interface.setVisibility(View.GONE);
            txt_sub_type_network.setVisibility(View.GONE);
            recycler_usage.setAdapter(null);

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
            Common.cpt_wifi=0;
            Common.cpt_no_internet=0;
            displayTypeNetwork();
        }
        else {
            Common.cpt_no_internet=0;
            //Toast.makeText(getApplicationContext(), R.string.alert_wifi_title, Toast.LENGTH_LONG).show();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            Picasso.get().load(R.drawable.ic_network_wifi).into(img_type_network);
            txt_nom_operateur.setText(ssid.substring(1,ssid.length()-1));
            txt_couverture.setText(R.string.connecetd_wifi);
            txt_title_interface.setVisibility(View.GONE);
            txt_sub_type_network.setVisibility(View.GONE);
            recycler_usage.setAdapter(null);
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
        refresh(4000);
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

    @RequiresApi(api = JELLY_BEAN_MR1)
    private void displayTypeNetwork() {
        txt_nom_operateur.setText(carrierName);
        txt_couverture.setText(R.string.niveau_couverture);
        txt_title_interface.setVisibility(View.VISIBLE);
        ConnectionStateListener = new PhoneStateListener() {

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                super.onDataConnectionStateChanged(state, networkType);
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        Picasso.get().load(R.drawable.ic_network_disable_signal).into(img_type_network);
                        txt_title_interface.setText(R.string.find_cell);
                        txt_couverture.setText(R.string.txt_unknown_network);
                        findNearestBts();
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(R.string.Sub_type );
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " GPRS");
                        displayListeQuality("GPRS");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " EDGE");
                        displayListeQuality("EDGE");
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        Picasso.get().load(R.drawable.gsm).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " CDMA");
                        displayListeQuality("CDMA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " UMTS");
                        displayListeQuality("UMTS");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA: //3G+ h
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:// est un complément de HSDPA
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " HSPA");
                        displayListeQuality("HSPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA://HSPA+   h+   3g++
                        Picasso.get().load(R.drawable.umts).into(img_type_network);
                        txt_sub_type_network.setVisibility(View.VISIBLE);
                        txt_sub_type_network.setText(getResources().getString(R.string.Sub_type)+ " HSPA+");
                        displayListeQuality("HSPAP");
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        Picasso.get().load(R.drawable.lte).into(img_type_network);
                        displayListeQuality("LTE");
                        break;
                    default:
                        Picasso.get().load(R.drawable.rx_5g).into(img_type_network);
                        displayListeQuality("5G");
                        break;
                }
            }

        };
        telephonyManager.listen(ConnectionStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    private void displayListeQuality(String network_subtype) {
        recycler_usage.setVisibility(View.VISIBLE);
        if(network_subtype.equals("Unknown")){
            listeUses.clear();
        }
        else if(network_subtype.equals("GPRS")){
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));

        }
        else if(network_subtype.equals("EDGE")){
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));

        }
        else if(network_subtype.equals("UMTS")){
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));

        }
        else if(network_subtype.equals("HSPA")){
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));

        }
        else if(network_subtype.equals("HSPAP")){
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp, R.drawable.ic_star_gray_24dp));

        }
        else if(network_subtype.equals("LTE")){
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_gray_24dp));

        }
        else {
            listeUses.clear();
            listeUses.add(new uses(R.drawable.ic_phone_msg_black_24dp, R.string.appel, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mail_outline_black_24dp, R.string.email, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_navigation_web_24dp, R.string.navigation_web, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_mms_black_24dp, R.string.email_piece, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_music_black_24dp, R.string.musique, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_voice_chat_black_24dp, R.string.appels_video, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));
            listeUses.add(new uses(R.drawable.ic_videogame_asset_black_24dp, R.string.jeux, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp, R.drawable.ic_star_green_24dp));

        }

        adapter = new usesAdapter(this,listeUses);
        recycler_usage.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void findNearestBts() {

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


}