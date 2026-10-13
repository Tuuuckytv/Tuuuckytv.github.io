package com.tuuuckytv.nfcinspector;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView status;
    private TextView report;
    private String lastReport = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        updateNfcStatus();
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNfcStatus();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            try { nfcAdapter.disableForegroundDispatch(this); } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void buildUi() {
        int pad = dp(18);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(Color.rgb(8, 14, 18));

        TextView title = new TextView(this);
        title.setText("TUUUCKY NFC INSPECTOR");
        title.setTextColor(Color.rgb(142, 216, 204));
        title.setTextSize(23);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("READ-ONLY CARD DIAGNOSTICS • NO WRITING • NO EMULATION");
        note.setTextColor(Color.rgb(176, 190, 196));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER_HORIZONTAL);
        note.setPadding(0, 0, 0, dp(14));
        root.addView(note);

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(12), dp(15), dp(12), dp(15));
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(12), 0, dp(12));

        Button settings = button("NFC SETTINGS");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
        buttons.addView(settings, new LinearLayout.LayoutParams(0, -2, 1f));

        Button copy = button("COPY REPORT");
        copy.setOnClickListener(v -> copyReport());
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, -2, 1f);
        copyParams.setMarginStart(dp(8));
        buttons.addView(copy, copyParams);

        Button clear = button("CLEAR");
        clear.setOnClickListener(v -> {
            lastReport = "";
            report.setText(defaultText());
        });
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(0, -2, 0.7f);
        clearParams.setMarginStart(dp(8));
        buttons.addView(clear, clearParams);
        root.addView(buttons);

        report = new TextView(this);
        report.setTextColor(Color.rgb(226, 232, 235));
        report.setTextSize(14);
        report.setTextIsSelectable(true);
        report.setMovementMethod(new ScrollingMovementMethod());
        report.setPadding(dp(4), dp(4), dp(4), dp(20));
        report.setText(defaultText());

        ScrollView scroll = new ScrollView(this);
        scroll.addView(report);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }

    private String defaultText() {
        return "Tap an NFC card against the back of the phone.\n\n"
                + "The app only reads Android-exposed identification and technology metadata. "
                + "It does not authenticate to, write to, or emulate the card.";
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(27, 74, 73));
        return b;
    }

    private void updateNfcStatus() {
        if (nfcAdapter == null) {
            status.setText("NFC NOT AVAILABLE ON THIS DEVICE");
            status.setBackgroundColor(Color.rgb(90, 30, 30));
        } else if (!nfcAdapter.isEnabled()) {
            status.setText("NFC IS OFF — TAP NFC SETTINGS");
            status.setBackgroundColor(Color.rgb(103, 65, 20));
        } else {
            status.setText("READY — TAP YOUR CARD");
            status.setBackgroundColor(Color.rgb(20, 82, 68));
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                && !NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                && !NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) return;

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
        if (tag == null) return;

        lastReport = buildReport(tag);
        report.setText(lastReport);
        status.setText("CARD READ — REMOVE IT WHEN READY");
        status.setBackgroundColor(Color.rgb(27, 74, 73));
    }

    private String buildReport(Tag tag) {
        StringBuilder out = new StringBuilder();
        out.append("TUUUCKY NFC INSPECTOR REPORT\n");
        out.append("Captured: ").append(new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.UK).format(new Date())).append("\n\n");

        byte[] id = tag.getId();
        out.append("TAG IDENTIFIER\n");
        out.append("UID / serial: ").append(hex(id)).append("\n");
        out.append("UID length: ").append(id == null ? 0 : id.length).append(" bytes\n\n");

        out.append("ANDROID NFC TECHNOLOGIES\n");
        for (String tech : tag.getTechList()) out.append("• ").append(shortTech(tech)).append("\n");
        out.append("\n");

        NfcA a = NfcA.get(tag);
        if (a != null) {
            out.append("NFC-A\n");
            out.append("ATQA: ").append(hex(a.getAtqa())).append("\n");
            out.append("SAK: 0x").append(String.format(Locale.US, "%02X", a.getSak() & 0xFF)).append("\n\n");
        }

        NfcB b = NfcB.get(tag);
        if (b != null) {
            out.append("NFC-B\n");
            out.append("Application data: ").append(hex(b.getApplicationData())).append("\n");
            out.append("Protocol info: ").append(hex(b.getProtocolInfo())).append("\n\n");
        }

        NfcF f = NfcF.get(tag);
        if (f != null) {
            out.append("NFC-F / FeliCa\n");
            out.append("Manufacturer: ").append(hex(f.getManufacturer())).append("\n");
            out.append("System code: ").append(hex(f.getSystemCode())).append("\n\n");
        }

        NfcV v = NfcV.get(tag);
        if (v != null) {
            out.append("NFC-V / ISO 15693\n");
            out.append("DSF ID: 0x").append(String.format(Locale.US, "%02X", v.getDsfId() & 0xFF)).append("\n");
            out.append("Response flags: 0x").append(String.format(Locale.US, "%02X", v.getResponseFlags() & 0xFF)).append("\n\n");
        }

        IsoDep iso = IsoDep.get(tag);
        if (iso != null) {
            out.append("ISO-DEP / ISO 14443-4\n");
            out.append("Historical bytes: ").append(hex(iso.getHistoricalBytes())).append("\n");
            out.append("Higher-layer response: ").append(hex(iso.getHiLayerResponse())).append("\n");
            out.append("Max transceive: ").append(iso.getMaxTransceiveLength()).append(" bytes\n");
            out.append("Extended APDU supported: ").append(iso.isExtendedLengthApduSupported() ? "YES" : "NO").append("\n\n");
        }

        MifareClassic classic = MifareClassic.get(tag);
        if (classic != null) {
            out.append("MIFARE CLASSIC\n");
            out.append("Type: ").append(mifareClassicType(classic.getType())).append("\n");
            out.append("Memory size: ").append(classic.getSize()).append(" bytes\n");
            out.append("Sectors: ").append(classic.getSectorCount()).append("\n");
            out.append("Blocks: ").append(classic.getBlockCount()).append("\n\n");
        }

        MifareUltralight ultra = MifareUltralight.get(tag);
        if (ultra != null) {
            out.append("MIFARE ULTRALIGHT\n");
            out.append("Type: ").append(mifareUltralightType(ultra.getType())).append("\n\n");
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            out.append("NDEF\n");
            out.append("Type: ").append(ndef.getType()).append("\n");
            out.append("Writable: ").append(ndef.isWritable() ? "YES" : "NO").append("\n");
            out.append("Max size: ").append(ndef.getMaxSize()).append(" bytes\n");
            NdefMessage message = ndef.getCachedNdefMessage();
            if (message == null) {
                out.append("Cached message: none\n\n");
            } else {
                NdefRecord[] records = message.getRecords();
                out.append("Records: ").append(records.length).append("\n");
                for (int i = 0; i < records.length; i++) {
                    NdefRecord r = records[i];
                    out.append("  #").append(i + 1)
                            .append(" TNF=").append(r.getTnf())
                            .append(" type=").append(safeAscii(r.getType()))
                            .append(" payload=").append(r.getPayload() == null ? 0 : r.getPayload().length).append(" bytes");
                    try {
                        if (r.toUri() != null) out.append(" uri=").append(r.toUri());
                    } catch (Exception ignored) {}
                    out.append("\n");
                }
                out.append("\n");
            }
        }

        if (NdefFormatable.get(tag) != null) {
            out.append("NDEF FORMATABLE\n");
            out.append("This tag can be formatted as NDEF by compatible software. This inspector does not write or format it.\n\n");
        }

        out.append("WHAT THIS MEANS\n");
        if (iso != null) {
            out.append("• ISO-DEP is present. Android HCE can emulate ISO-DEP applications only when the reader/system is designed or configured to accept a phone credential.\n");
        }
        if (classic != null || ultra != null) {
            out.append("• MIFARE technology is present. A normal Android phone cannot generically reproduce an arbitrary physical card as the same card.\n");
        }
        if (ndef != null && iso == null && classic == null && ultra == null) {
            out.append("• This looks primarily like an NDEF/data tag from Android's view.\n");
        }
        out.append("• The UID shown above is diagnostic metadata only. This app does not use it to emulate a credential.\n");
        out.append("• No authentication commands, protected-memory reads, writes, or card emulation are performed.\n");

        return out.toString();
    }

    private String shortTech(String tech) {
        int dot = tech.lastIndexOf('.');
        return dot >= 0 ? tech.substring(dot + 1) : tech;
    }

    private String mifareClassicType(int type) {
        if (type == MifareClassic.TYPE_CLASSIC) return "Classic";
        if (type == MifareClassic.TYPE_PLUS) return "Plus";
        if (type == MifareClassic.TYPE_PRO) return "Pro";
        return "Unknown (" + type + ")";
    }

    private String mifareUltralightType(int type) {
        if (type == MifareUltralight.TYPE_ULTRALIGHT) return "Ultralight";
        if (type == MifareUltralight.TYPE_ULTRALIGHT_C) return "Ultralight C";
        return "Unknown (" + type + ")";
    }

    private String safeAscii(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "(none)";
        String s = new String(bytes, StandardCharsets.US_ASCII).replaceAll("[^\\x20-\\x7E]", ".");
        if (s.length() > 60) s = s.substring(0, 60) + "…";
        return s;
    }

    private String hex(byte[] data) {
        if (data == null || data.length == 0) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (byte value : data) sb.append(String.format(Locale.US, "%02X", value));
        return sb.toString();
    }

    private void copyReport() {
        if (lastReport.isEmpty()) {
            Toast.makeText(this, "Tap a card first", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("NFC Inspector Report", lastReport));
        Toast.makeText(this, "Report copied", Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
