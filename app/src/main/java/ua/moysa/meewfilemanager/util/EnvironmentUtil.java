package ua.moysa.meewfilemanager.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Sergey Moysa
 */

public class EnvironmentUtil {

    @NonNull
    private Context mContext;

    private EnvironmentUtil(@NonNull Context context) {
        this.mContext = context;
    }

    @NonNull
    public static EnvironmentUtil from(@NonNull Context context) {
        return new EnvironmentUtil(context);
    }

    @NonNull
    public File getRemovableStorageDir() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getRemovableStorageNew();
        } else {
            return getRemovableStorageOld();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    private File getRemovableStorageNew() {
        File[] dirs = mContext.getExternalFilesDirs(null);

        for (File dir : dirs) {
            if (dir.exists() &&
                    Environment.getExternalStorageState(dir)
                            .equals(Environment.MEDIA_MOUNTED) &&
                    Environment.isExternalStorageRemovable(dir)) {

                File file = new File(dir.getPath().split("/Android")[0]);

                if (file.exists() && file.canRead()) {
                    return file;
                }
            }
        }
        return Environment.getExternalStorageDirectory();
    }

    //taken from https://stackoverflow.com/questions/5694933/find-an-external-sd-card-location
    @NonNull
    private File getRemovableStorageOld() {
        String sSDpath = null;
        File fileCur = null;
        for (String sPathCur : Arrays.asList("ext_card", "external_sd", "ext_sd", "external", "extSdCard", "externalSdCard")) // external sdcard
        {
            fileCur = new File("/mnt/", sPathCur);
            if (fileCur.isDirectory() && fileCur.canWrite()) {
                sSDpath = fileCur.getAbsolutePath();
                break;
            }
        }
        fileCur = null;
        if (sSDpath == null) sSDpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        return new File(sSDpath);
    }
}
