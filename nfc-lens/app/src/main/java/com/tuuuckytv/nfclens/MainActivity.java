package com.tuuuckytv.nfclens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity implements NfcAdapter.ReaderCallback {
    private NfcAdapter adapter;
    private TextView status;
    private TextView output;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        adapter = NfcAdapter.getDefaultAdapter(this);
        setContentView(makeUi());
        updateStatus();
    }

    @Override protected void onResume() {
        super.onResume();
        if (adapter != null && adapter.isEnabled()) {
            int flags = NfcAdapter.FLAG_READER_NFC_A |
                    NfcAdapter.FLAG_READER_NFC_B |
                    NfcAdapter.FLAG_READER_NFC_F |
                    NfcAdapter.FLAG_READER_NFC_V |
                    NfcAdapter.FLAG_READER_NFC_BARCODE |
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
            adapter.enableReaderMode(this, this, flags, null);
        }
        updateStatus();
    }

    @Override protected void onPause() {
        super.onPause();
        if (adapter != null) adapter.disableReaderMode(this);
    }

    private View makeUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(20));
        root.setBackgroundColor(Color.rgb(16,20,23));

        root.addView(text("NFC LENS", 34, Color.rgb(231,225,212), true));
        TextView sub = text("CARD INSPECTOR // READ-ONLY", 13, Color.rgb(76,145,141), true);
        sub.setPadding(0, dp(2), 0, dp(12));
        root.addView(sub);

        status = text("CHECKING NFC…", 14, Color.rgb(231,225,212), true);
        status.setPadding(dp(12), dp(12), dp(12), dp(12));
        status.setBackgroundColor(Color.rgb(24,32,37));
        root.addView(status, full());

        Button settings = new Button(this);
        settings.setText("OPEN NFC SETTINGS");
        settings.setAllCaps(false);
        settings.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        settings.setTextColor(Color.rgb(231,225,212));
        settings.setBackgroundColor(Color.rgb(32,54,51));
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
        LinearLayout.LayoutParams bp = full();
        bp.topMargin = dp(10);
        root.addView(settings, bp);

        TextView hint = text("Hold an NFC card against the back of the phone. NFC Lens reports the technologies and normal readable metadata Android exposes.", 12, Color.rgb(154,168,169), false);
        hint.setPadding(0, dp(12), 0, dp(12));
        root.addView(hint);

        ScrollView scroll = new ScrollView(this);
        output = text("WAITING FOR CARD…", 13, Color.rgb(231,225,212), false);
        output.setTextIsSelectable(true);
        output.setPadding(dp(12), dp(12), dp(12), dp(20));
        output.setBackgroundColor(Color.rgb(24,32,37));
        scroll.addView(output, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return root;
    }

    private TextView text(String value, int size, int color, boolean bold) {
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

    private void updateStatus() {
        if (status == null) return;
        if (adapter == null) status.setText("NFC NOT AVAILABLE ON THIS DEVICE");
        else if (!adapter.isEnabled()) status.setText("NFC IS OFF // TURN IT ON TO SCAN");
        else status.setText("READY // TAP A CARD TO THE PHONE");
    }

    @Override public void onTagDiscovered(Tag tag) {
        String report = inspect(tag);
        runOnUiThread(() -> {
            status.setText("CARD READ // REMOVE OR TAP AGAIN");
            output.setText(report);
        });
    }

    private String inspect(Tag tag) {
        StringBuilder s = new StringBuilder();
        s.append("NFC CARD REPORT\n==============================\n\n");
        s.append("TAG ID\n").append(hex(tag.getId())).append("\n\n");

        s.append("ANDROID TECH LIST\n");
        for (String tech : tag.getTechList()) s.append("• ").append(shortName(tech)).append("\n");
        s.append("\n");

        NfcA a = NfcA.get(tag);
        if (a != null) {
            s.append("NFC-A\nATQA: ").append(hex(a.getAtqa())).append("\n");
            s.append(String.format(Locale.UK, "SAK: 0x%02X\n\n", a.getSak() & 0xff));
        }

        NfcB b = NfcB.get(tag);
        if (b != null) {
            s.append("NFC-B\nApplication data: ").append(hex(b.getApplicationData())).append("\n");
            s.append("Protocol info: ").append(hex(b.getProtocolInfo())).append("\n\n");
        }

        NfcF f = NfcF.get(tag);
        if (f != null) {
            s.append("NFC-F\nManufacturer: ").append(hex(f.getManufacturer())).append("\n");
            s.append("System code: ").append(hex(f.getSystemCode())).append("\n\n");
        }

        NfcV v = NfcV.get(tag);
        if (v != null) {
            s.append("NFC-V / ISO 15693\n");
            s.append(String.format(Locale.UK, "DSF ID: 0x%02X\n", v.getDsfId() & 0xff));
            s.append(String.format(Locale.UK, "Response flags: 0x%02X\n\n", v.getResponseFlags() & 0xff));
        }

        IsoDep iso = IsoDep.get(tag);
        if (iso != null) {
            s.append("ISO-DEP / ISO 14443-4\n");
            s.append("Historical bytes: ").append(hex(iso.getHistoricalBytes())).append("\n");
            s.append("Higher-layer response: ").append(hex(iso.getHiLayerResponse())).append("\n");
            s.append("Max transceive: ").append(iso.getMaxTransceiveLength()).append(" bytes\n\n");
        }

        MifareClassic mc = MifareClassic.get(tag);
        if (mc != null) {
            s.append("MIFARE CLASSIC\n");
            s.append("Type: ").append(mcType(mc.getType())).append("\n");
            s.append("Size: ").append(mc.getSize()).append(" bytes\n");
            s.append("Sectors: ").append(mc.getSectorCount()).append("\n");
            s.append("Blocks: ").append(mc.getBlockCount()).append("\n\n");
        }

        MifareUltralight mu = MifareUltralight.get(tag);
        if (mu != null) {
            s.append("MIFARE ULTRALIGHT\n");
            s.append("Type: ").append(muType(mu.getType())).append("\n\n");
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            s.append("NDEF\n");
            s.append("Type: ").append(ndef.getType()).append("\n");
            s.append("Writable: ").append(ndef.isWritable() ? "YES" : "NO").append("\n");
            s.append("Capacity: ").append(ndef.getMaxSize()).append(" bytes\n");
            try {
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                if (msg == null || msg.getRecords().length == 0) {
                    s.append("Records: none\n");
                } else {
                    s.append("Records: ").append(msg.getRecords().length).append("\n");
                    int i = 1;
                    for (NdefRecord r : msg.getRecords()) {
                        s.append("  [").append(i++).append("] TNF=").append(r.getTnf());
                        s.append(" type=").append(printable(r.getType()));
                        s.append(" payload=").append(preview(r.getPayload())).append("\n");
                    }
                }
            } catch (Exception e) {
                s.append("Records: metadata available; message read failed\n");
            } finally {
                try { ndef.close(); } catch (Exception ignored) {}
            }
            s.append("\n");
        }

        s.append("NOTE\nThis inspector does not perform card emulation or authenticated-memory extraction.");
        return s.toString();
    }

    private String shortName(String tech) {
        int i = tech.lastIndexOf('.');
        return i >= 0 ? tech.substring(i + 1) : tech;
    }

    private String hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "—";
        StringBuilder s = new StringBuilder();
        for (byte b : bytes) s.append(String.format(Locale.US, "%02X", b & 0xff));
        return s.toString();
    }

    private String printable(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "—";
        for (byte b : bytes) {
            int c = b & 0xff;
            if (c < 32 || c > 126) return hex(bytes);
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private String preview(byte[] payload) {
        if (payload == null || payload.length == 0) return "—";
        int n = Math.min(payload.length, 64);
        byte[] first = new byte[n];
        System.arraycopy(payload, 0, first, 0, n);
        String v = printable(first);
        return payload.length > n ? v + "… (" + payload.length + " bytes)" : v;
    }

    private String mcType(int type) {
        if (type == MifareClassic.TYPE_CLASSIC) return "CLASSIC";
        if (type == MifareClassic.TYPE_PLUS) return "PLUS";
        if (type == MifareClassic.TYPE_PRO) return "PRO";
        return "UNKNOWN";
    }

    private String muType(int type) {
        if (type == MifareUltralight.TYPE_ULTRALIGHT) return "ULTRALIGHT";
        if (type == MifareUltralight.TYPE_ULTRALIGHT_C) return "ULTRALIGHT C";
        return "UNKNOWN";
    }
}
