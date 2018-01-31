package jp.co.chooyan.tcpdemo;

import android.content.res.AssetManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SimpleTcpServer server;
    private EditText userInput;
    private RequestListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View parent = getLayoutInflater().inflate(R.layout.activity_main, null);
        userInput = parent.findViewById(R.id.user_input);
        ListView listView = parent.findViewById(R.id.request_list);
        adapter = new RequestListAdapter(this);
        listView.setAdapter(adapter);

        startServer();

        setContentView(parent);
    }

    private void startServer() {
        if (server != null) {
            return;
        }
        server = new SimpleTcpServer(new SimpleTcpServer.TcpConnectionListener() {
            private HttpRequestParser parser = new HttpRequestParser();
            @Override
            public void onReceive(final byte[] data) {
                parser.add(data);
                final HttpRequest request = parser.parse();
                if (request != null) {
                    output(request);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.add(request);
                        }
                    });
                    parser.clear();
                }
            }

            @Override
            public void onResponseSent() {
                server.restart();
            }
        }, 20000);
        server.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.stop();
        server = null;
    }

    private void output(HttpRequest request) {
        if (server == null) {
            return;
        }

        if (request.getRequestTarget().equals("/") || request.getRequestTarget().equals("/index.html")) {
            outputHtml(buildIndexHtml(request), "200 OK");
        } else if (request.getRequestTarget().equals("/favicon.ico")) {
            outputPng(loadBinary("favicon.png"), "200 OK");
        } else {
            outputHtml(build404Html(request), "404 Not Found");
        }
    }

    private String buildIndexHtml(HttpRequest request) {
        return loadHtml("index.html").replace("{{client}}", request.getHeaders().get("User-Agent"))
                         .replace("{{brand}}", Build.BRAND)
                         .replace("{{device}}", Build.DEVICE)
                         .replace("{{location}}", "35.656559, 139.6929806") // TODO: use GPS if I have time...
                         .replace("{{message}}", userInput.getText().toString());
    }

    private String build404Html(HttpRequest request) {
        return loadHtml("404.html");
    }

    private static final int BUFFER_SIZE = 1024 * 1024 * 1;
    private static final byte LF = 0x0a;
    private static final byte CR = 0x0d;

    private void outputHtml(String html, String responseCode) {

        String startLine = "HTTP/1.1 " + responseCode;
        List<String> responseHeaders = new ArrayList<>();
        responseHeaders.add("Content-Type: text/html; charset=UTF-8");
        responseHeaders.add(String.format("Content-Length: %d", html.getBytes().length));

        StringBuilder builder = new StringBuilder();
        builder.append(startLine).append(new String(new byte[]{CR, LF}));
        for (String responseHeader : responseHeaders) {
            builder.append(responseHeader).append(new String(new byte[]{CR, LF}));
        }
        builder.append(new String(new byte[]{CR, LF}));
        builder.append(html);

        server.output(builder.toString());
    }

    private void outputPng(byte[] png, String responseCode) {

        String startLine = "HTTP/1.1 " + responseCode;
        List<String> responseHeaders = new ArrayList<>();
        responseHeaders.add("Content-Type: image/png");
        responseHeaders.add(String.format("Content-Length: %d", png.length));

        StringBuilder builder = new StringBuilder();
        builder.append(startLine).append(new String(new byte[]{CR, LF}));
        for (String responseHeader : responseHeaders) {
            builder.append(responseHeader).append(new String(new byte[]{CR, LF}));
        }
        builder.append(new String(new byte[]{CR, LF}));
        byte[] headerField = builder.toString().getBytes();

        byte[] output = new byte[headerField.length + png.length];
        System.arraycopy(headerField, 0, output, 0, headerField.length);
        System.arraycopy(png, 0, output, headerField.length, png.length);
        server.output(output);
    }

    @Nullable
    private String loadHtml(String fileName) {
        byte[] binary = loadBinary(fileName);
        if (binary == null) {
            return null;
        }
        return new String(binary);
    }

    @Nullable
    private byte[] loadBinary(String fileName) {
        AssetManager assetManager = getAssets();

        try {
            InputStream is = assetManager.open(fileName);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] chunk = new byte[BUFFER_SIZE];
            BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);

            try {
                int len = 0;
                while ((len = bis.read(chunk, 0, BUFFER_SIZE)) > 0) {
                    byteStream.write(chunk, 0, len);
                }
                return byteStream.toByteArray();

            } finally {
                try {
                    byteStream.reset();
                    bis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
