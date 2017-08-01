package ua.moysa.meewfilemanager.data.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ua.moysa.meewfilemanager.data.response.Response;

/**
 * Created by Sergey Moysa
 */

public class FilesRepository {

    @NonNull
    private MutableLiveData<Response<File[]>> mData = new MutableLiveData<>();

    @WorkerThread
    @Nullable
    private File[] obtainFilesList(@NonNull File parent) {

        if (parent.exists() &&
                parent.canRead() &&
                parent.canWrite() &&
                parent.isDirectory()) {

            return parent.listFiles();
        }
        throw new IllegalArgumentException();
    }

    @WorkerThread
    private boolean delete(@NonNull File file) {

        if (file.isDirectory()) {

            File[] files = file.listFiles();

            if (files != null)
                for (File f : files) delete(f);
        }
        return file.delete();
    }

    @MainThread
    @NonNull
    public LiveData<Response<File[]>> listFiles(@NonNull File parent) {

        Observable.just(parent)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::obtainFilesList)
                .map(Response::success)
                .subscribe(
                        r -> mData.setValue(r),
                        e -> mData.setValue(Response.error(e.getMessage(), null)),
                        () -> {
                        },
                        d -> mData.setValue(Response.loading(null))
                );

        return mData;
    }

    @MainThread
    @NonNull
    public LiveData<Response<File[]>> deleteFile(@NonNull File file) {

        Observable.just(file)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::delete)
                .map(b -> b ? mData.getValue() : Response.error("error", (File[]) null))
                .subscribe(
                        r -> mData.setValue(r),
                        e -> mData.setValue(Response.error(e.getMessage(), null)),
                        () -> {
                        },
                        d -> mData.setValue(Response.loading(null))
                );
        return mData;
    }

    @NonNull
    public LiveData<Response<File[]>> getCurrentFiles() {
        return mData;
    }
}
