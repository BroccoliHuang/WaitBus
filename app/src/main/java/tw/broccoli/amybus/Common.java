package tw.broccoli.amybus;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;

/**
 * Created by broccoli on 15/9/23.
 */
public class Common extends Application {
//    private static Common mInstance = null;

    public static enum Direct{
        Go,
        Back
    }

    private static Context mContextMyWidget = null;
    private static AppWidgetManager mAppWidgetManager = null;
    private static int[] mAppWidgetIds = null;
    private static String mText = null;



//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mInstance = this;
//        Log.i("AmyBus", "onCreate");
//    }
//
//    public static synchronized Common getInstance() {
//        return mInstance;
//    }

    public static synchronized void setContextMyWidget(Context contextMyWidget){
        mContextMyWidget = contextMyWidget;
    }

    public static synchronized void setAppWidgetManager(AppWidgetManager appWidgetManager){
        mAppWidgetManager = appWidgetManager;
    }

    public static synchronized void setAppWidgetIds(int[] appWidgetIds){
        mAppWidgetIds = appWidgetIds;
    }

    public static synchronized void setText(String text){
        mText = text;
    }

    public static synchronized Context getContextMyWidget(){
        return mContextMyWidget;
    }

    public static synchronized AppWidgetManager getAppWidgetManager(){
        return mAppWidgetManager;
    }

    public static synchronized int[] getAppWidgetIds(){
        return mAppWidgetIds;
    }

    public static synchronized String getText(){
        return mText;
    }

    public static synchronized String getUrl(String direct){
        return "http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid=184&sec=" + direct;
    }
}
