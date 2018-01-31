package jp.co.chooyan.tcpdemo;

import android.content.res.AssetManager;
import android.os.Build;
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
                    outputHtml(request);
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

    private static final int BUFFER_SIZE = 1024 * 1024 * 1;
    private static final byte LF = 0x0a;
    private static final byte CR = 0x0d;

    private void outputHtml(HttpRequest request) {
        if (server == null) {
            return;
        }

        String indexHtml = loadHtml();
        String responseBody = indexHtml.replace("{{client}}", request.getHeaders().get("User-Agent"))
                                       .replace("{{brand}}", Build.BRAND)
                                       .replace("{{device}}", Build.DEVICE)
//                                       .replace("{{location}}", "")
                                       .replace("{{message}}", userInput.getText().toString());

        String startLine = "HTTP/1.1 200 OK";
        List<String> responseHeaders = new ArrayList<>();
        responseHeaders.add("Content-Type: text/html; charset=UTF-8");
        responseHeaders.add(String.format("Content-Length: %d", responseBody.getBytes().length));

        StringBuilder builder = new StringBuilder();
        builder.append(startLine).append(new String(new byte[]{CR, LF}));
        for (String responseHeader : responseHeaders) {
            builder.append(responseHeader).append(new String(new byte[]{CR, LF}));
        }
        builder.append(new String(new byte[]{CR, LF}));
        builder.append(responseBody);

        server.output(builder.toString());
    }

    private String loadHtml() {
        AssetManager assetManager = getAssets();

        try {
            InputStream is = assetManager.open("index.html");
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] chunk = new byte[BUFFER_SIZE];
            BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);

            try {
                int len = 0;
                while ((len = bis.read(chunk, 0, BUFFER_SIZE)) > 0) {
                    byteStream.write(chunk, 0, len);
                }
                return new String(byteStream.toByteArray());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.stop();
        server = null;
    }
}
