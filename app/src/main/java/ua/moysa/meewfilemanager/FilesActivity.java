package ua.moysa.meewfilemanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class FilesActivity extends BaseLifecycleActivity {

    public static final int PERMISSION_REQUEST_CODE = 982;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {

        //TODO at first show funny dialog with explanation

        new AlertDialog.Builder(this)
                .setMessage("This is file manager. It needs a permission to read / write files")
                .setPositiveButton("Okay", (dialog, which) -> requestNecessaryPermissions())
                .setNegativeButton("I don't think so", (dialog, which) -> finish());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestNecessaryPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {

            boolean grantResult = true;

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                        grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                    grantResult = false;
                }
            }

            if (!grantResult) {
                showSadDialog();
            }
        }
    }

    private void showSadDialog() {
        //TODO do better
        new AlertDialog.Builder(this)
                .setMessage("Very sad:( Try again?")
                .setPositiveButton("Yes", (dialog, which) -> checkPermissions())
                .setNegativeButton("No", (dialog, which) -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
