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

public class DeleteConfirmationDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(R.string.alert_message_delete_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {

                    if (getTargetFragment() != null && getTargetFragment() instanceof OnDeleteConfirmationInteractionListener) {
                        ((OnDeleteConfirmationInteractionListener) getTargetFragment())
                                .onDeleteConfirmationPositiveClick();
                    }
                    DeleteConfirmationDialog.this.dismiss();
                })
                .setNegativeButton(R.string.button_no, (dialog, which) -> {
                    if (getTargetFragment() != null && getTargetFragment() instanceof OnDeleteConfirmationInteractionListener) {
                        ((OnDeleteConfirmationInteractionListener) getActivity())
                                .onDeleteConfirmationNegativeClick();
                    }
                    DeleteConfirmationDialog.this.dismiss();
                })
                .show();
    }

    public interface OnDeleteConfirmationInteractionListener {
        void onDeleteConfirmationPositiveClick();

        void onDeleteConfirmationNegativeClick();
    }
}
