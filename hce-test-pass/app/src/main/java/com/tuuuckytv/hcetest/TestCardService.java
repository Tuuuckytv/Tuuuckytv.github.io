package com.tuuuckytv.hcetest;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestCardService extends HostApduService {
    private static final byte[] AID = hex("F054555555434B59545601");
    private static final byte[] OK = hex("9000");
    private static final byte[] NOT_FOUND = hex("6A82");
    private static final byte[] CONDITIONS_NOT_SATISFIED = hex("6985");
    private static final byte[] INS_NOT_SUPPORTED = hex("6D00");
    private static final byte[] SELECT_PREFIX = hex("00A40400");
    private static final byte[] GET_DATA = hex("00CA000000");

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        boolean enabled = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                .getBoolean(MainActivity.KEY_ENABLED, true);

        if (!enabled) return CONDITIONS_NOT_SATISFIED;
        if (commandApdu == null || commandApdu.length < 4) return INS_NOT_SUPPORTED;

        if (isSelectForOurAid(commandApdu)) {
            return withStatus("TUUUCKY-HCE-TEST-V1".getBytes(StandardCharsets.US_ASCII), OK);
        }

        if (Arrays.equals(commandApdu, GET_DATA)) {
            return withStatus("OFFLINE-TEST-OK".getBytes(StandardCharsets.US_ASCII), OK);
        }

        if (startsWith(commandApdu, SELECT_PREFIX)) return NOT_FOUND;
        return INS_NOT_SUPPORTED;
    }

    @Override public void onDeactivated(int reason) {
        // No state or network activity. A new reader selection starts a fresh test session.
    }

    private boolean isSelectForOurAid(byte[] apdu) {
        if (!startsWith(apdu, SELECT_PREFIX) || apdu.length < 5) return false;
        int lc = apdu[4] & 0xFF;
        if (lc != AID.length || apdu.length < 5 + lc) return false;
        for (int i = 0; i < AID.length; i++) {
            if (apdu[5 + i] != AID[i]) return false;
        }
        return true;
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    private static byte[] withStatus(byte[] payload, byte[] status) {
        byte[] out = new byte[payload.length + status.length];
        System.arraycopy(payload, 0, out, 0, payload.length);
        System.arraycopy(status, 0, out, payload.length, status.length);
        return out;
    }

    private static byte[] hex(String value) {
        int len = value.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
        }
        return out;
    }
}
