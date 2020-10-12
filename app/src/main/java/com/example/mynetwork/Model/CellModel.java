package com.example.mynetwork.Model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CellModel {
    private List<Cell> result;
    private String message;
    private boolean success;


    public List<Cell> getResult() {
        return result;
    }

    public void setResult(List<Cell> result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
