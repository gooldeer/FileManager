package ua.moysa.meewfilemanager.data.repo;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Sergey Moysa
 */

public class FilesRepository {

    @WorkerThread
    @NonNull
    public File[] obtainFilesList(@NonNull File parent) {

        if (parent.exists() &&
                parent.canRead() &&
                parent.isDirectory()) {

            File[] files = parent.listFiles();

            if (files != null) {

                Arrays.sort(files, (o1, o2) -> {
                    if (o1.isDirectory())
                        return o2.isDirectory() ? o1.compareTo(o2) : -1;
                    else if (o2.isDirectory())
                        return 1;

                    return o1.compareTo(o2);
                });
                return files;
            }
        }
        throw new IllegalArgumentException();
    }

    @WorkerThread
    public boolean delete(@NonNull File file) {

        if (file.exists() && file.isDirectory() && file.canWrite() && file.canExecute()) {

            File[] files = file.listFiles();

            if (files != null)
                for (File f : files) delete(f);
        }
        return file.delete();
    }
}
