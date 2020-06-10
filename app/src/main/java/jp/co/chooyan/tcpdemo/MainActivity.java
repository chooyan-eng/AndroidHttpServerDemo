package jp.co.chooyan.tcpdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    public static SimpleTcpServer server;
    @SuppressLint("StaticFieldLeak")
    public static EditText userInput;
    public static RequestListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View parent = getLayoutInflater().inflate(R.layout.activity_main, null);
        userInput = parent.findViewById(R.id.user_input);
        ListView listView = parent.findViewById(R.id.request_list);
        adapter = new RequestListAdapter(this);
        listView.setAdapter(adapter);

        Intent intent;
        intent = new Intent(getApplicationContext(), HttpService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }

        setContentView(parent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.stop();
        server = null;

        HttpService.stopIfActive(getApplicationContext());
    }
}
