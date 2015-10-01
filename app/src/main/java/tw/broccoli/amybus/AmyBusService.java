package tw.broccoli.amybus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by Broccoli on 2015/9/27.
 */
public class AmyBusService extends Service {
    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    @Override
//    public void onStart(Intent intent, int startId) {
//        handler.postDelayed(showTime, 1000);
//        super.onStart(intent, startId);
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showTime.run();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(showTime);
        super.onDestroy();
    }

    private Runnable showTime = new Runnable() {
        public void run() {
            handler.postDelayed(showTime, 5000);

            //TODO update MyWidgetProvider

            intentCancelNotifiaction();
        }
    };

    private void intentCancelNotifiaction(){
        final int notifyID = 1; // 通知的識別號碼


//        final Intent intent = getIntent(); // 目前Activity的Intent
//        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
//        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, flags); // 取得PendingIntent

        final Intent cancelIntent = new Intent(getApplicationContext(), NotificationReceiver.class); // 取消通知的的Intent
        cancelIntent.putExtra("cancel_notify_id", notifyID); // 傳入通知的識別號碼
        int flags = PendingIntent.FLAG_ONE_SHOT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
        final PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cancelIntent, flags); // 取得PendingIntent

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification;
        if(Build.VERSION.SDK_INT < 19) {
            notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.mipmap.notification_small_icon).setContentTitle("內容標題").setContentText("內容文字").addAction(android.R.drawable.ic_menu_close_clear_cancel, "關閉通知", pendingCancelIntent).build(); // 建立通知
        }else{
            notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.mipmap.notification_small_icon).setContentTitle("內容標題").setContentText("內容文字").addAction(new Notification.Action(android.R.drawable.ic_menu_close_clear_cancel, "關閉通知", pendingCancelIntent)).build(); // 建立通知
        }
        notificationManager.notify(notifyID, notification); // 發送通知
    }
}