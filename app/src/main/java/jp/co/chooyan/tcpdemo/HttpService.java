package jp.co.chooyan.tcpdemo;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpService extends BaseHttpService {

    private SimpleTcpServer server;
    public static boolean deviceBootStart = false;
    public static RequestListAdapter adapter;
    private static final int listenerPort = 20000;

    public static BaseHttpService activeService;

    public HttpService() {
    }

    @Override
    protected void start(Context context) {
        activeService = this;
        Intent intent = new Intent(context, HttpService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

        NotificationHelper notificationHelper = new NotificationHelper(this);
        Notification.Builder builder = notificationHelper.getNotification();
        startForeground(2, builder.build());
        startHttpServer(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return super.binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        super.stopHttpService(getApplicationContext());

        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public  static void stopIfActive(Context context){
        if(activeService != null){
            activeService.stopHttpService(context);
        }
    }

    private void startHttpServer(final Context context) {
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
                    switch (request.getMethod()){
                        case "GET":
                            output(request);
                            break;
                        case "POST":
                            input(request);
                            break;
                    }

                    new Thread(new Runnable() {
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
        }, listenerPort);
        server.start();
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
        return deviceBootStart ?
                loadHtml("index.html").replace("{{client}}", request.getHeaders().get("User-Agent"))
                        .replace("{{brand}}", Build.BRAND)
                        .replace("{{device}}", Build.DEVICE)
                        .replace("{{location}}", "35.656559, 139.6929806") // TODO: use GPS if I have time...
                        .replace("{{message}}", "Device power on with service start") :
                loadHtml("index.html").replace("{{client}}", request.getHeaders().get("User-Agent"))
                        .replace("{{brand}}", Build.BRAND)
                        .replace("{{device}}", Build.DEVICE)
                        .replace("{{location}}", "35.656559, 139.6929806") // TODO: use GPS if I have time...
                        .replace("{{message}}", MainActivity.userInput.getText().toString()
                        );
    }

    private void input(HttpRequest request){
        //Describe the processing corresponding to the POST method

        if (request.getRequestTarget().equals("/") || request.getRequestTarget().equals("/dataload")) {
            outputHtml(buildPostResult(request), "200 OK");
        } else if (request.getRequestTarget().equals("/favicon.ico")) {
            outputPng(loadBinary("favicon.png"), "200 OK");
        } else {
            outputHtml(build404Html(request), "404 Not Found");
        }
    }

    private String buildPostResult(HttpRequest request) {
        return deviceBootStart ?
                loadHtml("index.html").replace("{{client}}", request.getHeaders().get("User-Agent"))
                        .replace("{{brand}}", Build.BRAND)
                        .replace("{{device}}", Build.DEVICE)
                        .replace("{{location}}", "35.656559, 139.6929806") // TODO: use GPS if I have time...
                        .replace("{{message}}", "Device power on with service start") :
                loadHtml("index.html").replace("{{client}}", request.getHeaders().get("User-Agent"))
                        .replace("{{brand}}", Build.BRAND)
                        .replace("{{device}}", Build.DEVICE)
                        .replace("{{location}}", "35.656559, 139.6929806") // TODO: use GPS if I have time...
                        .replace("{{message}}", MainActivity.userInput.getText().toString());
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
