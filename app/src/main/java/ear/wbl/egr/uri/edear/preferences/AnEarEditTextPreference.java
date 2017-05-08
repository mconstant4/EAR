package ear.wbl.egr.uri.edear.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ear.wbl.egr.uri.edear.R;

/**
 * Created by mconstant on 5/8/17.
 */

public class AnEarEditTextPreference extends android.preference.EditTextPreference {
    protected TextView textValue;

    public AnEarEditTextPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_edit_text);
    }

    public AnEarEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_edit_text);
    }

    public AnEarEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_edit_text);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        textValue = (TextView) view.findViewById(R.id.pref_value);
        if (textValue != null) {
            textValue.setText(getText());
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (textValue != null) {
            textValue.setText(getText());
        }
    }
}
