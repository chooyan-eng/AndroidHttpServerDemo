package jp.co.chooyan.tcpdemo;

import java.util.Map;

/**
 * Created by chooyan-eng on 18/01/31.
 */

public class HttpRequest {
    private StartLine startLine;
    private Map<String, String> headers;
    private byte[] messageBody;

    public HttpRequest(StartLine startLine, Map<String, String> headers, byte[] messageBody) {
        this.startLine = startLine;
        this.headers = headers;
        this.messageBody = messageBody;
    }

    public String getMethod() {
        return startLine.method;
    }

    public String getRequestTarget() {
        return startLine.requestTarget;
    }

    public String getHttpVersion() {
        return startLine.httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public static class StartLine {
        private String method;
        private String requestTarget;
        private String httpVersion;

        public StartLine(String method, String requestTarget, String httpVersion) {
            this.method = method;
            this.requestTarget = requestTarget;
            this.httpVersion = httpVersion;
        }
    }
}
