package com.example.mynetwork.DataBase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class localCellDataSource implements cellDataSource {
    private CellDAO cellDAO;

    public localCellDataSource(CellDAO cellDAO) {
        this.cellDAO = cellDAO;
    }


    @Override
    public Flowable<List<cellItem>> getAllCell() {
        return cellDAO.getAllCell();
    }

    @Override
    public Single<Integer> countItemCell() {
        return cellDAO.countItemCell();
    }

    @Override
    public Completable insertAll(List<cellItem> cellItems) {
        return cellDAO.insertAll(cellItems);
    }


}
