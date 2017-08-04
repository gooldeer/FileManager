package ua.moysa.meewfilemanager.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ua.moysa.meewfilemanager.data.repo.FilesRepository;
import ua.moysa.meewfilemanager.data.response.Response;

/**
 * Created by Sergey Moysa
 */

public class FilesViewModel extends ViewModel {

    @NonNull
    private final FilesRepository mRepo;
    @NonNull
    private MutableLiveData<Response<File[]>> mData = new MutableLiveData<>();
    @NonNull
    private File mParent;

    FilesViewModel(@NonNull FilesRepository repo, @NonNull File parent) {
        this.mRepo = repo;
        this.mParent = parent;
    }

    @NonNull
    @MainThread
    public LiveData<Response<File[]>> listFiles(@NonNull File parent) {
        Observable.just(parent)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(mRepo::obtainFilesList)
                .map(Response::success)
                .doOnSubscribe(disposable -> mData.setValue(Response.loading(null)))
                .subscribe(
                        r -> mData.setValue(r),
                        e -> mData.setValue(Response.error(e.getMessage(), null))
                );

        return mData;
    }

    @NonNull
    @MainThread
    public LiveData<Response<File[]>> delete(@NonNull File... files) {
        Observable.fromArray(files)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(mRepo::delete)
                .filter(deleted -> !deleted)
                .toList()
                .map(notDeletedList ->
                        notDeletedList.size() > 0 ?
                                Response.error("error", (File[]) null) :
                                Response.success(mRepo.obtainFilesList(mParent)))
                .doOnSubscribe(disposable -> mData.postValue(Response.loading(null)))
                .subscribe(
                        r -> mData.setValue(r),
                        e -> mData.setValue(Response.error(e.getMessage(), null))
                );
        return mData;
    }

    @NonNull
    public MutableLiveData<Response<File[]>> getLiveData() {
        return mData;
    }
}
