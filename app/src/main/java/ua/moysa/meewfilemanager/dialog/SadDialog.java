package ua.moysa.meewfilemanager.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ua.moysa.meewfilemanager.R;

/**
 * Created by Sergey Moysa
 */

public class SadDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(R.string.dialog_message_sad)
                .setCancelable(false)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    if (getActivity() instanceof OnSadDialogInteractionListener) {
                        ((OnSadDialogInteractionListener) getActivity())
                                .onSadDialogPositiveClick();
                    }
                    SadDialog.this.dismiss();
                })
                .setNegativeButton(R.string.button_no, (dialog, which) -> {
                    if (getActivity() instanceof OnSadDialogInteractionListener) {
                        ((OnSadDialogInteractionListener) getActivity())
                                .onSadDialogNegativeClick();
                    }
                    SadDialog.this.dismiss();
                })
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (getActivity() instanceof OnSadDialogInteractionListener) {
            ((OnSadDialogInteractionListener) getActivity()).onSadDialogDismiss();
        }
    }

    public interface OnSadDialogInteractionListener {
        void onSadDialogPositiveClick();

        void onSadDialogNegativeClick();

        void onSadDialogDismiss();
    }
}
