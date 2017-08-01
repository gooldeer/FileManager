package ua.moysa.meewfilemanager.view;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private OnFileClickListener mOnClickListener = file -> {
        //Dump
    };

    public FilesAdapter(@Nullable File[] items) {
        this.mItems = items == null ? new File[0] : items;
    }

    public void setItems(@Nullable File[] items) {
        this.mItems = items == null ? new File[0] : items;

        notifyDataSetChanged();
    }

    public void setOnClickListener(@NonNull OnFileClickListener onFileClickListener) {
        this.mOnClickListener = onFileClickListener;
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

    public interface OnFileClickListener {
        void onFileClick(@NonNull File file);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class FileViewHolder extends ViewHolder {

        private ListItemFileBinding mBinding;

        public FileViewHolder(View itemView) {
            super(itemView);

            mBinding = DataBindingUtil.bind(itemView);

            mBinding.setPresenter(mOnClickListener);
        }
    }
}
