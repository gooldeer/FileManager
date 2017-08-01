package ua.moysa.meewfilemanager;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import ua.moysa.meewfilemanager.data.response.Response;
import ua.moysa.meewfilemanager.databinding.FragmentFolderBinding;
import ua.moysa.meewfilemanager.view.FilesAdapter;
import ua.moysa.meewfilemanager.view.FilesViewModel;
import ua.moysa.meewfilemanager.view.FilesViewModelProviderFactory;

/**
 * Created by Sergey Moysa
 */
public class FolderFragment extends Fragment {

    private static final String PARENT_KEY = "fragment_folder.parent";

    private FragmentFolderBinding mBinding;

    private File mParent;

    private FilesViewModel mViewModel;

    private FilesAdapter mAdapter;

    public FolderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_folder, container, false);

        mBinding = DataBindingUtil.bind(root);

        if (savedInstanceState != null) {

            mParent = (File) savedInstanceState.getSerializable(PARENT_KEY);
        }

        if (mParent == null) {
            //TODO use preferences for this
            mParent = Environment.getExternalStorageDirectory();
        }

        FilesViewModelProviderFactory factory = new FilesViewModelProviderFactory();
        mViewModel = ViewModelProviders.of(this, factory).get(FilesViewModel.class);

        mAdapter = new FilesAdapter(null);

        mBinding.filesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.filesRecycler.setItemAnimator(new DefaultItemAnimator());

        mBinding.filesRecycler.setAdapter(mAdapter);

        mAdapter.setOnClickListener(this::selectFile);

        loadFiles();

        return root;
    }

    private void loadFiles() {
        mViewModel
                .listFiles(mParent)
                .observe((LifecycleOwner) getActivity(), this::onFilesLoad);
    }

    private void onFilesLoad(Response<File[]> response) {
        if (response.getStatus() == Response.Status.SUCCESS) {
            mAdapter.setItems(response.getData());
        } else if (response.getStatus() == Response.Status.ERROR) {
            Snackbar
                    .make(
                            mBinding.filesContent,
                            R.string.snack_files_load_error,
                            Snackbar.LENGTH_SHORT)
                    .show();
        } else if (response.getStatus() == Response.Status.LOADING) {
            //TODO show progress bar
        }
    }

    private void selectFile(@NonNull File file) {
        //TODO open new folder fragment or send intent
        Snackbar
                .make(
                        mBinding.filesContent,
                        "File selected: " + file.getName(),
                        Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(PARENT_KEY, mParent);
    }
}
