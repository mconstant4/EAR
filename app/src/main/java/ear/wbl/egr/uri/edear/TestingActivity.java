package ear.wbl.egr.uri.edear;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import ear.wbl.egr.uri.edear.fragments.TestingFragment;
import ear.wbl.egr.uri.edear.interfaces.AnEarMessenger;

/**
 * Created by mconstant on 5/9/17.
 */

public class TestingActivity extends AppCompatActivity implements AnEarMessenger {

    private CoordinatorLayout mMessageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageContainer = (CoordinatorLayout) findViewById(R.id.message_container);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, TestingFragment.getInstance(), "testing_fragment")
                .addToBackStack("testing_fragment")
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(mMessageContainer, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }
}
