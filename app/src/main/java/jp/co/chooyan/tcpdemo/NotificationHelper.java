package jp.co.chooyan.tcpdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

public class NotificationHelper extends ContextWrapper {
    // Notification channel ID
    private final String CHANNEL_ID = getString(R.string.http_service_channel_id);
    private NotificationManager notificationManager;

    public NotificationHelper(Context context){
        super(context);

        if(isOreoOrLater()){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.http_service_name), NotificationManager.IMPORTANCE_DEFAULT);
            getManager().createNotificationChannel(notificationChannel);
        }
    }

    public Notification.Builder getNotification(){
        Notification.Builder builder = isOreoOrLater() ?
                new Notification.Builder(this, CHANNEL_ID) :
                new Notification.Builder(this);

        return builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.http_service_text));
    }

    private NotificationManager getManager(){
        if(notificationManager == null){
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private boolean isOreoOrLater(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
