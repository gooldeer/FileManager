package ua.moysa.meewfilemanager;

import android.arch.lifecycle.LifecycleFragment;
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
import ua.moysa.meewfilemanager.dialog.DeleteConfirmationDialog;
import ua.moysa.meewfilemanager.util.DialogUtil;
import ua.moysa.meewfilemanager.util.SettingsUtil;
import ua.moysa.meewfilemanager.view.FilesAdapter;
import ua.moysa.meewfilemanager.view.FilesViewModel;
import ua.moysa.meewfilemanager.view.FilesViewModelProviderFactory;

/**
 * Created by Sergey Moysa
 */
public class FolderFragment extends LifecycleFragment implements DeleteConfirmationDialog.OnDeleteConfirmationInteractionListener {

    public static final String FRAG_TAG = "fragment_folder.tag";
    public static final int DELETE_REQUEST_CODE = 456;
    private static final String PARENT_KEY = "fragment_folder.parent";
    private static final String MULTISELECT_KEY = "fragment_folder.multiselect";
    private FragmentFolderBinding mBinding;

    private File mParent;

    private FilesViewModel mViewModel;

    private FilesAdapter mAdapter;

    private MultiSelector mMultiSelector = new MultiSelector();
    private FilesMultiSelectorCallback mActionModeCallback = new FilesMultiSelectorCallback(mMultiSelector);

    private boolean mAlreadyLoaded;

    public FolderFragment() {
    }

    //taken from
    // https://stackoverflow.com/questions/6265298/action-view-intent-for-a-file-with-unknown-mimetype
    private static String getMimeType(@NonNull String url) {

        String extension = url.substring(url.lastIndexOf(".") + 1);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public void onDeleteConfirmationPositiveClick() {
        mActionModeCallback.deleteSelections();
    }

    @Override
    public void onDeleteConfirmationNegativeClick() {

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

        mViewModel.getLiveData().observe(this, this::onFilesLoad);

        restoreMultiSelect(savedInstanceState);
        initRecycler();

        mBinding.filesContent.setOnRefreshListener(this::loadFiles);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if ((savedInstanceState == null && !mAlreadyLoaded)
                || mViewModel.getLiveData().getValue() == null) {

            mAlreadyLoaded = true;

            loadFiles();
        }
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
        File defaultFolderFile = SettingsUtil.from(getContext()).getDefaultFolderFile();
        if (mParent == null) {
            mParent = defaultFolderFile;
        }
        if (title == null || mParent.getPath().equals(defaultFolderFile.getPath())) {
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

    @SuppressWarnings("ConstantConditions")
    private void restoreMultiSelect(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMultiSelector.restoreSelectionStates(savedInstanceState.getBundle(MULTISELECT_KEY));

            if (mMultiSelector.isSelectable()) {
                startMultiSelection();
                mMultiSelector.restoreSelectionStates(savedInstanceState.getBundle(MULTISELECT_KEY));
            }
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
        mViewModel.listFiles(mParent);
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

        outState.putSerializable(PARENT_KEY, mParent);
        outState.putBundle(MULTISELECT_KEY, mMultiSelector.saveSelectionStates());

        super.onSaveInstanceState(outState);
    }

    private class FilesMultiSelectorCallback extends ModalMultiSelectorCallback {

        private ActionMode mMode;

        public FilesMultiSelectorCallback(MultiSelector multiSelector) {
            super(multiSelector);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);

            mMode = actionMode;

            getActivity().getMenuInflater().inflate(R.menu.menu_files_action_mode, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            mMode = mode;

            if (item.getItemId() == R.id.action_delete) {
                DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
                dialog.setTargetFragment(FolderFragment.this, DELETE_REQUEST_CODE);
                DialogUtil.from((AppCompatActivity) getActivity())
                        .showDialog(dialog, true);
                return true;
            }
            return false;
        }

        private void deleteSelections() {
            mMode.finish();

            Observable.fromArray(mAdapter.getItems())
                    .filter(file -> mMultiSelector.isSelected(mAdapter.getItemPosition(file), 0))
                    .toList()
                    .map(files -> files.toArray(new File[files.size()]))
                    .subscribe(files -> mViewModel.delete(files));

            mMultiSelector.clearSelections();
        }
    }
}
