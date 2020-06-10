package jp.co.chooyan.tcpdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class BaseOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        onDeviceBoot(context);
        // NOTE:
        // ・This object disappears at the end of this method
        // ・This method is called in the main thread, and long processing of 10 seconds or more is prohibited.
    }

    protected abstract void onDeviceBoot(Context context);
}
