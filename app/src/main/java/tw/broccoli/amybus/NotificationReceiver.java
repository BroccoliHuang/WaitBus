package tw.broccoli.amybus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

/**
 * Created by Broccoli on 2015/9/27.
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String CANCEL_ID = "cancel_notify_id";
    public static final int NOTIFY_ID = 22508750;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("AmyBus", "NotificationReceiver onReceive");
        final int notifyID = intent.getIntExtra(CANCEL_ID, 0);
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        notificationManager.cancel(notifyID);

        context.stopService(new Intent(context, WidgetService.class));
    }
}