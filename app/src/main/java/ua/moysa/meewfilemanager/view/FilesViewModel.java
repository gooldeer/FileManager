package ua.moysa.meewfilemanager.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.io.File;

import ua.moysa.meewfilemanager.data.repo.FilesRepository;
import ua.moysa.meewfilemanager.data.response.Response;

/**
 * Created by Sergey Moysa
 */

public class FilesViewModel extends ViewModel {

    @NonNull
    private final FilesRepository mRepo;

    public FilesViewModel(@NonNull FilesRepository repo) {
        this.mRepo = repo;
    }

    @NonNull
    public LiveData<Response<File[]>> listFiles(@NonNull File parent) {
        return mRepo.listFiles(parent);
    }

    @NonNull
    public LiveData<Response<File[]>> delete(@NonNull File... files) {
        return mRepo.deleteFile(files);
    }
}
