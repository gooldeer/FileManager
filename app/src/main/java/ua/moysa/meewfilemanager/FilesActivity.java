package ua.moysa.meewfilemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class FilesActivity extends BaseLifecycleActivity implements FragmentManager.OnBackStackChangedListener {

    public static final int PERMISSION_REQUEST_CODE = 982;

    private boolean mShowPermissions = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        shouldDisplayHomeUp();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        resume();
    }

    private void resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermissions()) {
            if (mShowPermissions) {
                showPermissionsRequestDialog();
            }
        } else {
            instantiateFragment();
        }
    }

    private void instantiateFragment() {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.findFragmentByTag(FolderFragment.FRAG_TAG) == null) {

            manager
                    .beginTransaction()
                    .replace(R.id.fragment, new FolderFragment(), FolderFragment.FRAG_TAG)
                    .commit();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissions() {

        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showPermissionsRequestDialog() {
        //TODO at first show funny dialog with explanation
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("This is file manager. It needs a permission to read / write files")
                .setPositiveButton("Okay", (dialog, which) -> requestNecessaryPermissions())
                .setNegativeButton("I don't think so", (dialog, which) -> showSadDialog())
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestNecessaryPermissions() {
        requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE
        );
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
                mShowPermissions = false;
                showSadDialog();
            }
        }
    }

    private void showSadDialog() {
        //TODO do better
        new AlertDialog.Builder(this)
                .setMessage("Very sad:( Try again?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    mShowPermissions = true;
                    resume();
                })
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
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

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp() {
        boolean goBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(goBack);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }
}
