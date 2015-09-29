package tw.broccoli.amybus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Broccoli on 2015/9/27.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final int notifyID = intent.getIntExtra("cancel_notify_id", 0);
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        notificationManager.cancel(notifyID);

        context.stopService(new Intent(context, AmyBusService.class));
    }

}