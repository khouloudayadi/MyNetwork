package com.example.mynetwork.DataBase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface cellDataSource {
    Flowable<List<cellItem>> getAllCell(int cid);
    Completable insertorReplaceAll(cellItem... cellItems);
}
