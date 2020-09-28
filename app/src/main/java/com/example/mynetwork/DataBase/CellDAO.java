package com.example.mynetwork.DataBase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface CellDAO {

    @Query("SELECT * FROM Cell ")
    Flowable<List<cellItem>> getAllCell();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertorReplaceAll(cellItem... cellItems);
}
