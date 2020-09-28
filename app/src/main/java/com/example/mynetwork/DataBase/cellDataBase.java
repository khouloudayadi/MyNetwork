package com.example.mynetwork.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 5,entities = cellItem.class,exportSchema = false)
public abstract class cellDataBase extends RoomDatabase {
    private static cellDataBase  instance;

    public abstract CellDAO cartDAO();

    public  static cellDataBase getInstance(Context context){
        if (instance == null)
            instance= Room.databaseBuilder(context,cellDataBase.class,"CellTowerOrange")
                    .build();
        return instance;
    }
}
