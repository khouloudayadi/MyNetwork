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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.developer.kalert.KAlertDialog;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.Test.GetSpeedTestHostsHandler;
import com.example.mynetwork.Test.HttpDownloadTest;
import com.example.mynetwork.Test.HttpUploadTest;
import com.example.mynetwork.Test.PingTest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

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
    AlertDialog alertDialog;
    KAlertDialog alert_no_conn;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        startButton.setText(R.string.btn_test);
        pingTextView.setText("0 ms");
        downloadTextView.setText("0 Mbps");
        uploadTextView.setText("0 Mbps");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_debit);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initView();
        init();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if gps is enabled or not and then request user to enable it
                GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (GpsStatus == true) {
                    if(activeNetworkInfo == null ) {
                        Common.cpt_wifi = 0;
                        alert_no_conn.show();
                    }
                    else{
                        dialog.show();
                        getDeviceLocation();
                        testDebit();
                    }
                } else {
                    alertDialog.show();
                }
            }

        });
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
        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_alert_gps)
                .setMessage(R.string.msg_alert_gps_1)
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

        alert_no_conn = new KAlertDialog(this);
        alert_no_conn.setContentText(getResources().getString(R.string.check_connection));
        alert_no_conn.setContentTextSize(20);
        alert_no_conn.setConfirmText("OK");
        alert_no_conn.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
            @Override
            public void onClick(KAlertDialog kAlertDialog) {
                alert_no_conn.dismiss();
                Common.cpt_no_conn = 0 ;
            }
        });
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
        Log.d("location1", String.valueOf(lat_user));
        pingTextView.setText("0 ms");
        downloadTextView.setText("0 Mbps");
        uploadTextView.setText("0 Mbps");
        startButton.setEnabled(false);
        startButton.setBackgroundResource(R.drawable.border_button);
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.run();
        new Thread(new Runnable() {
            RotateAnimation rotate;
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setTextSize(13);
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
                                Toast.makeText(getApplicationContext(), R.string.Pas_de_connection, Toast.LENGTH_LONG).show();
                                //Restart Test
                                startButton.setEnabled(true);
                                startButton.setTextSize(16);
                                startButton.setText(R.string.btn_redémarrer_le_test);
                                startButton.setBackgroundResource(android.R.color.transparent);
                                dialog.dismiss();
                            }

                        });
                        getSpeedTestHostsHandler = null;
                        return;
                    }
                }

                if (lat_user == 0.0) {
                    try {
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
                    Log.d("location2", String.valueOf(source));

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
                                startButton.setTextSize(12);
                                startButton.setText(R.string.hote_pb);
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setTextSize(13);
                            startButton.setText(String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                        }
                    });


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
                                pingTextView.setText(R.string.Erreur_ping);
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pingTextView.setText(dec.format(pingTest.getAvgRtt()) + " ms");

                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pingTextView.setText(dec.format(pingTest.getInstantRtt()) + " ms");
                                }
                            });
                        }

                        //Download Test
                        if (pingTestFinished) {
                            if (downloadTestFinished) {
                                //Failure
                                if (downloadTest.getFinalDownloadRate() == 0) {
                                    downloadTextView.setText(R.string.Erreur_de_téléchargement);
                                } else {
                                    //Success
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
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
                                    System.out.println(R.string.Erreur_d_envoi);
                                } else {
                                    //Success
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
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
                            startButton.setTextSize(16);
                            startButton.setText(R.string.Redmarrer_le_test);
                            startButton.setBackgroundResource(android.R.color.transparent);
                        }
                    });
                }
            }
        }).start();


    }
}