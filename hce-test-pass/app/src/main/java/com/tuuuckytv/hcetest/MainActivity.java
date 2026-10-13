package com.tuuuckytv.hcetest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static final String PREFS = "hce_test";
    public static final String KEY_ENABLED = "enabled";
    private static final String AID = "F054555555434B59545601";
    private SharedPreferences prefs;
    private TextView state;
    private Button toggle;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        setContentView(buildUi());
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    private LinearLayout buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(22));
        root.setBackgroundColor(Color.rgb(16,20,23));

        TextView title = label("HCE TEST PASS", 32, Color.rgb(231,225,212), true);
        root.addView(title);

        TextView sub = label("OFFLINE // CUSTOM TEST CREDENTIAL", 13, Color.rgb(76,145,141), true);
        sub.setPadding(0, dp(3), 0, dp(18));
        root.addView(sub);

        state = label("", 25, Color.rgb(231,225,212), true);
        state.setPadding(dp(14), dp(18), dp(14), dp(18));
        state.setBackgroundColor(Color.rgb(24,32,37));
        root.addView(state, full());

        toggle = new Button(this);
        toggle.setAllCaps(false);
        toggle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        toggle.setTextColor(Color.rgb(231,225,212));
        toggle.setOnClickListener(v -> {
            boolean next = !prefs.getBoolean(KEY_ENABLED, true);
            prefs.edit().putBoolean(KEY_ENABLED, next).apply();
            refresh();
        });
        LinearLayout.LayoutParams tp = full();
        tp.topMargin = dp(12);
        root.addView(toggle, tp);

        Button nfc = new Button(this);
        nfc.setText("OPEN NFC SETTINGS");
        nfc.setAllCaps(false);
        nfc.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        nfc.setTextColor(Color.rgb(231,225,212));
        nfc.setBackgroundColor(Color.rgb(32,54,51));
        nfc.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
        LinearLayout.LayoutParams np = full();
        np.topMargin = dp(8);
        root.addView(nfc, np);

        TextView info = label(
            "TEST AID\n" + AID +
            "\n\nTEST RESPONSE\nTUUUCKY-HCE-TEST-V1" +
            "\n\nWHAT IT DOES\nWhen a compatible NFC reader selects this test AID, your Pixel answers as a simple ISO-DEP/HCE card." +
            "\n\nThis test does not import, copy or emulate your work card. It only proves that Android HCE is working on the phone.",
            13, Color.rgb(190,201,199), false);
        info.setTextIsSelectable(true);
        info.setPadding(dp(2), dp(18), dp(2), 0);
        root.addView(info);

        return root;
    }

    private void refresh() {
        if (state == null) return;
        NfcAdapter a = NfcAdapter.getDefaultAdapter(this);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, true);

        if (a == null) {
            state.setText("NFC NOT AVAILABLE");
            toggle.setText("TEST UNAVAILABLE");
            toggle.setEnabled(false);
            return;
        }
        toggle.setEnabled(true);

        if (!a.isEnabled()) {
            state.setText("NFC OFF\nTURN NFC ON");
        } else if (enabled) {
            state.setText("TEST PASS ACTIVE\nREADY TO TAP");
        } else {
            state.setText("TEST PASS DISABLED");
        }

        toggle.setText(enabled ? "DISABLE TEST PASS" : "ENABLE TEST PASS");
        toggle.setBackgroundColor(enabled ? Color.rgb(37,80,73) : Color.rgb(68,50,50));
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setLineSpacing(0, 1.12f);
        return t;
    }

    private LinearLayout.LayoutParams full() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
