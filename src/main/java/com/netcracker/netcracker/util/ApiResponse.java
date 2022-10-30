package com.netcracker.netcracker.util;

import com.netcracker.netcracker.entity.InputFile;

public class ApiResponse {
    Integer status;
    String message;
    InputFile data;

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

    public InputFile getData() {
        return data;
    }

    public void setData(InputFile data) {
        this.data = data;
    }

    public ApiResponse(){

    }

    public ApiResponse(Integer status, String message, InputFile data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
