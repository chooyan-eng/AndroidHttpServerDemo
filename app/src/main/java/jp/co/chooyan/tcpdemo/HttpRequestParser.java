package jp.co.chooyan.tcpdemo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chooyan-eng on 18/01/31.
 * TODO: Current logic of parsing does NOT follow RFC
 *       @see "https://tools.ietf.org/html/rfc7230#section-3.2.4"
 */

public class HttpRequestParser {
    private static final byte SP = 0x20;
    private static final byte LF = 0x0a;
    private static final byte CR = 0x0d;

    private byte[] rawData;

    public HttpRequestParser() {
        rawData = new byte[]{};
    }

    public void add(byte[] data) {
        byte[] newBytes = new byte[rawData.length + data.length];
        System.arraycopy(rawData, 0, newBytes, 0, rawData.length);
        System.arraycopy(data, 0, newBytes, rawData.length, data.length);
        rawData = newBytes;
    }

    public void clear() {
        rawData = new byte[]{};
    }

    @Nullable
    public HttpRequest parse() {
        String[] lines = splitLines(rawData);
        if (lines == null || lines.length == 0) {
            return null;
        }

        HttpRequest.StartLine startLine = parseStartLine(lines[0]);
        if (startLine == null) {
            return null;
        }

        int emptyLineNum = findEmptyLine(lines);
        String[] headerField = Arrays.copyOfRange(lines, 1, emptyLineNum);
        Map<String, String> headers = parseRequestHeader(headerField);

        // TODO:
//        byte[] messageBody = null;
//        if (lines.length > emptyLineNum) {
//            messageBody
//        }
        return new HttpRequest(startLine, headers, null);
    }

    @Nullable
    private HttpRequest.StartLine parseStartLine(String startLine) {
        if (startLine == null) {
            return null;
        }

        String[] splitLine = startLine.split(new String(new byte[]{SP}));
        if(splitLine.length != 3) {
            return null;
        }

        return new HttpRequest.StartLine(splitLine[0], splitLine[1], splitLine[2]);
    }

    @NonNull
    private Map<String, String> parseRequestHeader(String[] headerField) {
        if(headerField == null || headerField.length == 0) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();

        for (String header : headerField) {
            String[] splitHeader = header.split(":", 2);
            if (splitHeader.length != 2) {
                continue;
            }
            result.put(splitHeader[0], splitHeader[1].trim());
        }

        return result;
    }

    @Nullable
    private String[] splitLines(byte[] rawData) {
        if (rawData == null || rawData.length == 0) {
            return null;
        }
        if (rawData[0] == CR || rawData[0] == LF) {
            return null;
        }

        String dataStr = new String(rawData);
        return dataStr.split(new String(new byte[]{CR, LF}));
    }

    @NonNull
    private int findEmptyLine(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                return i;
            }
        }
        return lines.length;
    }
}
