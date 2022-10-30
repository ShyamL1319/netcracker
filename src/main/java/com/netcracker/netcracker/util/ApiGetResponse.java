package com.netcracker.netcracker.util;

import com.netcracker.netcracker.entity.InputFile;

import java.util.ArrayList;

public class ApiGetResponse {
    Integer status;
    String message;
    ArrayList<InputFile> data = new ArrayList<>();
    public ApiGetResponse(){

    }
    public ApiGetResponse(Integer status, String message, ArrayList<InputFile> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<InputFile> getData() {
        return data;
    }

    public void setData(ArrayList<InputFile> data) {
        this.data = data;
    }
}
