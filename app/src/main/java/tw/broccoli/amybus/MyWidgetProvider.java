package tw.broccoli.amybus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by broccoli on 15/9/22.
 */
public class MyWidgetProvider extends AppWidgetProvider {
    public final static String WIDGET_LINEARLAYOUT_CLICK = "tw.broccoli.amybus.mywidgetprovider.widget_linearlayout_click";
    public final static String WIDGET_LISTVIEW_CLICK     = "tw.broccoli.amybus.mywidgetprovider.widget_listview_click";
    public final static String WIDGET_ITEM_INDEX         = "tw.broccoli.amybus.mywidgetprovider.widget_item_index";
    public final static String WIDGET_ITEM_TEXT          = "tw.broccoli.amybus.mywidgetprovider.widget_item_text";
    public final static String WIDGET_COMPLETE           = "tw.broccoli.amybus.mywidgetprovider.widget_complete";

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

        if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(intent.getAction())){

        }else if(WIDGET_LISTVIEW_CLICK.equals(intent.getAction()) ||
                 WIDGET_LINEARLAYOUT_CLICK.equals(intent.getAction())){
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int itemIndex = intent.getIntExtra(WIDGET_ITEM_INDEX, -1);
            String itemText = intent.getStringExtra(WIDGET_ITEM_TEXT);

            if(itemIndex == -1) { //按下板子(list以外的地方)
                if(!WidgetService.isRunning()) context.startService(new Intent(context, WidgetService.class));
            }else{
                WidgetService.setAlarmable(itemText, WidgetService.AlarmState.TOGGLE);
            }
        }else if(WIDGET_COMPLETE.equals(intent.getAction())){

        }

        super.onReceive(context, intent);
    }
}