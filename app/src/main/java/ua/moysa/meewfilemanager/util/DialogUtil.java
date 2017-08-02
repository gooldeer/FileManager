package ua.moysa.meewfilemanager.util;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Sergey Moysa
 */

public class DialogUtil {

    public static final String DIALOG_TAG = "dialog";

    @NonNull
    private AppCompatActivity mActivity;

    private DialogUtil(@NonNull AppCompatActivity activity) {
        this.mActivity = activity;
    }

    @NonNull
    public static DialogUtil from(@NonNull AppCompatActivity activity) {
        return new DialogUtil(activity);
    }

    public void showDialog(DialogFragment dialog, boolean addToBackStack) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        dialog.show(ft, DIALOG_TAG);
    }
}
