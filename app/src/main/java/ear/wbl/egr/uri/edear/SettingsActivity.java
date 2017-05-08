package ear.wbl.egr.uri.edear;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.ref.WeakReference;

import ear.wbl.egr.uri.edear.fragments.SettingsFragment;
import ear.wbl.egr.uri.edear.interfaces.AnEarMessenger;
import wbl.egr.uri.anear.band.tasks.RequestHeartRateTask;

public class SettingsActivity extends AppCompatActivity implements AnEarMessenger {
    private CoordinatorLayout mMessageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageContainer = (CoordinatorLayout) findViewById(R.id.message_container);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissions, 435);
        }
        new RequestHeartRateTask().execute(new WeakReference<Activity>(this));
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(mMessageContainer, message, Snackbar.LENGTH_INDEFINITE).show();
    }
}
