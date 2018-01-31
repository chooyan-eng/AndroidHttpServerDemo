package jp.co.chooyan.tcpdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chooyan-eng on 18/01/31.
 */

public class RequestListAdapter extends BaseAdapter {
    private List<HttpRequest> requests;
    private Context context;

    public RequestListAdapter(Context context) {
        this.context = context;
        requests = new ArrayList<>();
    }

    public void add(HttpRequest request) {
        requests.add(request);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return requests.size();
    }

    @Override
    public Object getItem(int i) {
        return requests.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.layout_request_item, viewGroup, false);
        }
        TextView timeView = view.findViewById(R.id.request_time);
        TextView startLineView = view.findViewById(R.id.request_startline);
        TextView headerLinesView = view.findViewById(R.id.request_headerlines);

        HttpRequest request = requests.get(i);

        timeView.setText(new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Calendar.getInstance().getTime()));
        startLineView.setText(request.getMethod() + " " + request.getRequestTarget() + " " + request.getHttpVersion());
        headerLinesView.setText(request.getHeaders().get("User-Agent"));

        return view;
    }
}
