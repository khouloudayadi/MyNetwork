package com.example.mynetwork.Retrofit;


import com.example.mynetwork.Model.CellModel;
import com.example.mynetwork.Model.addCellModel;
import com.example.mynetwork.Model.predictModel;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface INetworkAPI {

    @Headers({"Content-Type:application/json"})
    @POST("predict")
    Observable<predictModel> getPredictTime(@Body JsonObject precictTime);

    @GET("getCell")
    Observable<CellModel> getCell();
    //Call<List<Cell>> getCell();

    @Headers({"Content-Type:application/json"})
    @POST("addCell")
    Observable<addCellModel> addCell(@Body JsonObject cell);
}
