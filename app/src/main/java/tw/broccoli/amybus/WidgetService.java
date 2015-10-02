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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by broccoli on 15/9/30.
 */
@SuppressLint("SetJavaScriptEnabled")
public class WidgetService extends RemoteViewsService {

    private WidgetListViewsFactory mWidgetListViewsFactory = null;
    private static RemoteViewsService me = null;
    private static Callback mCallback = null;
    private static Handler handler = new Handler();
    private static boolean isRunning = false;
    private static List<Bus> mListBus = null;
    private static MyJavaScriptInterface mMyJavaScriptInterface = null;
    private static Vibrator mVibrator = null;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("AmyBus", "WidgetService onGetViewFactory");
        mWidgetListViewsFactory = new WidgetListViewsFactory(this.getApplicationContext(), intent);
        return mWidgetListViewsFactory;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AmyBus", "WidgetService onStartCommand");
        if(isRunning) {
            if(mCallback != null) mCallback.onStartCommand();
            return 0;
        }else{
            me = this;
            mMyJavaScriptInterface = new MyJavaScriptInterface();
            showTime.run();
        }
        int returnValue = super.onStartCommand(intent, flags, startId);
        if(mCallback != null) mCallback.onStartCommand();
        return returnValue;
    }

    public static void setCallback(Callback callback){
        mCallback = callback;
    }

    private static Runnable showTime = new Runnable() {
        public void run() {
            Log.i("AmyBus", "WidgetService showTime");
            isRunning = true;
            refreshBus(true);
        }
    };

    private static void check(){
        List<Map<String, String>> list_text = WidgetListViewsFactory.getListText();
        for(Map<String, String> listItem : list_text){
            if(String.valueOf(R.color.red).equals(listItem.get(WidgetListViewsFactory.KEY_COLOR))){
                String text = listItem.get(WidgetListViewsFactory.KEY_TEXT);
                String bus = text.substring(0, text.indexOf(" - "));
                String direct_text = text.substring(text.indexOf("往 "), text.indexOf(" ", text.indexOf("往 ") + 2));
                String time = text.substring(text.lastIndexOf(" ", text.indexOf(" 到 ") - 1) + 1, text.indexOf(" 到 "));
                String onbus = text.substring(text.indexOf(" 到 ")+3);

//                Log.i("AmyBus", "bus="+bus+"=");
//                Log.i("AmyBus", "direct_text="+direct_text+"=");
//                Log.i("AmyBus", "time="+time+"=");
//                Log.i("AmyBus", "onbus="+onbus+"=");

//                if(time.contains("約") && time.contains("分")){
                if(time.contains("約") && time.contains("分")){
                    Alarm alarm = BusDBHelper.getAlarm(new Bus(bus, "", "", direct_text, onbus, ""));

                    if(alarm != null){
                        if(Integer.valueOf(time.replace("約", "").replace("分", ""))<=Integer.valueOf(alarm.getMinute())){
                            Log.i("AmyBus", "約＝"+Integer.valueOf(time.replace("約", "").replace("分", "")));
                            Log.i("AmyBus", "getMinute＝" + Integer.valueOf(alarm.getMinute()));

                            alarm(true);
                        }
                    }
                }else if(time.contains("未發車")){
                    handler.postDelayed(showTime, 5000);
                }else if(time.contains("將到站")){
                    handler.postDelayed(showTime, 5000);
                }else if(time.contains("-")){
                    //已到站
                    handler.postDelayed(showTime, 5000);
                }else if(time.contains("已過")){
                    //末班車 已過
                    Log.i("AmyBus", "末班車 已過");
                    alarm(true);
                }

            }
        }
    }

    private static void alarm(boolean start){
        if(mVibrator == null) mVibrator = (Vibrator)Common.getAppContext().getSystemService(Service.VIBRATOR_SERVICE);
        if(start){
            intentCancelNotifiaction();
            mVibrator.vibrate(new long[]{520, 520}, 0);

            MaterialDialog md = new MaterialDialog.Builder(Common.getAppContext())
                    .title("title")
                    .content("content")
                    .build();
            md.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//設定提示框為系統提示框
            md.show();
        }else{
            mVibrator.cancel();
        }
    }

    public static void refreshBus(boolean initial){
        Log.i("AmyBus", "refreshBus");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(Common.getAppContext());
        ComponentName thisAppWidget = new ComponentName(Common.getAppContext().getPackageName(), MyWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        //這行也要先刷新，因為呼叫這支函式的人需要
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget);

        if(initial) {
            WidgetListViewsFactory.cleanText();

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget);

            WidgetService.setListBus(BusDBHelper.getAllStop());
        }else{
            WidgetService.removeListBus(0);
        }

        if(WidgetService.isListBusEmpty()) {
            check();
        }else{
            showBusTimeOnBoard(WidgetService.getBus(0));
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
    }

    public static void removeListBus(int position){
        if(!isListBusEmpty()) mListBus.remove(position);
    }

    public static Bus getBus(int position){
        if(!isListBusEmpty()) {
            return mListBus.get(position);
        }else{
            return null;
        }
    }
    public static Bus getBus(String rid){
        if(!isListBusEmpty()) {
            for (int temp = 0; temp < mListBus.size(); temp++) {
                if (mListBus.get(temp).getRid().contains(rid)) return getBus(temp);
            }
        }
        return null;
    }

    public static boolean isListBusEmpty(){
        return (mListBus==null || mListBus.size()==0);
    }

    private static void intentCancelNotifiaction(){
        Log.i("AmyBus", "WidgetService intentCancelNotifiaction");
//        final int notifyID = 1; // 通知的識別號碼


//        final Intent intent = getIntent(); // 目前Activity的Intent
//        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
//        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, flags); // 取得PendingIntent

        final Intent cancelIntent = new Intent(Common.getAppContext(), NotificationReceiver.class); // 取消通知的的Intent
        cancelIntent.putExtra(NotificationReceiver.CANCEL_ID, NotificationReceiver.NOTIFY_ID); // 傳入通知的識別號碼
        int flags = PendingIntent.FLAG_ONE_SHOT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
        final PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(Common.getAppContext(), 0, cancelIntent, flags); // 取得PendingIntent

        final NotificationManager notificationManager = (NotificationManager) Common.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification;
        if(Build.VERSION.SDK_INT < 19) {
            notification = new Notification.Builder(Common.getAppContext()).setSmallIcon(R.mipmap.notification_small_icon).setContentTitle("內容標題").setContentText("內容文字").addAction(android.R.drawable.ic_menu_close_clear_cancel, "關閉通知", pendingCancelIntent).build(); // 建立通知
        }else{
            notification = new Notification.Builder(Common.getAppContext()).setSmallIcon(R.mipmap.notification_small_icon).setContentTitle("內容標題").setContentText("內容文字").addAction(new Notification.Action(android.R.drawable.ic_menu_close_clear_cancel, "關閉通知", pendingCancelIntent)).build(); // 建立通知
        }
        notificationManager.notify(NotificationReceiver.NOTIFY_ID, notification); // 發送通知
    }

    @Override
    public void onDestroy() {
        Log.i("AmyBus", "WidgetService onDestroy");
        isRunning = false;
        alarm(false);
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
                if(returnText != null && !"".equals(returnText)){
                    Log.i("AmyBus", "returnText＝"+returnText);
                    WidgetListViewsFactory.addText(returnText);
                }
                refreshBus(false);
            }
        }
    }

    public interface Callback{
        RemoteViewsService onStartCommand();
    }
}