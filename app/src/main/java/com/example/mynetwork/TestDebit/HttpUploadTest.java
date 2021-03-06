package com.example.mynetwork.TestDebit;

import android.util.Log;

import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class HttpUploadTest extends Thread {

    public String fileURL = "";
    static int uploadedKByte = 0;
    double uploadElapsedTime = 0;   //upload time
    boolean finished = false;
    double elapsedTime = 0;
    double finalUploadRate = 0.0;   //débit up
    long startTime;


    public HttpUploadTest(String fileURL) {
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

    public double getFinalUploadRate() {
        return round(finalUploadRate, 2);
    } // 1.499 ==>1.50

    public double getInstantUploadRate() { // get débit en cours de télechargement
        try {
            BigDecimal bd = new BigDecimal(uploadedKByte);
        } catch (Exception ex) {
            return 0.0;
        }

        if (uploadedKByte >= 0) {
            long now = System.currentTimeMillis();//millisecondes
            elapsedTime = (now - startTime) / 1000.0;   //millisecondes to secondes
            return round((Double) (((uploadedKByte / 1000.0) * 8) / elapsedTime), 2); // KByte to MByte : / 1000 | Byte to bit : *8   |= Mbps
        } else {
            return 0.0;
        }
    }



    @Override
    public void run() {
        try {
            URL url = new URL(fileURL);
            uploadedKByte = 0;
            startTime = System.currentTimeMillis();   //millisecondes

            //https://www.baeldung.com/java-executor-service-tutorial
            ExecutorService executor = Executors.newFixedThreadPool(4); // 4 threads asynchrone
            for (int i = 0; i < 4; i++) {
                executor.execute(new HandlerUpload(url));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

            long now = System.currentTimeMillis(); //time in milliseconds
            uploadElapsedTime = (now - startTime) / 1000.0;
            finalUploadRate = (Double) (((uploadedKByte / 1000.0) * 8) / uploadElapsedTime);//D=taille/temps |Ko --> Mo : /1000 | Byte --> bit: *8
            Log.d("uploadedKByte", String.valueOf(uploadedKByte));
            Log.d("uploadElapsedTime", String.valueOf(uploadElapsedTime));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        finished = true;
    }
}

class HandlerUpload extends Thread {

    URL url;

    public HandlerUpload(URL url) {
        this.url = url;
    }

    public void run() {
        byte[] buffer = new byte[150 * 1024];
        long startTime = System.currentTimeMillis();
        int timeout = 8;

        while (true) {

            try {
                HttpsURLConnection conn = null;
                conn = (HttpsURLConnection) url.openConnection();
                conn.setDoOutput(true);//allow output
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                conn.connect();

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());


                dos.write(buffer, 0, buffer.length);
                dos.flush();

                conn.getResponseCode();

                HttpUploadTest.uploadedKByte += buffer.length / 1024.0;
                long endTime = System.currentTimeMillis();
                double uploadElapsedTime = (endTime - startTime) / 1000.0; //upload time en s
                if (uploadElapsedTime >= timeout) {
                    break;
                }

                dos.close();
                conn.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}
