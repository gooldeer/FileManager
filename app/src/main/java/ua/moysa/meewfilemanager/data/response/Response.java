package ua.moysa.meewfilemanager.data.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static ua.moysa.meewfilemanager.data.response.Response.Status.ERROR;
import static ua.moysa.meewfilemanager.data.response.Response.Status.LOADING;
import static ua.moysa.meewfilemanager.data.response.Response.Status.SUCCESS;

/**
 * Created by Sergey Moysa
 */

public class Response<T> {

    @Nullable
    private final T data;

    @Nullable
    private final String error;

    @NonNull
    private final Status status;

    private Response(@NonNull Status status, @Nullable T data, @Nullable String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Response<T> success(@NonNull T data) {
        return new Response<T>(SUCCESS, data, null);
    }

    public static <T> Response<T> error(@NonNull String error, @Nullable T data) {
        return new Response<T>(ERROR, data, error);
    }

    public static <T> Response<T> loading(@Nullable T data) {
        return new Response<T>(LOADING, data, null);
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getError() {
        return error;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    public enum Status {
        SUCCESS, ERROR, LOADING
    }
}
