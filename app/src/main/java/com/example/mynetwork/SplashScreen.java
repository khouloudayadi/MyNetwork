package com.example.mynetwork;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.example.mynetwork.Common.Common;
import com.example.mynetwork.DataBase.cellDataBase;
import com.example.mynetwork.DataBase.cellDataSource;
import com.example.mynetwork.DataBase.cellItem;
import com.example.mynetwork.DataBase.localCellDataSource;
import com.example.mynetwork.Model.Cell;
import com.example.mynetwork.Retrofit.INetworkAPI;
import com.example.mynetwork.Retrofit.RetrofitClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashScreen extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    INetworkAPI myNetworkAPI;
    cellDataSource cellDataSource;
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;
    List<cellItem> cells = new ArrayList<>();
    private ProgressBar progressBar;
    URL url = null;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        init();

        Dexter.withActivity(SplashScreen.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        progressBar.setVisibility(View.VISIBLE);
                        cellDataSource.countItemCell()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                    }
                                    @Override
                                    public void onSuccess(Integer integer) {
                                        Log.i("countCell", String.valueOf(integer));
                                        if(integer > 0){
                                            Intent home = new Intent(SplashScreen.this,HomeActivity.class);
                                            startActivity(home);
                                            finish();
                                        }
                                        else{

                                            /*compositeDisposable.add(myNetworkAPI.getCell()
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(cellModel -> {
                                                                if(cellModel.isSuccess()){
                                                                    for (Cell cell : cellModel.getResult()) {
                                                                        cellItem cellItem = new cellItem();
                                                                        cellItem.setRadio(cell.getRadio());
                                                                        cellItem.setCid(cell.getCid());
                                                                        cellItem.setArea(cell.getArea());
                                                                        cellItem.setMcc(cell.getMcc());
                                                                        cellItem.setMnc(cell.getMnc());
                                                                        cellItem.setLat(cell.getLat());
                                                                        cellItem.setLon(cell.getLon());
                                                                        cellItem.setRange(cell.getRange());
                                                                        cells.add(cellItem);
                                                                    }
                                                                    compositeDisposable.add(cellDataSource.insertAll(cells)
                                                                            .subscribeOn(Schedulers.io())
                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(()->
                                                                                    {
                                                                                        Log.d("addCell", String.valueOf(cells.size()));
                                                                                        Intent home = new Intent(SplashScreen.this,HomeActivity.class);
                                                                                        startActivity(home);
                                                                                        finish();
                                                                                    },
                                                                                    throwable ->
                                                                                    {
                                                                                        Log.d("addedCell",throwable.getMessage());
                                                                                    })
                                                                    );
                                                                }
                                                                else{
                                                                    Log.d("getCellDB", cellModel.getMessage());
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.d("getCellDB", throwable.getMessage());
                                                                Toast.makeText(SplashScreen.this,R.string.errorServer,Toast.LENGTH_LONG).show();
                                                                finish();
                                                            }

                                                    )
                                            );*/
                                            if(activeNetworkInfo == null ) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(SplashScreen.this,R.string.check_connection,Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                try {
                                                    url = new URL("http://clients3.google.com/generate_204");
                                                    HttpURLConnection httpUrlConnection =  (HttpURLConnection) url.openConnection();
                                                    httpUrlConnection.setRequestProperty("User-Agent", "android");
                                                    httpUrlConnection.setRequestProperty("Connection", "close");
                                                    httpUrlConnection.setConnectTimeout(1500); // Timeout is in seconds
                                                    httpUrlConnection.connect();
                                                    if (httpUrlConnection.getResponseCode() == 204 && httpUrlConnection.getContentLength() ==0) {
                                                        //progressBar.setVisibility(View.GONE);
                                                        //Toast.makeText(SplashScreen.this,"connection established",Toast.LENGTH_LONG).show();
                                                        compositeDisposable.add(myNetworkAPI.getCell()
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(cellModel -> {
                                                                            if(cellModel.isSuccess()){
                                                                                for (Cell cell : cellModel.getResult()) {
                                                                                    cellItem cellItem = new cellItem();
                                                                                    cellItem.setRadio(cell.getRadio());
                                                                                    cellItem.setCid(cell.getCid());
                                                                                    cellItem.setArea(cell.getArea());
                                                                                    cellItem.setMcc(cell.getMcc());
                                                                                    cellItem.setMnc(cell.getMnc());
                                                                                    cellItem.setLat(cell.getLat());
                                                                                    cellItem.setLon(cell.getLon());
                                                                                    cellItem.setRange(cell.getRange());
                                                                                    cells.add(cellItem);
                                                                                }
                                                                                compositeDisposable.add(cellDataSource.insertAll(cells)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(()->
                                                                                                {
                                                                                                    Log.d("addCell", String.valueOf(cells.size()));
                                                                                                    Intent home = new Intent(SplashScreen.this,HomeActivity.class);
                                                                                                    startActivity(home);
                                                                                                    finish();
                                                                                                },
                                                                                                throwable ->
                                                                                                {
                                                                                                    Log.d("addedCell",throwable.getMessage());
                                                                                                })
                                                                                );
                                                                            }
                                                                            else{
                                                                                Log.d("getCellDB", cellModel.getMessage());
                                                                            }
                                                                        },
                                                                        throwable -> {
                                                                            Log.d("getCellDB", throwable.getMessage());
                                                                            Toast.makeText(SplashScreen.this,R.string.errorServer,Toast.LENGTH_LONG).show();
                                                                            finish();
                                                                        }
                                                                )
                                                        );
                                                    }
                                                    else {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(SplashScreen.this,R.string.error_connection,Toast.LENGTH_LONG).show();
                                                        finish();
                                                    }
                                                } catch (MalformedURLException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }


                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.i("countCell",e.getMessage());
                                    }
                                });

                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            Toast.makeText(SplashScreen.this, R.string.Permission_Denied, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
                            builder.setTitle(R.string.Permission_Denied)
                                    .setMessage(R.string.permission_title)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();

    }

    private void init() {
        cellDataSource = new localCellDataSource(cellDataBase.getInstance(this).cellDAO());
        myNetworkAPI = RetrofitClient.getInstance(Common.baseUrl).create(INetworkAPI.class);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    }



}