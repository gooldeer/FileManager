package ua.moysa.meewfilemanager.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ua.moysa.meewfilemanager.R;

/**
 * Created by Sergey Moysa
 */

public class RequestPermissionDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setMessage(R.string.dialog_message_permission_dialog)
                .setPositiveButton(R.string.button_okay, (dialog, which) -> {
                    if (getActivity() instanceof PermissionDialogInteractionListener) {
                        ((PermissionDialogInteractionListener) getActivity())
                                .onPermissionDialogPositiveClick();
                    }
                    RequestPermissionDialog.this.dismiss();
                })
                .setNegativeButton(R.string.button_dont_think_so, (dialog, which) -> {
                    if (getActivity() instanceof PermissionDialogInteractionListener) {
                        ((PermissionDialogInteractionListener) getActivity())
                                .onPermissionDialogNegativeClick();
                    }
                    RequestPermissionDialog.this.dismiss();
                })
                .create();
    }

    public interface PermissionDialogInteractionListener {
        void onPermissionDialogPositiveClick();

        void onPermissionDialogNegativeClick();
    }
}
