package ua.moysa.meewfilemanager;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ua.moysa.meewfilemanager.data.response.Response;
import ua.moysa.meewfilemanager.databinding.FragmentFolderBinding;
import ua.moysa.meewfilemanager.view.FilesAdapter;
import ua.moysa.meewfilemanager.view.FilesViewModel;
import ua.moysa.meewfilemanager.view.FilesViewModelProviderFactory;

/**
 * Created by Sergey Moysa
 */
public class FolderFragment extends Fragment {

    public static final String FRAG_TAG = "fragment_folder.tag";
    private static final String PARENT_KEY = "fragment_folder.parent";
    private static final String MULTISELECT_KEY = "fragment_folder.multiselect";
    private FragmentFolderBinding mBinding;

    private File mParent;

    private FilesViewModel mViewModel;

    private FilesAdapter mAdapter;

    private MultiSelector mMultiSelector = new MultiSelector();

    private ModalMultiSelectorCallback mActionModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);

            getActivity().getMenuInflater().inflate(R.menu.menu_files_action_mode, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getItemId() == R.id.action_delete) {
                showDeletionConfirmation(mode);
                return true;
            }
            return false;
        }

        private void showDeletionConfirmation(ActionMode mode) {

            new AlertDialog.Builder(getContext())
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteSelection(mode))
                    .setNegativeButton("No", null)
                    .show();
        }

        private void deleteSelection(ActionMode mode) {
            mode.finish();

            List<File> toDelete = new ArrayList<>();

            for (int i = mAdapter.getItemCount(); i >= 0; i--) {

                if (mMultiSelector.isSelected(i, 0)) {

                    File file = mAdapter.getItem(i);
                    if (file != null) {
                        toDelete.add(file);
                    }
                }
            }
            mViewModel.delete(toDelete.toArray(new File[toDelete.size()]));
            mMultiSelector.clearSelections();
        }
    };

    public FolderFragment() {
    }

    //taken from
    // https://stackoverflow.com/questions/6265298/action-view-intent-for-a-file-with-unknown-mimetype
    private static String getMimeType(@NonNull String url) {

        String extension = url.substring(url.lastIndexOf(".") + 1);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_folder, container, false);

        mBinding = DataBindingUtil.bind(root);

        fillParentFolder(savedInstanceState);

        FilesViewModelProviderFactory factory = new FilesViewModelProviderFactory(mParent);
        mViewModel = ViewModelProviders.of(this, factory).get(FilesViewModel.class);

        initMultiSelect(savedInstanceState);
        initRecycler();

        loadFiles();

        getActivity().setTitle(mParent.getName());

        return root;
    }

    private void fillParentFolder(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();

        if (bundle != null) {
            mParent = (File) bundle.getSerializable(PARENT_KEY);
        }

        if (mParent == null) {
            //TODO use preferences for this
            mParent = Environment.getExternalStorageDirectory();
        }
    }

    private void initMultiSelect(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //noinspection ConstantConditions
            mMultiSelector.restoreSelectionStates(savedInstanceState.getBundle(MULTISELECT_KEY));
        }
        if (mMultiSelector.isSelectable()) {
            startMultiSelection();
        }
    }

    private void initRecycler() {
        mAdapter = new FilesAdapter(null, mMultiSelector);

        if (getActivity().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {

            mBinding.filesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        } else {
            mBinding.filesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }


        mBinding.filesRecycler.setItemAnimator(new DefaultItemAnimator());

        mBinding.filesRecycler.setAdapter(mAdapter);

        mAdapter.setOnClickListener(this::selectFile);
        mAdapter.setOnMultiSelectStartListener(this::startMultiSelection);
    }

    private ActionMode startMultiSelection() {
        return ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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

        if (file.exists() && file.canRead()) {

            if (file.isDirectory()) {
                openDirectory(file);
            } else {
                openFile(file);
            }
        }
    }

    private void openFile(@NonNull File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), getMimeType(file.getAbsolutePath()));
        startActivity(intent);
    }

    private void openDirectory(@NonNull File file) {
        Bundle args = new Bundle();
        args.putSerializable(PARENT_KEY, file);

        getFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment,
                        Fragment.instantiate(
                                getContext(), FolderFragment.class.getName(), args),
                        FRAG_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setTransitionStyle(android.R.style.Animation_Activity)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(PARENT_KEY, mParent);
        outState.putBundle(MULTISELECT_KEY, mMultiSelector.saveSelectionStates());
    }
}
