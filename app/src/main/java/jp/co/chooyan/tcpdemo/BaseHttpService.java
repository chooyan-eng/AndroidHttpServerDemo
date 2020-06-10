package jp.co.chooyan.tcpdemo;

import android.app.Service;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

public abstract class BaseHttpService extends Service {

    protected abstract void start(@Nullable Context context);

    public class BindHttpServiceBinder extends Binder{
        BaseHttpService getService(){
            return BaseHttpService.this;
        }
    }
    protected  final IBinder binder = new BindHttpServiceBinder();

    @Override
    public void onCreate(){
        Context context = this;
        start(context);
    }

    @Override
    public  IBinder onBind(Intent intent){
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return true;
    }

    @Override
    public void onDestroy(){
    }

    public BaseHttpService baseStartHttpService(Context context){
        Intent intent = new Intent(context, this.getClass());
        intent.putExtra("type", "start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else{
            context.startService(intent);
        }

        return this;
    }

    public void stopHttpService(Context context){
        Intent intent = new Intent(context, this.getClass());

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0, // If this is set to -1, it will not succeed in releasing
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        stopSelf();
    }
}
