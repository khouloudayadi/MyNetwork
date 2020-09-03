package com.example.mynetwork;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<CellInfo> cellInfoList = null;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TelephonyManager telephonyManager;
    @SuppressLint({"NewApi", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.cell_value);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cellInfoList = telephonyManager.getAllCellInfo();
        }

        if (cellInfoList == null) {
            tv.setText("getAllCellInfo() is null");
        }
        else if (cellInfoList.size() == 0) {
            tv.setText("The base station list is empty");
        }
        else {
            Log.d("size cell = ",String.valueOf(cellInfoList.size()));
            CellInfo cellInfo = cellInfoList.get(0);
            if (cellInfo instanceof CellInfoLte) {
                //4G
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                tv.setText("RSSI :"+cellInfoLte.getCellSignalStrength().getRssi());
            } else if (cellInfo instanceof CellInfoGsm) {
                //2G
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                tv.setText("RSSI :"+cellInfoGsm.getCellSignalStrength().getDbm());
            } else if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                tv.setText("RSSI :"+cellInfoWcdma.getCellSignalStrength().getDbm());
            }

        }

    }
}