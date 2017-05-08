package ear.wbl.egr.uri.edear.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ear.wbl.egr.uri.edear.R;

/**
 * Created by mconstant on 5/8/17.
 */

public class DurationEditTextPreference extends AnEarEditTextPreference {
    public DurationEditTextPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_edit_text);
    }

    public DurationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_edit_text);
    }

    public DurationEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_edit_text);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (textValue != null) {
            textValue.append(" seconds");
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (textValue != null) {
            textValue.append(" seconds");
        }
    }
}
