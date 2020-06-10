package jp.co.chooyan.tcpdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class OnBootReceiver extends BaseOnBootReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        try{
            if(intent.getAction().equals(intent.ACTION_LOCKED_BOOT_COMPLETED) || intent.getAction().equals(intent.ACTION_BOOT_COMPLETED)){
                super.onReceive(context, intent);
                onDeviceBoot(context);
            }
        }catch(Exception ex){
            // Try-catch because there is a possibility of equalization. When excepion comes out, it doesn't start.
        }
    }

    @Override
    protected void onDeviceBoot(Context context){
        HttpService.deviceBootStart = true;
        Intent intent;
        intent = new Intent(context, HttpService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else{
            context.startService(intent);
        }
    }
}
