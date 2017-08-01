package ua.moysa.meewfilemanager.data.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.Arrays;

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

    @NonNull
    private File mParent;

    public FilesRepository(@NonNull File parent) {
        this.mParent = parent;
    }

    @WorkerThread
    @NonNull
    private File[] obtainFilesList(@NonNull File parent) {

        if (parent.exists() &&
                parent.canRead() &&
                parent.canWrite() &&
                parent.isDirectory()) {

            File[] files = parent.listFiles();

            if (files != null) {

                mParent = parent;
                Arrays.sort(files);
                return files;
            }
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
    public LiveData<Response<File[]>> deleteFile(@NonNull File... files) {

        Observable.just(files)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(all -> {
                    for (File f : all) {
                        if (!delete(f)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(b -> b ?
                        Response.success(obtainFilesList(mParent)) :
                        Response.error("error", (File[]) null))
                .subscribe(
                        r -> mData.setValue(r),
                        e -> mData.setValue(Response.error(e.getMessage(), null)),
                        () -> {
                        },
                        d -> mData.setValue(Response.loading(null))
                );
        return mData;
    }
}
