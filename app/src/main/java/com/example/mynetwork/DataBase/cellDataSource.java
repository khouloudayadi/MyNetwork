package com.example.mynetwork.DataBase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface cellDataSource {

    Flowable<List<cellItem>> getAllCell();
    Single<Integer> countItemCell();
    Completable insertAll(List<cellItem> cellItems);
}
