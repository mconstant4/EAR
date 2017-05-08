package ear.wbl.egr.uri.edear.enums;

/**
 * Created by mconstant on 5/8/17.
 */

public enum Preference {
    SENSOR_ENABLE("pref_sensor_enable"),
    SENSOR_PERIODIC("pref_sensor_periodic"),
    AUDIO_ENABLE("pref_audio_enable"),
    AUDIO_DURATION("pref_audio_duration"),
    AUDIO_DELAY("pref_audio_delay"),
    SCHOOL_BLACKOUT("pref_school_blackout"),
    PATIENT_ID("pref_pid");

    public static Preference lookup(String key) {
        switch (key) {
            case "pref_sensor_enable":
                return SENSOR_ENABLE;
                //break;
            case "pref_sensor_periodic":
                return SENSOR_PERIODIC;
                //break;
            case "pref_audio_enable":
                return AUDIO_ENABLE;
                //break;
            case "pref_audio_duration":
                return AUDIO_DURATION;
                //break;
            case "pref_audio_delay":
                return AUDIO_DELAY;
                //break;
            case "pref_school_blackout":
                return SCHOOL_BLACKOUT;
                //break;
            case "pref_pid":
                return PATIENT_ID;
                //break;
            default:
                return null;
        }
    }

    private String mKey;

    Preference(String key) {
        mKey = key;
    }

    @Override
    public String toString() {
        return mKey;
    }
}
