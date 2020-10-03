package com.example.mynetwork.DataBase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class localCellDataSource implements cellDataSource  {
    private CellDAO cellDAO;

    public localCellDataSource(CellDAO cellDAO) {
        this.cellDAO = cellDAO;
    }

    @Override
    public Flowable<List<cellItem>> getAllCell(int cid) {
        return cellDAO.getAllCell(cid);
    }

    @Override
    public Completable insertorReplaceAll(cellItem... cellItems) {
        return cellDAO.insertorReplaceAll(cellItems);
    }
}
