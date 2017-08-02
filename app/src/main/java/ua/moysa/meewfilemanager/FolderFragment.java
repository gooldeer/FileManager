package ua.moysa.meewfilemanager;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;

import java.io.File;

import io.reactivex.Observable;
import ua.moysa.meewfilemanager.data.response.Response;
import ua.moysa.meewfilemanager.databinding.FragmentFolderBinding;
import ua.moysa.meewfilemanager.util.SettingsUtil;
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

            Observable.fromArray(mAdapter.getItems())
                    .filter(file -> mMultiSelector.isSelected(mAdapter.getItemPosition(file), 0))
                    .toList()
                    .map(files -> files.toArray(new File[files.size()]))
                    .subscribe(files -> mViewModel.delete(files));

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

        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_folder, container, false);

        mBinding = DataBindingUtil.bind(root);

        fillParentFolder(savedInstanceState);

        FilesViewModelProviderFactory factory = new FilesViewModelProviderFactory(mParent);
        mViewModel = ViewModelProviders.of(this, factory).get(FilesViewModel.class);

        initMultiSelect(savedInstanceState);
        initRecycler();

        mBinding.filesContent.setOnRefreshListener(this::loadFiles);

        loadFiles();

        return root;
    }

    private void fillParentFolder(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
        String title = null;

        if (bundle != null) {
            mParent = (File) bundle.getSerializable(PARENT_KEY);

            if (mParent != null && !TextUtils.isEmpty(mParent.getName())) {
                title = mParent.getName();
            }
        }
        if (mParent == null) {
            mParent = SettingsUtil.from(getContext()).getDefaultFolderFile();
        }
        if (TextUtils.isEmpty(title)) {
            title = getString(obtainDefaultTitle());
        }
        getActivity().setTitle(title);
    }

    @StringRes
    private int obtainDefaultTitle() {

        switch (SettingsUtil.from(getContext()).getDefaultFolder()) {

            case SettingsUtil.FOLDER_INTERNAL:
                return R.string.folder_internal;
            case SettingsUtil.FOLDER_ROOT:
                return R.string.folder_root;
            case SettingsUtil.FOLDER_SDCARD:
                return R.string.folder_sdcard;
            default:
                return R.string.folder_internal;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_refresh) {
            mBinding.filesContent.setRefreshing(true);
            loadFiles();

            return true;
        } else if (item.getItemId() == R.id.menu_home) {

            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            openDirectory(null, false);
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFiles() {
        mViewModel
                .listFiles(mParent)
                .observe((LifecycleOwner) getActivity(), this::onFilesLoad);
    }

    private void onFilesLoad(Response<File[]> response) {
        if (response.getStatus() == Response.Status.SUCCESS) {
            mBinding.filesContent.setRefreshing(false);
            mAdapter.setItems(response.getData());
        } else if (response.getStatus() == Response.Status.ERROR) {
            mBinding.filesContent.setRefreshing(false);
            Snackbar
                    .make(
                            mBinding.filesContent,
                            R.string.snack_files_load_error,
                            Snackbar.LENGTH_SHORT)
                    .show();
        } else if (response.getStatus() == Response.Status.LOADING) {
            if (!mBinding.filesContent.isRefreshing()) {
                mBinding.filesContent.setRefreshing(true);
            }
        }
    }

    private void selectFile(@NonNull File file) {

        if (file.exists() && file.canRead()) {

            if (file.isDirectory()) {
                openDirectory(file, true);
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

    private void openDirectory(@Nullable File file, boolean withBackStack) {
        Bundle args = new Bundle();
        args.putSerializable(PARENT_KEY, file);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment,
                        Fragment.instantiate(
                                getContext(), FolderFragment.class.getName(), args),
                        FRAG_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setTransitionStyle(android.R.style.Animation_Activity);

        if (withBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(PARENT_KEY, mParent);
        outState.putBundle(MULTISELECT_KEY, mMultiSelector.saveSelectionStates());
    }
}
