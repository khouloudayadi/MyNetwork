package com.example.mynetwork;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.mynetwork.TestDebit.GetSpeedTestHostsHandler;
import com.example.mynetwork.TestDebit.HttpDownloadTest;
import com.example.mynetwork.TestDebit.HttpUploadTest;
import com.example.mynetwork.TestDebit.PingTest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static java.lang.Thread.sleep;

public class testDebitActivity extends AppCompatActivity {

    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    static int position = 0;
    static int lastPosition = 0;
    final DecimalFormat dec = new DecimalFormat("#.##");

    private FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private double lat_user, lon_user;

    //enable GPS
    boolean GpsStatus;
    LocationManager locationManager;
    ConnectivityManager connectivityManager;

    android.app.AlertDialog dialog;
    AlertDialog alertDialogGPS;
    URL url = null;

    //refresh
    private Handler handler;
    private Runnable runnable;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_ping)
    TextView pingTextView;
    @BindView(R.id.downloadTextView)
    TextView downloadTextView;
    @BindView(R.id.uploadTextView)
    TextView uploadTextView;
    @BindView(R.id.barImageView)
    ImageView barImageView;
    @BindView(R.id.startButton)
    Button startButton;
    @BindView(R.id.txt_state)
    TextView txt_state;
    @BindView(R.id.layout_sans_conx_test) LinearLayout layout_sans_conx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_debit);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initView();
        init();

        checkConnexion();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if gps is enabled or not and then request user to enable it
                GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (GpsStatus == true) {
                    if(activeNetworkInfo == null ) {
                       layout_sans_conx.setVisibility(View.VISIBLE);
                    }
                    else{
                        dialog.show();
                        getDeviceLocation();
                        testDebit();
                    }
                } else {
                    alertDialogGPS.show();
                }
            }

        });
    }

    private void checkConnexion() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null ) {
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
        toolbar.setTitle(R.string.tester_debit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        dialog = new SpotsDialog.Builder().setContext(this).setMessage(R.string.wating_location).setCancelable(false).build();
        //enable GPS
        alertDialogGPS = new AlertDialog.Builder(this)
                .setTitle(R.string.title_alert_gps)
                .setMessage(R.string.msg_alert_gps_test)
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

    private void getDeviceLocation() {
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                lat_user = locationResult.getLastLocation().getLatitude();
                lon_user = locationResult.getLastLocation().getLongitude();
            }
        };

    }

    public int getPositionByRate(double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);

        } else if (rate <= 10) {
            return (int) (rate * 6) + 30;

        } else if (rate <= 30) {
            return (int) ((rate - 10) * 3) + 90;

        } else if (rate <= 50) {
            return (int) ((rate - 30) * 1.5) + 150;

        } else if (rate <= 100) {
            return (int) ((rate - 50) * 1.2) + 180;
        }

        return 0;
    }

    private void testDebit() {
        pingTextView.setText("0 ms");
        downloadTextView.setText("0 Mbps");
        uploadTextView.setText("0 Mbps");
        startButton.setEnabled(false);
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.run();
        new Thread(new Runnable() {
            RotateAnimation rotate;
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setTextSize(15);
                        startButton.setText(R.string.meilleur_serveur);
                    }
                });

                //Get hosts
                int timeCount = 60;
                while (!getSpeedTestHostsHandler.isFinished()) {
                    timeCount--;
                    try { sleep(100);} catch (InterruptedException e) {}
                    if (timeCount <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_connection, Toast.LENGTH_LONG).show();
                                //Restart Test
                                startButton.setEnabled(true);
                                startButton.setText(R.string.btn_redémarrer_le_test);
                                dialog.dismiss();
                            }

                        });
                        getSpeedTestHostsHandler = null;
                        return;
                    }
                }

                if (lat_user == 0.0) {
                    try {
                        Log.d("lat_user","ok");
                        sleep(2000);
                    } catch (InterruptedException e) {}
                    testDebit();
                }
                else {
                    //Trouver le serveur le plus proche
                    HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                    HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                    double tmp = 19349458; //plus grand distance entre serveur et user
                    double dist = 0.0;
                    int findServerIndex = 0;
                    Location source = new Location("Source");
                    source.setLatitude(lat_user);
                    source.setLongitude(lon_user);
                    Log.d("location", String.valueOf(source));

                    for (int index : mapKey.keySet()) {
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

                    String testAddr = mapKey.get(findServerIndex).replace("http://", "https://");
                    final List<String> info = mapValue.get(findServerIndex); //info=[lat,lon,name,country,cc,sponsor,host]
                    final double distance = dist;
                    if (info == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("Error Host", "There was a problem in getting Host Location. Try again later");
                                startButton.setTextSize(13);
                                startButton.setText(R.string.error_hote);
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setTextSize(15);
                            startButton.setText(String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                        }
                    });

                    Log.d("Distance", String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));

                    //Reset value
                    final List<Double> downloadRateList = new ArrayList<>();
                    final List<Double> uploadRateList = new ArrayList<>();

                    Boolean pingTestStarted = false;
                    Boolean pingTestFinished = false;
                    Boolean downloadTestStarted = false;
                    Boolean downloadTestFinished = false;
                    Boolean uploadTestStarted = false;
                    Boolean uploadTestFinished = false;

                    //Init Test
                    final PingTest pingTest = new PingTest(info.get(6).replace(":8080", ""), 3);
                    final HttpDownloadTest downloadTest = new HttpDownloadTest(testAddr.replace(testAddr.split("/")[testAddr.split("/").length - 1], ""));
                    final HttpUploadTest uploadTest = new HttpUploadTest(testAddr);

                    dialog.dismiss();
                    //Tests
                    while (true) {

                        if (!pingTestStarted) {
                            pingTest.run();
                            pingTestStarted = true;
                        }
                        if (pingTestFinished && !downloadTestStarted) {
                            downloadTest.run();
                            downloadTestStarted = true;
                        }
                        if (downloadTestFinished && !uploadTestStarted) {
                            uploadTest.start();
                            uploadTestStarted = true;
                        }


                        //Ping Test
                        if (pingTestFinished) {
                            //Failure
                            if (pingTest.getAvgRtt() == 0) {
                                txt_state.setText("");
                                pingTextView.setText(R.string.error_ping);
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txt_state.setText("");
                                        pingTextView.setText(dec.format(pingTest.getAvgRtt()) + " ms");

                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_state.setText(R.string.ping_cours);
                                    pingTextView.setText(dec.format(pingTest.getInstantRtt()) + " ms");
                                }
                            });
                        }

                        //Download Test
                        if (pingTestFinished) {
                            if (downloadTestFinished) {
                                //Failure
                                if (downloadTest.getFinalDownloadRate() == 0) {
                                    txt_state.setText("");
                                    downloadTextView.setText(R.string.error_download);
                                }
                                else {
                                    //Success
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txt_state.setText("");
                                            downloadTextView.setText(dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
                                        }
                                    });
                                }
                            } else {
                                //Calc position
                                double downloadRate = downloadTest.getInstantDownloadRate();
                                downloadRateList.add(downloadRate);
                                position = getPositionByRate(downloadRate);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        rotate.setInterpolator(new LinearInterpolator());
                                        rotate.setDuration(100);
                                        barImageView.startAnimation(rotate);
                                        txt_state.setText(R.string.download_cours);
                                        downloadTextView.setText(dec.format(downloadRate) + " Mbps");

                                    }

                                });
                                lastPosition = position;
                            }

                        }

                        //Upload Test
                        if (downloadTestFinished) {

                            if (uploadTestFinished) {
                                //Failure
                                if (uploadTest.getFinalUploadRate() == 0) {
                                    txt_state.setText("");
                                    uploadTextView.setText(R.string.error_upload);
                                } else {
                                    //Success
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txt_state.setText("");
                                            uploadTextView.setText(dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
                                        }
                                    });
                                }
                            } else {
                                //Calc position
                                double uploadRate = uploadTest.getInstantUploadRate();
                                uploadRateList.add(uploadRate);
                                position = getPositionByRate(uploadRate);

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        rotate.setInterpolator(new LinearInterpolator());
                                        rotate.setDuration(100);
                                        barImageView.startAnimation(rotate);
                                        txt_state.setText(R.string.upload_cours);
                                        uploadTextView.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
                                    }

                                });
                                lastPosition = position;
                            }

                        }

                        //Test terminé
                        if (pingTestFinished && downloadTestFinished && uploadTest.isFinished()) {
                            break;
                        }
                        if (pingTest.isFinished()) {
                            pingTestFinished = true;
                        }
                        if (downloadTest.isFinished()) {
                            downloadTestFinished = true;
                        }
                        if (uploadTest.isFinished()) {
                            uploadTestFinished = true;
                        }
                        if (pingTestStarted && !pingTestFinished) {
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                    //réactiver button
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setEnabled(true);
                            startButton.setTextSize(18);
                            txt_state.setText("");
                            startButton.setText(R.string.restart_test);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        startButton.setText(R.string.btn_test);
        pingTextView.setText("0 ms");
        downloadTextView.setText("0 Mbps");
        uploadTextView.setText("0 Mbps");
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        getSpeedTestHostsHandler = null;
        super.onPause();
    }

}