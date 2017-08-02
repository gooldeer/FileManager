package ua.moysa.meewfilemanager.util;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ua.moysa.meewfilemanager.R;

/**
 * Created by Sergey Moysa
 */

public class SettingsUtil {

    public static final int FOLDER_ROOT = 1;
    public static final int FOLDER_SDCARD = 0;
    public static final int FOLDER_INTERNAL = -1;
    @NonNull
    private Context mContext;

    private SettingsUtil(@NonNull Context context) {
        this.mContext = context;
    }

    @NonNull
    public static SettingsUtil from(@NonNull Context context) {
        return new SettingsUtil(context);
    }

    @SettingsUtil.DefaultFolder
    public int getDefaultFolder() {
        //noinspection WrongConstant
        return Integer.valueOf(
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(
                                mContext.getString(R.string.pref_key_default_folder),
                                String.valueOf(FOLDER_INTERNAL)
                        ));
    }

    @NonNull
    public File getDefaultFolderFile() {

        switch (getDefaultFolder()) {

            case FOLDER_INTERNAL:
                return Environment.getExternalStorageDirectory();
            case FOLDER_ROOT:
                return new File("/");
            case FOLDER_SDCARD:
                return EnvironmentUtil.from(mContext).getRemovableStorageDir();
            default:
                return Environment.getExternalStorageDirectory();

        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FOLDER_ROOT, FOLDER_SDCARD, FOLDER_INTERNAL})
    public @interface DefaultFolder {
    }
}
