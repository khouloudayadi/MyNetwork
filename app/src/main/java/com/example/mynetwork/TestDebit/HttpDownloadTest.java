package com.example.mynetwork.TestDebit;

import android.util.Log;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class HttpDownloadTest {

    public String fileURL = "";
    long startTime = 0;
    long endTime = 0;
    double downloadElapsedTime = 0;
    int downloadedByte = 0;
    double finalDownloadRate = 0.0;
    boolean finished = false;
    double instantDownloadRate = 0;
    int timeout = 5;

    HttpsURLConnection httpsConn = null;

    public HttpDownloadTest(String fileURL) {
        this.fileURL = fileURL;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd;
        try {
            bd = new BigDecimal(value);
        } catch (Exception ex) {
            return 0.0;
        }
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public boolean isFinished() {
        return finished;
    }

    public double getFinalDownloadRate() {
        return round(finalDownloadRate, 2);
    }

    public double getInstantDownloadRate() {
        return instantDownloadRate;
    }

    public void setInstantDownloadRate(int downloadedByte, double elapsedTime) {

        if (downloadedByte >= 0) {
            this.instantDownloadRate = round((Double) (((downloadedByte * 8) / (1000 * 1000)) / elapsedTime), 2);
        } else {
            this.instantDownloadRate = 0.0;
        }
    }

    public void run() {
        URL url = null;
        downloadedByte = 0;
        int responseCode = 0;

        List<String> fileUrls = new ArrayList<>();
        fileUrls.add(fileURL + "random4000x4000.jpg");
        fileUrls.add(fileURL + "random3000x3000.jpg");

        startTime = System.currentTimeMillis();

        outer:
        for (String link : fileUrls) {
            try {
                url = new URL(link);  //link=random4000x4000.jpg
                httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                httpsConn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                httpsConn.connect();
                responseCode = httpsConn.getResponseCode();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                break outer;
            }

            try {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    byte[] buffer = new byte[10240];

                    InputStream inputStream = httpsConn.getInputStream();
                    int len = 0;

                    while ((len = inputStream.read(buffer)) != -1) {
                        downloadedByte += len;
                        endTime = System.currentTimeMillis();
                        downloadElapsedTime = (endTime - startTime) / 1000.0;//en s
                        setInstantDownloadRate(downloadedByte, downloadElapsedTime);
                        if (downloadElapsedTime >= timeout) {
                            break outer;
                        }
                    }

                    inputStream.close();
                    httpsConn.disconnect();
                }
                else {
                    Log.d("ErrorDlLink","Link not found...");
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        endTime = System.currentTimeMillis();
        downloadElapsedTime = (endTime - startTime) / 1000.0;
        finalDownloadRate = ((downloadedByte * 8) / 1000000.0) / downloadElapsedTime;  // bits to Mbits
        Log.d("downloadedByte", String.valueOf(downloadedByte));
        Log.d("downloadElapsedTime", String.valueOf(downloadElapsedTime));
        finished = true;
    }
}
