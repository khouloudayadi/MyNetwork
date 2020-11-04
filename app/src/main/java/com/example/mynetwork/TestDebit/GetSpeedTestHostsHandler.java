package com.example.mynetwork.TestDebit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GetSpeedTestHostsHandler {
    HashMap<Integer, String> mapKey = new HashMap<>();
    HashMap<Integer, List<String>> mapValue = new HashMap<>();
    boolean finished = false;


    public HashMap<Integer, String> getMapKey() {
        return mapKey;
    }

    public HashMap<Integer, List<String>> getMapValue() {
        return mapValue;
    }

    public boolean isFinished() {
        return finished;
    }

    public void run() {

        String uploadAddress = "";
        String name = "";
        String country = "";
        String cc = "";
        String sponsor = "";
        String lat = "";
        String lon = "";
        String host = "";


        //Best server
        int count = 0;
        try {
            URL url = new URL("https://www.speedtest.net/speedtest-servers-static.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int code = urlConnection.getResponseCode();

            if (code == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                urlConnection.getInputStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("<server url")) {
                        uploadAddress = line.split("server url=\"")[1].split("\"")[0];//http://speedtest3.ooredoo.tn:8080/speedtest/upload.php
                        lat = line.split("lat=\"")[1].split("\"")[0];
                        lon = line.split("lon=\"")[1].split("\"")[0];
                        name = line.split("name=\"")[1].split("\"")[0];//sousse
                        country = line.split("country=\"")[1].split("\"")[0];//Tunisia
                        cc = line.split("cc=\"")[1].split("\"")[0];//TN
                        sponsor = line.split("sponsor=\"")[1].split("\"")[0]; //ooredoo
                        host = line.split("host=\"")[1].split("\"")[0];//speedtest3.ooredoo.tn:8080

                        List<String> ls = Arrays.asList(lat, lon, name, country, cc, sponsor, host);
                        mapKey.put(count, uploadAddress);
                        mapValue.put(count, ls);
                        count++;
                    }
                }

                br.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        finished = true;
    }
}
