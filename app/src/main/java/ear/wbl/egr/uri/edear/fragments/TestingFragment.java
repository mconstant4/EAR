package ear.wbl.egr.uri.edear.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ear.wbl.egr.uri.edear.R;
import wbl.egr.uri.anear.AnEar;
import wbl.egr.uri.anear.io.services.CsvLogService;

/**
 * Created by mconstant on 5/9/17.
 */

public class TestingFragment extends Fragment {
    protected Button mTestButton;

    public static Fragment getInstance() {
        return new TestingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_testing, container, false);

        mTestButton = (Button) view.findViewById(R.id.test_button);
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean sensorsPassed = sensorTest();
                boolean audioPassed = audioTest();

                Date currentTime = Calendar.getInstance().getTime();
                final String header = "Date,Time,Result";
                final String date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(currentTime);
                final String time = new SimpleDateFormat("kk:mm:ss", Locale.US).format(currentTime);
                final String[] contents;

                if (sensorsPassed && audioPassed) {
                    // Success

                    // Update UI
                    contents = new String[] {date, time, "success"};

                    final MaterialDialog resultDialog = new MaterialDialog.Builder(getActivity())
                            .title("Success!")
                            .customView(R.layout.view_test_success, true)
                            .positiveText("OK")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    remindToChargeBand();
                                }
                            })
                            .autoDismiss(true)
                            .canceledOnTouchOutside(false)
                            .show();
                } else if (!sensorsPassed && !audioPassed) {
                    // Both Failed

                    // Update UI
                    contents = new String[] {date, time, "failed"};

                    new MaterialDialog.Builder(getActivity())
                            .title("Test Failed")
                            .customView(R.layout.view_test_failed, true)
                            .negativeText("Restart")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // Restart
                                }
                            })
                            .canceledOnTouchOutside(false)
                            .show();
                } else if (!sensorsPassed) {
                    // Sensors Failed

                    // Update UI
                    contents = new String[] {date, time, "sensors failed"};

                    new MaterialDialog.Builder(getActivity())
                            .title("Test Failed")
                            .customView(R.layout.view_test_sensors_fail, true)
                            .negativeText("Restart")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // Restart BandCollectionService
                                }
                            })
                            .canceledOnTouchOutside(false)
                            .show();
                } else {
                    // Audio Failed

                    // Update UI
                    contents = new String[] {date, time, "audio failed"};

                    new MaterialDialog.Builder(getActivity())
                            .title("Test Failed")
                            .customView(R.layout.view_test_audio_fail, true)
                            .negativeText("Restart")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // Restart AudioRecorderService
                                }
                            })
                            .canceledOnTouchOutside(false)
                            .show();
                }

                CsvLogService.logData(getActivity(), new File(AnEar.getRoot(getActivity()), "test_log.csv"), header, CsvLogService.generateContents(contents));
            }
        });


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Restart Algorithm
    // Problems:
    //      - Does not work if BandCollectionService isn't already initialized
    //      - Does not check if connection successful
                /*
                BandStateReceiver bandStateReceiver = new BandStateReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        BandState bandState = intent.getSerializableExtra(BandStateReceiver.EXTRA_STATE);
                        switch (bandState) {
                            case INITIALIZED:
                                BandCollectionService.connect(context);
                                break;
                        }
                    }
                }

                registerReceiver(bandStateReceiver);
                BandCollectionService.disconnect(context);
                 */

    private boolean sensorTest() {
        // Sensor Test Algorithm
        // Problems:
        //      - Does not work when Band is not being worn
        //      - Will pass if no files have been created
        //      - Have to manually exclude other CSV files (i.e. AudioRecordLog.csv)

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, Calendar.MINUTE - 3);
        Date currentTimeMinus3 = calendar.getTime();

        File root = AnEar.getRoot(getActivity());
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".csv") && !name.equals("AudioRecordLog.csv") && !name.equals("charge.csv"));
            }
        };
        for (File file : root.listFiles(filter)) {
            Date date1 = new Date(file.lastModified());
            Log.d("LASTMOD", file.getName() + ": " + date1);

            if (date1.before(currentTimeMinus3)) {
                // Fail
                return false;
            }
        }

        // All Sensors Passed
        return true;
    }

    private boolean audioTest() {
        // Audio Test Algorithm
        // Problems:
        //      - Does not take Blackout Times into account
        //      - Will pass if no files have been created

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, Calendar.MINUTE - 12);
        Date currentTimeMinus12 = calendar.getTime();

        File root = AnEar.getRoot(getActivity());
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".wav");
            }
        };
        for (File file : root.listFiles(filter)) {
            Date date1 = new Date(file.lastModified());
            Log.d("LASTMOD", file.getName() + ": " + date1);

            if (date1.after(currentTimeMinus12)) {
                // Success
                return true;
            }
        }

        // No Recordings in the last 12 minutes
        return false;
    }

    private void remindToChargeBand() {
        new MaterialDialog.Builder(getActivity())
                .title("Please charge your Band and keep charging your device!")
                .positiveText("OK")
                .show();
    }

    /*private void test() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int audioResult = checkAudio();
                        int sensorResult = checkSensors();

                        if (audioResult == 0 && sensorResult == 0) {
                            //Test Passed
                            mAttempts = 0;

                            Calendar calendar = Calendar.getInstance();
                            String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
                            String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(calendar.getTime());
                            String contents = dateString + "," + timeString + "," + "success";
                            DataLogService.log(getActivity(), new File(MainActivity.getRootFile(getActivity()), "test_log.csv"), contents, "Date,Time,Result");
                            final MaterialDialog resultDialog = new MaterialDialog.Builder(getActivity())
                                    .title("Success!")
                                    .customView(R.layout.view_test_success, true)
                                    .positiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            remindToChargeBand();
                                        }
                                    })
                                    .autoDismiss(true)
                                    .canceledOnTouchOutside(false)
                                    .show();
                        } else if (audioResult != 0 && sensorResult == 0) {
                            //Only Audio Failed
                            Calendar calendar = Calendar.getInstance();
                            String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
                            String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(calendar.getTime());
                            String contents = dateString + "," + timeString + "," + "audio failed";
                            DataLogService.log(getActivity(), new File(MainActivity.getRootFile(getActivity()), "test_log.csv"), contents, "Date,Time,Result");
                            if (mAttempts < 4) {
                                new MaterialDialog.Builder(getActivity())
                                        .title("Test Failed")
                                        .customView(R.layout.view_test_audio_fail, true)
                                        .negativeText("Restart")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);
                                                SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_SENSOR_ENABLE, true);
                                                SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_AUDIO_ENABLE, true);
                                            }
                                        })
                                        .canceledOnTouchOutside(false)
                                        .show();
                                mAttempts++;
                            } else {
                                callRA(audioResult);
                            }
                        } else if (audioResult == 0 && sensorResult != 0) {
                            //Only Sensors Failed
                            Calendar calendar = Calendar.getInstance();
                            String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
                            String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(calendar.getTime());
                            String contents = dateString + "," + timeString + "," + "sensors failed";
                            DataLogService.log(getActivity(), new File(MainActivity.getRootFile(getActivity()), "test_log.csv"), contents, "Date,Time,Result");
                            if (mAttempts < 4) {
                                new MaterialDialog.Builder(getActivity())
                                        .title("Test Failed")
                                        .customView(R.layout.view_test_sensors_fail, true)
                                        .negativeText("Restart")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);

                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            BandCollectionService.disconnect(getActivity());
                                                            Thread.sleep(250);
                                                            BandCollectionService.connect(getActivity());
                                                            Thread.sleep(250);
                                                            BandCollectionService.startStream(getActivity());
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_SENSOR_ENABLE, true);
                                                                    SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_AUDIO_ENABLE, true);
                                                                    //test();
                                                                }
                                                            });
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).start();
                                            }
                                        })
                                        .canceledOnTouchOutside(false)
                                        .show();
                                mAttempts++;
                            } else {
                                callRA(sensorResult);
                            }
                        } else {
                            //Both Failed
                            Calendar calendar = Calendar.getInstance();
                            String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
                            String timeString = new SimpleDateFormat("kk:mm:ss.SSS", Locale.US).format(calendar.getTime());
                            String contents = dateString + "," + timeString + "," + "Sensors and audio failed";
                            DataLogService.log(getActivity(), new File(MainActivity.getRootFile(getActivity()), "test_log.csv"), contents, "Date,Time,Result");
                            if (mAttempts < 4) {
                                new MaterialDialog.Builder(getActivity())
                                        .title("Test Failed")
                                        .customView(R.layout.view_test_fail, true)
                                        .negativeText("Restart")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                                                AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);

                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            BandCollectionService.disconnect(getActivity());
                                                            Thread.sleep(250);
                                                            BandCollectionService.connect(getActivity());
                                                            Thread.sleep(250);
                                                            BandCollectionService.startStream(getActivity());
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_SENSOR_ENABLE, true);
                                                                    SettingsActivity.putBoolean(getActivity(), SettingsActivity.KEY_AUDIO_ENABLE, true);
                                                                    //test();
                                                                }
                                                            });
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).start();
                                            }
                                        })
                                        .canceledOnTouchOutside(false)
                                        .show();
                                mAttempts++;
                            } else {
                                callRA(4);
                            }
                        }
                    }
                });
            }
        }, 500);
    }

    private int checkAudio() {
        File root = MainActivity.getRootFile(getActivity());
        if (!root.exists()) {
            return 1;
        }

        ArrayList<File> audioFiles = new ArrayList<>();
        for (File file : root.listFiles()) {
            if (file.getName().endsWith(".wav")) {
                audioFiles.add(file);
            }
        }
        if (audioFiles.size() == 0) {
            return 2;
        }
        long lastModified = 0;
        for (File file : audioFiles) {
            if (file.lastModified() > lastModified) {
                lastModified = file.lastModified();
            }
        }
        Calendar currentTimeThreshold = Calendar.getInstance();

        //Check nightly blackout
        Calendar currentTime = Calendar.getInstance();
        Calendar calendar1am = Calendar.getInstance();
        calendar1am.set(Calendar.HOUR_OF_DAY, 1);
        Calendar calendar5am = Calendar.getInstance();
        calendar5am.set(Calendar.HOUR_OF_DAY, 5);

        if (currentTime.compareTo(calendar1am) > 0 && currentTime.compareTo(calendar5am) < 0) {
            //In nightly blackout time
            //Check from initial blackout time
            currentTimeThreshold.setTimeInMillis(calendar1am.getTimeInMillis());
            currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
        }


        //check school blackout
        if (SettingsActivity.getBoolean(getActivity(), SettingsActivity.KEY_BLACKOUT_TOGGLE, false)) {
            //Check if in nightly blackout
            Calendar calendar730am = Calendar.getInstance();
            calendar730am.set(Calendar.HOUR_OF_DAY, 7);
            calendar730am.set(Calendar.MINUTE, 30);
            Calendar calendar3pm = Calendar.getInstance();
            calendar3pm.set(Calendar.HOUR_OF_DAY, 15);

            if (currentTime.compareTo(calendar730am) > 0 && currentTime.compareTo(calendar3pm) < 0) {
                //In school blackout
                //Check from initial blackout time
                currentTimeThreshold.setTimeInMillis(calendar730am.getTimeInMillis());
                currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
            } else {
                //Not in blackout time
                //Check from current time
                currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
            }
        } else {
            currentTimeThreshold.set(Calendar.MINUTE, currentTimeThreshold.get(Calendar.MINUTE)-15);
        }
        Calendar lastModifiedTime = Calendar.getInstance();
        lastModifiedTime.setTimeInMillis(lastModified);
        Log.d("TEST", "Threshold Time: " + currentTimeThreshold.getTime() + "\nLast Modified: " + lastModifiedTime.getTime());
        if (lastModifiedTime.compareTo(currentTimeThreshold) < 0) {
            return 3;
        } else {
            return 0;
        }
    }

    private int checkSensors() {
        if (mSensorWorking) {
            return 0;
        } else {
            return 1;
        }
    }

    private void callRA(int error) {
        //Call RA
        mAttempts = 0;
        new MaterialDialog.Builder(getActivity())
                .title("Error - " + error)
                .content("Try testing again. If this error continues to occur, please call the RA at 401-354-2803")
                .positiveText("OK")
                .canceledOnTouchOutside(false)
                .show();
    }

    private void remindToChargeBand() {
        new MaterialDialog.Builder(getActivity())
                .title("Please charge your Band and keep charging your device!")
                .positiveText("OK")
                .show();
    }*/
}
