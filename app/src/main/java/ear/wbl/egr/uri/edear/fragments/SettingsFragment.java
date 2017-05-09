package ear.wbl.egr.uri.edear.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.microsoft.band.BandInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ear.wbl.egr.uri.edear.R;
import ear.wbl.egr.uri.edear.enums.Preference;
import ear.wbl.egr.uri.edear.interfaces.AnEarMessenger;
import wbl.egr.uri.anear.AnEar;
import wbl.egr.uri.anear.audio.enums.AudioState;
import wbl.egr.uri.anear.audio.receivers.AudioStateReceiver;
import wbl.egr.uri.anear.audio.services.AudioRecorderService;
import wbl.egr.uri.anear.band.enums.BandSensor;
import wbl.egr.uri.anear.band.enums.BandState;
import wbl.egr.uri.anear.band.receivers.BandStateReceiver;
import wbl.egr.uri.anear.band.services.BandCollectionService;
import wbl.egr.uri.anear.models.AudioObject;
import wbl.egr.uri.anear.models.AudioStorageObject;
import wbl.egr.uri.anear.models.BandObject;
import wbl.egr.uri.anear.models.CsvObject;
import wbl.egr.uri.anear.models.StorageObject;
import wbl.egr.uri.anear.models.WavObject;

/**
 * Created by mconstant on 5/8/17.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private WeakReference<Context> mContext;
    private SharedPreferences mSharedPreferences;

    private BandStateReceiver mBandStateReceiver = new BandStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BandState bandState = (BandState) intent.getSerializableExtra(BandStateReceiver.EXTRA_STATE);

            switch (bandState) {
                case INITIALIZED:
                    BandCollectionService.connect(mContext.get());
                    break;
                case CONNECTED:
                    // Auto Stream is enabled, so streaming is automatically started
                    break;
                case STREAMING:
                    if (getActivity() instanceof AnEarMessenger) {
                        ((AnEarMessenger) getActivity()).showMessage("Band Streaming");
                    }
            }
        }
    };

    private AudioStateReceiver mAudioStateReceiver = new AudioStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AudioState audioState = (AudioState) intent.getSerializableExtra(AudioStateReceiver.EXTRA_STATE);

            switch (audioState) {
                case INITIALIZED:
                    AudioRecorderService.start(mContext.get());
                    break;
                case RECORDING:
                    if (getActivity() instanceof AnEarMessenger) {
                        ((AnEarMessenger) getActivity()).showMessage("Device Recording");
                    }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = new WeakReference<Context>(context);

        context.registerReceiver(mBandStateReceiver, BandStateReceiver.INTENT_FILTER);
        context.registerReceiver(mAudioStateReceiver, AudioStateReceiver.INTENT_FILTER);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = new WeakReference<Context>(activity);

        activity.registerReceiver(mBandStateReceiver, BandStateReceiver.INTENT_FILTER);
        activity.registerReceiver(mAudioStateReceiver, AudioStateReceiver.INTENT_FILTER);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        mContext = new WeakReference<Context>(getActivity());

        Context context = mContext.get();

        context.registerReceiver(mBandStateReceiver, BandStateReceiver.INTENT_FILTER);
        context.registerReceiver(mAudioStateReceiver, AudioStateReceiver.INTENT_FILTER);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mContext != null) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            mContext.get().unregisterReceiver(mBandStateReceiver);
            mContext.get().unregisterReceiver(mAudioStateReceiver);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.clear();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = Preference.lookup(key);
        if (preference == null) {
            return;
        }
        switch (preference) {
            case SENSOR_ENABLE:
                if (sharedPreferences.getBoolean(key, false)) {
                    // Configure Band Object
                    BandSensor[] sensors = {
                            BandSensor.ACCELEROMETER,
                            BandSensor.AMBIENT_LIGHT,
                            BandSensor.CONTACT,
                            BandSensor.GSR,
                            BandSensor.HEART_RATE,
                            BandSensor.RR_INTERVAL,
                            BandSensor.SKIN_TEMPERATURE
                    };
                    BandInfo microsoftBand = BandObject.getPairedBands()[0];
                    BandObject bandObject = new BandObject(microsoftBand);
                    bandObject.enablePeriodic(sharedPreferences.getBoolean(Preference.SENSOR_PERIODIC.toString(), false));
                    bandObject.setSensorsToRecord(sensors);
                    bandObject.enableHapticFeedback(true);
                    bandObject.enableAutoStream(true);

                    // Configure Storage Object
                    StorageObject csvObject;
                    if (mContext != null) {
                        csvObject = new CsvObject(AnEar.getRoot(mContext.get()));
                    } else {
                        Log.d("ED EAR", "Could not Configure CSV Object (Null Context)");
                        return;
                    }

                    // Initialize Band Collection Service
                    BandCollectionService.initialize(mContext.get(), bandObject, csvObject);
                } else {
                    if (mContext != null) {
                        BandCollectionService.stopService(mContext.get());
                    } else {
                        Log.d("ED EAR", "Could not Stop Band Collection Service (Null Context)");
                        return;
                    }
                }
                break;
            case AUDIO_ENABLE:
                if (sharedPreferences.getBoolean(key, false)) {
                    // Configure Audio Object
                    int duration = Integer.parseInt(sharedPreferences.getString(Preference.AUDIO_DURATION.toString(), "30"));
                    int delay = Integer.parseInt(sharedPreferences.getString(Preference.AUDIO_DELAY.toString(), "12"));
                    AudioObject audioObject = new AudioObject(duration * 1000); // Duration Units are Seconds
                    if (delay > 0) {
                        audioObject.enablePeriodic(delay * 60 * 1000); // Delay Units are Minutes
                    } else if (delay < 0) {
                        Log.e("ED EAR", "Invalid Delay Time!!!");
                    }
                    audioObject.enableLog(true);

                    // Configure Audio Storage Object
                    File destination = new File(AnEar.getRoot(mContext.get()), "audio.wav"); // Filename will be replaced since we are providing a TimeFormat to the WavObject
                    AudioStorageObject wavObject = new WavObject(destination, "MM_dd_yyyy_kk.mm.ss");

                    // Initialize Audio Recorder Service
                    AudioRecorderService.initialize(mContext.get(), audioObject, wavObject);
                } else {
                    if (mContext != null) {
                        AudioRecorderService.destroy(mContext.get());
                    } else {
                        Log.d("ED EAR", "Could not Stop Audio Recorder Service (Null Context)");
                        return;
                    }
                }
                break;
            case PATIENT_ID:
                String pid = sharedPreferences.getString(key, null);
                AnEar.setRoot(mContext.get(), pid);
                break;
        }
    }
}
