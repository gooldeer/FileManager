package ua.moysa.meewfilemanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ua.moysa.meewfilemanager.dialog.RequestPermissionDialog;
import ua.moysa.meewfilemanager.dialog.SadDialog;
import ua.moysa.meewfilemanager.util.DialogUtil;

public class FilesActivity extends BaseLifecycleActivity implements
        FragmentManager.OnBackStackChangedListener,
        SadDialog.OnSadDialogInteractionListener,
        RequestPermissionDialog.PermissionDialogInteractionListener {

    public static final int PERMISSION_REQUEST_CODE = 982;

    private boolean mShowPermissions = true;
    private boolean mShowSadDialog = false;

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
            } else if (mShowSadDialog) {
                showSadDialog();
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
        DialogUtil.from(this).showDialog(new RequestPermissionDialog(), false);
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
                mShowSadDialog = true;
            }
        }
    }

    private void showSadDialog() {
        mShowSadDialog = false;
        DialogUtil.from(this).showDialog(new SadDialog(), false);
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

            startActivity(new Intent(this, SettingsActivity.class));
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

    @Override
    public void onSadDialogPositiveClick() {
        mShowPermissions = true;
    }

    @Override
    public void onSadDialogNegativeClick() {
        finish();
    }

    @Override
    public void onSadDialogDismiss() {
        if (mShowPermissions) {
            resume();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onPermissionDialogPositiveClick() {
        requestNecessaryPermissions();
    }

    @Override
    public void onPermissionDialogNegativeClick() {
        showSadDialog();
    }
}
