package ua.moysa.meewfilemanager.view;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.io.File;

import ua.moysa.meewfilemanager.data.repo.FilesRepository;

/**
 * Created by Sergey Moysa
 */

public class FilesViewModelProviderFactory extends ViewModelProvider.NewInstanceFactory {

    @NonNull
    private final FilesRepository mRepo;

    @NonNull
    private final File mParent;

    public FilesViewModelProviderFactory(@NonNull File parent) {
        mRepo = new FilesRepository();
        mParent = parent;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {

        if (modelClass.equals(FilesViewModel.class)) {
            //noinspection unchecked
            return (T) new FilesViewModel(mRepo, mParent);
        }

        return super.create(modelClass);
    }
}
