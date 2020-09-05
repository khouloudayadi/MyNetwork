package com.example.mynetwork;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import com.example.mynetwork.Model.BaseStation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<CellInfo> cellInfoList = null;
    boolean error;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TelephonyManager telephonyManager;
    @SuppressLint({"NewApi", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TextView tv = findViewById(R.id.cell_value);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cellInfoList = telephonyManager.getAllCellInfo();
        }
        if (cellInfoList == null) {
            tv.setText("The base station list is null");
        }
        else if (cellInfoList.size() == 0) {
            tv.setText("The base station list is empty");
        }
        else {
            for (CellInfo cellInfo : cellInfoList) {
                BaseStation bs = bindData(cellInfo);
                Log.i("cell",bs.toString());
                if(bs.getMnc() == 1) {
                    Log.i("cellOrange",bs.toString());
                }
                else{
                    continue;
                }

            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private BaseStation bindData(CellInfo cellInfo) {
        BaseStation baseStation = null;

        // La station de base a différents types de signaux: 2G, 3G, 4G
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (cellInfo instanceof CellInfoWcdma) {
                //3G
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                baseStation = new BaseStation();
                baseStation.setType("WCDMA");
                baseStation.setCid(cellIdentityWcdma.getCid());
                baseStation.setLac(cellIdentityWcdma.getLac());
                baseStation.setMcc(cellIdentityWcdma.getMcc());
                baseStation.setMnc(cellIdentityWcdma.getMnc());
                if (cellInfoWcdma.getCellSignalStrength() != null) {
                    baseStation.setRssi(cellInfoWcdma.getCellSignalStrength().getDbm()); //Get the signal strength as dBm
                }
            }
            else if (cellInfo instanceof CellInfoLte) {
                //4G
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                baseStation = new BaseStation();
                baseStation.setType("LTE");
                baseStation.setCid(cellIdentityLte.getCi());
                baseStation.setMnc(cellIdentityLte.getMnc());
                baseStation.setMcc(cellIdentityLte.getMcc());
                baseStation.setLac(cellIdentityLte.getTac());
                if (cellInfoLte.getCellSignalStrength() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        baseStation.setRssi(cellInfoLte.getCellSignalStrength().getRssi());
                    }
                }
            }
            else if (cellInfo instanceof CellInfoGsm) {
                //2G
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                baseStation = new BaseStation();
                baseStation.setType("GSM");
                baseStation.setCid(cellIdentityGsm.getCid());
                baseStation.setLac(cellIdentityGsm.getLac());
                baseStation.setMcc(cellIdentityGsm.getMcc());
                baseStation.setMnc(cellIdentityGsm.getMnc());
                if (cellInfoGsm.getCellSignalStrength() != null) {
                    baseStation.setRssi(cellInfoGsm.getCellSignalStrength().getDbm());
                }
            }
            else {
                Log.e("TAG", "CDMA CellInfo................................................");
            }
        }
        return baseStation;
    }
    public BaseStation GetOpenCellID(BaseStation bs) throws IOException {
        String strURLSent =
                "http://www.opencellid.org/cell/get?key=ec89397cee46f6&mcc=" + bs.getMcc()
                        + "&mnc=" + bs.getMnc()
                        + "&cellid=" + bs.getCid()
                        + "&lac=" + bs.getLac()
                        + "&format=json";

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet(strURLSent));
        String GetOpenCellID_fullresult = EntityUtils.toString(response.getEntity());
        if (GetOpenCellID_fullresult.equalsIgnoreCase("err")) {
            error = true;
        } else {
            error = false;
            String[] tResult = GetOpenCellID_fullresult.split(",");
            bs.setLat(Double.parseDouble(tResult[0].substring(7)));
            bs.setLon(Double.parseDouble(tResult[1].substring(6)));
        }
        return bs;
    }

}