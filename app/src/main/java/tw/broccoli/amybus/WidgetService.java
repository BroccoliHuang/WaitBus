package tw.broccoli.amybus;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViewsService;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by broccoli on 15/9/30.
 */
@SuppressLint("SetJavaScriptEnabled")
public class WidgetService extends RemoteViewsService {

    private static WidgetListViewsFactory mWidgetListViewsFactory = null;
    private static RemoteViewsService me = null;
    private static NotificationManager mNotificationManager = null;
    private static Handler handler = new Handler();
    private static boolean isRunning = false;
    private static boolean isNeedInitialByClickWidgetBoard = false;
    private static boolean isAlarming = false;
    private static List<Bus> mListBus = null;
    private static int mQueueCount = -1;
    private static MyJavaScriptInterface mMyJavaScriptInterface = null;
    private static Vibrator mVibrator = null;
    private static Ringtone mRingtone = null;

    public enum RefreshBus{
        INITIAL,
        AFTER_FIRST,
        JUST_REFRESH
    }
    public enum AlarmState{
        ON,
        OFF,
        TOGGLE
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("AmyBus", "WidgetService onGetViewFactory");
        mWidgetListViewsFactory = new WidgetListViewsFactory(WidgetService.this, intent);
        return mWidgetListViewsFactory;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AmyBus", "WidgetService onStartCommand");
        if(isRunning) {
            Log.i("AmyBus", "isRunning");
            check();
            return -1;
        }else{
            Log.i("AmyBus", "is not Running");
            me = this;
            mMyJavaScriptInterface = new MyJavaScriptInterface();
            isNeedInitialByClickWidgetBoard = true;
            showTime.run();
            int returnValue = super.onStartCommand(intent, flags, startId);
            return returnValue;
        }
    }

    private static Runnable showTime = new Runnable() {
        public void run() {
            Log.i("AmyBus", "WidgetService showTime");
            isRunning = true;

            boolean isInitial = false;
            List<Map<String, String>> list_text = null;

            if(mWidgetListViewsFactory != null) {
                list_text = mWidgetListViewsFactory.getListText();
                for (Map<String, String> listItem : list_text) {
                    if (String.valueOf(R.color.red).equals(listItem.get(WidgetListViewsFactory.KEY_COLOR))) {
                        isInitial = true;
                        break;
                    }
                }
            }
            if(isInitial || list_text==null || list_text.size()==0 || isNeedInitialByClickWidgetBoard){
                isNeedInitialByClickWidgetBoard = false;
                refreshBus(RefreshBus.INITIAL);
            }else{
                refreshBus(RefreshBus.JUST_REFRESH);
            }
        }
    };

