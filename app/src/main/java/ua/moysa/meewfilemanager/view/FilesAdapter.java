package ua.moysa.meewfilemanager.view;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.io.File;

import ua.moysa.meewfilemanager.R;
import ua.moysa.meewfilemanager.databinding.ListItemFileBinding;

/**
 * Created by Sergey Moysa
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    @NonNull
    private File[] mItems;

    @NonNull
    private MultiSelector mMultiSelector;

    @NonNull
    private OnFileClickListener mOnClickListener = file -> {
        //Dump
    };

    @NonNull
    private OnMultiSelectStartListener mOnMultiSelectStartListener = () -> {
        //Dump
    };

    public FilesAdapter(@Nullable File[] items, @NonNull MultiSelector multiSelector) {
        this.mItems = items == null ? new File[0] : items;
        mMultiSelector = multiSelector;
    }

    public void setItems(@Nullable File[] items) {
        this.mItems = items == null ? new File[0] : items;

        notifyDataSetChanged();
    }

    public void setOnClickListener(@NonNull OnFileClickListener onFileClickListener) {
        this.mOnClickListener = onFileClickListener;
    }

    public void setOnMultiSelectStartListener(@NonNull OnMultiSelectStartListener onMultiSelectStartListener) {
        this.mOnMultiSelectStartListener = onMultiSelectStartListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_file, parent, false);

        return new FileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof FileViewHolder) {

            ((FileViewHolder) holder).mBinding.setFile(mItems[position]);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    @Nullable
    public File getItem(int position) {

        if (position >= 0 && position < mItems.length) {
            return mItems[position];
        }
        return null;
    }

    public interface OnFileClickListener {
        void onFileClick(@NonNull File file);
    }

    public interface OnMultiSelectStartListener {
        void onMultiSelectStarted();
    }

    static class ViewHolder extends SwappingHolder {

        ViewHolder(View itemView, MultiSelector multiSelector) {
            super(itemView, multiSelector);
        }
    }

    private class FileViewHolder extends ViewHolder implements View.OnLongClickListener, OnFileClickListener {

        private ListItemFileBinding mBinding;

        FileViewHolder(View itemView) {
            super(itemView, mMultiSelector);

            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);

            mBinding = DataBindingUtil.bind(itemView);
            mBinding.setPresenter(this);
        }

        @Override
        public boolean onLongClick(View v) {

            if (!mMultiSelector.isSelectable()) {
                mOnMultiSelectStartListener.onMultiSelectStarted();
                mMultiSelector.setSelectable(true);
                mMultiSelector.setSelected(this, true);
                return true;
            }
            return false;
        }

        @Override
        public void onFileClick(@NonNull File file) {
            if (!mMultiSelector.tapSelection(this)) {
                mOnClickListener.onFileClick(file);
            }
        }
    }
}
