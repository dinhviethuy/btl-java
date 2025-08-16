package com.fullnestjob.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private Object error;
    private Object statusCode;
    private String message;

    public ApiResponse() {}

    public ApiResponse(T data, Object statusCode, String message) {
        this.data = data;
        this.statusCode = statusCode;
        this.message = message;
    }

    public ApiResponse(Object error, Object statusCode, String message, boolean isError) {
        this.error = error;
        this.statusCode = statusCode;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public Object getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Object statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


