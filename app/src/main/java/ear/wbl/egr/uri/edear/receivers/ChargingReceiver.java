package ear.wbl.egr.uri.edear.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.anear.AnEar;
import wbl.egr.uri.anear.io.services.CsvLogService;

/**
 * Created by mconstant on 5/9/17.
 */

public class ChargingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean charging = intent.getAction().equals(Intent.ACTION_POWER_CONNECTED);
        File chargeLog = new File(AnEar.getRoot(context), "charge.csv");
        final String header = "Date,Time,Charging?";
        Date currentTime = Calendar.getInstance().getTime();
        final String date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(currentTime);
        final String time = new SimpleDateFormat("kk:mm:ss", Locale.US).format(currentTime);
        final String[] contents;
        if (charging) {
            contents = new String[] {date, time, "Charging"};
        } else {
            contents = new String[] {date, time, "Not Charging"};
        }

        CsvLogService.logData(context, chargeLog, header, CsvLogService.generateContents(contents));
    }
}
