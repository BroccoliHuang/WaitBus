package tw.broccoli.amybus;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Created by broccoli on 15/9/23.
 */
public class Common extends Application {
    private static Context mContext;
//    private static Context mContextMyWidget = null;
//    private static AppWidgetManager mAppWidgetManager = null;
//    private static int[] mAppWidgetIds = null;
//    private static String mText = null;



    @Override
    public void onCreate() {
        super.onCreate();
//        Common.mContext = getApplicationContext();
        this.mContext = this;
        overrideFont(getApplicationContext(), "SERIF", "fonts/erh_feng.ttc");
    }

//    public static Context getAppContext() {
//        return Common.context;
//    }

//    public static synchronized void setContextMyWidget(Context contextMyWidget){
//        mContextMyWidget = contextMyWidget;
//    }

//    public static synchronized void setAppWidgetManager(AppWidgetManager appWidgetManager){
//        mAppWidgetManager = appWidgetManager;
//    }

//    public static synchronized void setAppWidgetIds(int[] appWidgetIds){
//        mAppWidgetIds = appWidgetIds;
//    }

//    public static synchronized void setText(String text){
//        mText = text;
//    }

    public static synchronized Context getAppContext(){
        return mContext;
    }

//    public static synchronized AppWidgetManager getAppWidgetManager(){
//        return mAppWidgetManager;
//    }

//    public static synchronized int[] getAppWidgetIds(){
//        return mAppWidgetIds;
//    }

//    public static synchronized String getText(){
//        return mText;
//    }

    public static synchronized String getUrl(String direct){
        return "http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid=184&sec=" + direct;
    }


    /**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     * @param context to work with assets
     * @param defaultFontNameToOverride for example "monospace"
     * @param customFontFileNameInAssets file name of the font from assets
     */
    public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);


            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {

        }
    }
}
