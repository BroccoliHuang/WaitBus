package tw.broccoli.amybus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by broccoli on 15/9/22.
 */
public class MyWidgetProvider extends AppWidgetProvider {
    public final static String WIDGET_LINEARLAYOUT_CLICK = "tw.broccoli.amybus.mywidgetprovider.widget_linearlayout_click";
    public final static String WIDGET_LISTVIEW_CLICK     = "tw.broccoli.amybus.mywidgetprovider.widget_listview_click";
    public final static String WIDGET_ITEM               = "tw.broccoli.amybus.mywidgetprovider.widget_item";
    public final static String WIDGET_COMPLETE           = "tw.broccoli.amybus.mywidgetprovider.widget_complete";

//    private static Context context_receive = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("AmyBus", "MyWidgetProvider onUpdate" + appWidgetIds.length);
        Log.i("AmyBus", "appWidgetIds.length=" + appWidgetIds.length);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent linearlayoutIntent = new Intent(context, MyWidgetProvider.class);
        linearlayoutIntent.setAction(WIDGET_LINEARLAYOUT_CLICK);
        linearlayoutIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent linearlayoutPendingIntent = PendingIntent.getBroadcast(context, 0, linearlayoutIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.linearlayout_widget, linearlayoutPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        for(int i=0;i<appWidgetIds.length;i++) {

            Intent serviceIntent = new Intent(context, WidgetService.class);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.listview_widget, serviceIntent);


            Intent listviewIntent = new Intent(context, MyWidgetProvider.class);
            listviewIntent.setAction(WIDGET_LISTVIEW_CLICK);
            listviewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            PendingIntent listviewPendingIntent = PendingIntent.getBroadcast(context, 0, listviewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.listview_widget, listviewPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onReceive(Context context, final Intent intent) {
        Log.i("AmyBus", "MyWidgetProvider onReceive");
        Log.i("AmyBus", "intent.getAction()=" + intent.getAction());

//        context_receive = context;

        if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(intent.getAction())){

        }else if(WIDGET_LISTVIEW_CLICK.equals(intent.getAction()) ||
                 WIDGET_LINEARLAYOUT_CLICK.equals(intent.getAction())){
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int itemIndex = intent.getIntExtra(WIDGET_ITEM, -1);

            Log.i("AmyBus", "appWidgetId=" + appWidgetId);
            Log.i("AmyBus", "itemIndex=" + itemIndex);


            WidgetService.setCallback(new WidgetService.Callback() {
                @Override
                public RemoteViewsService onStartCommand() {
                    Log.i("AmyBus", "ready to refreshBus");
                    WidgetService.refreshBus(true);
                    return null;
                }
            });

            context.startService(new Intent(context, WidgetService.class));
//            WidgetService.setListBus(BusDBHelper.getAllStop());
//
//            if(!WidgetService.isListBusEmpty()){
//                showBusTimeOnBoard(context, WidgetService.getBus(0));
//            }

        }else if(WIDGET_COMPLETE.equals(intent.getAction())){
//            String returnText = intent.getStringExtra(WIDGET_RETURN_TEXT);
//            if(returnText != null && !"".equals(returnText)){
//                Log.i("AmyBus", "returnText=" + returnText);
//                WidgetListViewsFactory.addText(returnText);
//
//                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MyWidgetProvider.class.getName());
//                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
//
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget);
//            }
//            WidgetService.refreshBus(false);
////            WidgetService.removeListBus(0);
////            if(!WidgetService.isListBusEmpty()) showBusTimeOnBoard(context, WidgetService.getBus(0));
        }

        super.onReceive(context, intent);
    }

//    private void showBusTimeOnBoard(Context context, Bus bus){
//        final WebView wb = new WebView(context);
//        wb.getSettings().setJavaScriptEnabled(true);
//        wb.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
//        wb.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                wb.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
//            }
//        });
//
//        Log.i("AmyBus", "loadUrl = http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid=" + bus.getRid() + "&sec=" + bus.getDirectParam());
//        wb.loadUrl("http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid=" + bus.getRid() + "&sec=" + bus.getDirectParam());
//    }
//
//    private class MyJavaScriptInterface {
//        @JavascriptInterface
//        public void showHTML(String html) {
//            Bus bus = WidgetService.getBus(html.substring(html.indexOf("rid=") + 4, html.indexOf("&amp", html.indexOf("rid=") + 4)));
//            if(bus != null) {
//                Map map = new HashMap();
//                map.put(ParseBusAsyncTask.KEY_BUS, bus);
//                map.put(ParseBusAsyncTask.KEY_URL, html);
//                new ParseBusAsyncTask().execute(map);
//            }
//        }
//    }
}