    private static void check(){
        Log.i("AmyBus", "check");
        boolean isNeedStopService = true;
        if(mWidgetListViewsFactory==null) return;
        List<Map<String, String>> list_text = mWidgetListViewsFactory.getListText();
        for(Map<String, String> listItem : list_text){
            if(String.valueOf(R.color.red).equals(listItem.get(WidgetListViewsFactory.KEY_COLOR))){
                isNeedStopService = false;
                String text = listItem.get(WidgetListViewsFactory.KEY_TEXT);

                String time = text.substring(text.lastIndexOf(" ", text.indexOf(" 到 ") - 1) + 1, text.indexOf(" 到 "));
                Bus bus = Bus.getBus(text);

                Alarm alarm = BusDBHelper.getAlarm(bus);
                if(alarm != null){
                    if(time.contains("約") && time.contains("分")){
                        if(Integer.valueOf(time.replace("約", "").replace("分", ""))<=Integer.valueOf(alarm.getMinute())){
                            Log.i("AmyBus", "約＝"+Integer.valueOf(time.replace("約", "").replace("分", "")));
                            Log.i("AmyBus", "getMinute＝" + Integer.valueOf(alarm.getMinute()));

                            alarm(text, bus.getNumber() + " - " + bus.getDirectText(), time + " 到 " + bus.getOnBus(), alarm, true);
                        }else{
                            handler.postDelayed(showTime, (DebugController.isDebug()?5000:20000));
                        }
                    }else if(time.contains("未發車")){
                        handler.postDelayed(showTime, (DebugController.isDebug()?5000:60000));
                    }else if(time.contains("將到站")){
                        alarm(text, bus.getNumber() + " - " + bus.getDirectText(), time + " 到 " + bus.getOnBus(), alarm, true);
    //                    handler.postDelayed(showTime, 5000);
                    }else if(time.contains("-")){//已到站
                        alarm(text, bus.getNumber() + " - " + bus.getDirectText(), time + " 到 " + bus.getOnBus(), alarm, true);
    //                    handler.postDelayed(showTime, 5000);
                    }else if(time.contains("已過")){//末班車 已過
                        setWidgetListTextColor(text, android.R.color.black);
                        MaterialDialog md = new MaterialDialog.Builder(Common.getAppContext())
                                .title("末班車已過")
                                .content("快點叫計程車！要注意安全喔！")
                                .positiveText("關閉")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        check();
                                    }
                                })
                                .build();
                        md.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//設定提示框為系統提示框
                        md.show();
                    }
                }
            }
        }
        if(isNeedStopService) me.stopSelf();
    }

    private static void setWidgetListTextColor(String text, int color){
        if(mWidgetListViewsFactory!=null) mWidgetListViewsFactory.setTextColor(text, color);
        refreshBus(RefreshBus.JUST_REFRESH);
    }

    public static void setAlarmable(String text, AlarmState alarmState){
        if(mWidgetListViewsFactory==null) return;
        List<Map<String, String>> list_text = mWidgetListViewsFactory.getListText();
        for(int temp=0;temp<list_text.size();temp++){
            if(text.equals(list_text.get(temp).get(WidgetListViewsFactory.KEY_TEXT))){
                if(alarmState == AlarmState.TOGGLE) {
                    if (mWidgetListViewsFactory.getTextColor(temp) == android.R.color.black) {
                        if(BusDBHelper.getAlarm(getBus(temp)) != null) {
                            setWidgetListTextColor(text, R.color.red);
                        }
                    } else if (mWidgetListViewsFactory.getTextColor(temp) == R.color.red) {
                        setWidgetListTextColor(text, android.R.color.black);
                    }
                }
            }
        }
    }

    public static boolean isRunning(){
        return isRunning;
    }

    private static void alarm(@Nullable String originalText, @Nullable String title, @Nullable String content, @Nullable Alarm alarm, boolean start){
        isAlarming = start;
        if(start){
            //set textcolor to black
            setWidgetListTextColor(originalText, android.R.color.black);

            //Notification
            intentCancelNotifiaction(title, content);

            //Vibrate
            if(alarm.getVibrate()) {
                if(mVibrator == null) mVibrator = (Vibrator) Common.getAppContext().getSystemService(Service.VIBRATOR_SERVICE);
                mVibrator.vibrate(new long[]{520, 520}, 0);
            }

            //Ring
            if("default".equals(alarm.getRing())){
                if(mRingtone == null) mRingtone = RingtoneManager.getRingtone(Common.getAppContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                mRingtone.play();
            }else if(!"".equals(alarm.getRing())){
                //TODO custom ring
            }
            MaterialDialog md = new MaterialDialog.Builder(Common.getAppContext())
                    .title(title)
                    .content(content)
                    .positiveText("關閉")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            alarm(null, null, null, null, false);
                            check();
                            super.onPositive(dialog);
                        }
                    })
                    .build();
            md.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//設定提示框為系統提示框
            md.show();
        }else{
            if(mNotificationManager!=null) mNotificationManager.cancelAll();
            if(mVibrator != null) mVibrator.cancel();
            if(mRingtone != null) mRingtone.stop();
        }
    }

    public static void refreshBus(RefreshBus refreshBus){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(Common.getAppContext());
        ComponentName thisAppWidget = new ComponentName(Common.getAppContext().getPackageName(), MyWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        //這行也要先刷新，因為呼叫這支函式的人需要
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget);
        if(refreshBus == RefreshBus.JUST_REFRESH){
            Log.i("AmyBus", "refreshBus JUST_REFRESH");
            if(!isAlarming) check();
            return;
        }else if(refreshBus == RefreshBus.INITIAL) {
            Log.i("AmyBus", "refreshBus INITIAL");
            if(mWidgetListViewsFactory!=null) mWidgetListViewsFactory.cleanText();

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget);

            WidgetService.setListBus(BusDBHelper.getAllStop());
        }else if(refreshBus == RefreshBus.AFTER_FIRST){
            Log.i("AmyBus", "refreshBus AFTER_FIRST");
            mQueueCount++;
        }

        if(mListBus.size()==mQueueCount) {
            check();
        }else if(mListBus.size()>mQueueCount) {
            showBusTimeOnBoard(WidgetService.getBus(mQueueCount));
        }
    }

    private static void showBusTimeOnBoard(Bus bus){
        if(me==null) return;
        final WebView wb = new WebView(me);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.addJavascriptInterface(mMyJavaScriptInterface, "HtmlViewer");
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wb.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        Log.i("AmyBus", "loadUrl = http://e-bus.tpc.gov.tw/" + bus.getRid() + "&sec=" + bus.getDirectParam());
        wb.loadUrl("http://e-bus.tpc.gov.tw/" + bus.getRid() + "&sec=" + bus.getDirectParam());
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            Bus bus = WidgetService.getBus(html.substring(html.indexOf("rid=") + 4, html.indexOf("&amp", html.indexOf("rid=") + 4)));
            if(bus != null) {
                Map map = new HashMap();
                map.put(ParseBusAsyncTask.KEY_BUS, bus);
                map.put(ParseBusAsyncTask.KEY_URL, html);
                new ParseBusAsyncTask().execute(map);
            }
        }
    }

    public static void setListBus(List<Bus> listBus){
        mListBus = listBus;
        mQueueCount = 0;
    }

    public static Bus getBus(int position){
        if(mListBus!=null && mListBus.size()!=0) {
            return mListBus.get(position);
        }else{
            return null;
        }
    }
    public static Bus getBus(String rid){
        if(mListBus!=null && mListBus.size()!=0) {
            for (int temp = 0; temp < mListBus.size(); temp++) {
                if (mListBus.get(temp).getRid().contains(rid)) return getBus(temp);
            }
        }
        return null;
    }

    private static void intentCancelNotifiaction(String title, String content){
        Log.i("AmyBus", "WidgetService intentCancelNotifiaction");
//        final int notifyID = 1; // 通知的識別號碼


//        final Intent intent = getIntent(); // 目前Activity的Intent
//        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
//        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, flags); // 取得PendingIntent

        final Intent cancelIntent = new Intent(Common.getAppContext(), NotificationReceiver.class); // 取消通知的的Intent
        cancelIntent.putExtra(NotificationReceiver.CANCEL_ID, NotificationReceiver.NOTIFY_ID); // 傳入通知的識別號碼
        int flags = PendingIntent.FLAG_ONE_SHOT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
        final PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(Common.getAppContext(), 0, cancelIntent, flags); // 取得PendingIntent

        mNotificationManager = (NotificationManager) Common.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification;
//        if(Build.VERSION.SDK_INT < 19) {
            notification = new Notification.Builder(Common.getAppContext())
                    .setSmallIcon(R.mipmap.notification_small_icon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "關閉", pendingCancelIntent)
                    .setOngoing(true)
                    .build(); // 建立通知
//        }else{
//            notification = new Notification.Builder(Common.getAppContext())
//                    .setSmallIcon(R.mipmap.notification_small_icon)
//                    .setContentTitle(title)
//                    .setContentText(content)
//                    .addAction(new Notification.Action(android.R.drawable.ic_menu_close_clear_cancel, "關閉", pendingCancelIntent))
//                    .setOngoing(true)
//                    .build(); // 建立通知
//        }
        mNotificationManager.notify(NotificationReceiver.NOTIFY_ID, notification); // 發送通知
    }

    @Override
    public void onDestroy() {
        Log.i("AmyBus", "WidgetService onDestroy");
        isRunning = false;
        alarm(null, null, null, null, false);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static class CompleteReceiver extends BroadcastReceiver {
        public final static String PARSE_COMPLETE = "tw.broccoli.amybus.widgetservice.completereceiver.parse_complete";
        public final static String RETURN_TEXT = "tw.broccoli.amybus.mywidgetprovider.widget_return_text";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("AmyBus", "onReceive");
            if(PARSE_COMPLETE.equals(intent.getAction())){
                String returnText = intent.getStringExtra(RETURN_TEXT);
                if(returnText != null && !"".equals(returnText)) {
                    Log.i("AmyBus", "returnText＝"+returnText);
                    if(mWidgetListViewsFactory!=null) mWidgetListViewsFactory.addText(returnText);
                }
                refreshBus(RefreshBus.AFTER_FIRST);
            }
        }
    }
}