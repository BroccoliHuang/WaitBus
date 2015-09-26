package tw.broccoli.amybus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Created by broccoli on 15/9/22.
 */
public class MyWidget extends AppWidgetProvider {
    public final static String BOARD_CLICK = "tw.broccoli.amybus.board_click";

    private Context context_receive = null;
    private List<Bus> queue_bus = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("AmyBus", "onUpdate");

        if(Common.getContextMyWidget() == null) Common.setContextMyWidget(context);
        if(Common.getAppWidgetManager() == null) Common.setAppWidgetManager(appWidgetManager);
        if(Common.getAppWidgetIds() == null) Common.setAppWidgetIds(appWidgetIds);

        RemoteViews updateViews = new RemoteViews(Common.getContextMyWidget().getPackageName(), R.layout.widget);

        updateViews.setOnClickPendingIntent(R.id.board, PendingIntent.getBroadcast(Common.getContextMyWidget(), 0, new Intent(BOARD_CLICK), 0));

        if(Common.getText() != null){
            updateViews.setTextViewText(R.id.board, Common.getText());
        }

        Common.getAppWidgetManager().updateAppWidget(Common.getAppWidgetIds(), updateViews);
    }

    private void setText(String s){
        if("".equals(s) || s == null){
            Common.setText("");
        }else {
            Common.setText((Common.getText() == null ? "" : Common.getText()) + "\n" + s);
        }
        onUpdate(Common.getContextMyWidget(), Common.getAppWidgetManager(), Common.getAppWidgetIds());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onReceive(Context context, final Intent intent) {
        super.onReceive(context, intent);

        context_receive = context;

        if(intent.getAction().equals(BOARD_CLICK)){
            setText("");
        }else{
            return;
        }

        queue_bus = BusDBHelper.getAllStop(context);
        showBusTime();
    }

    private void showBusTime(){
        final WebView wb = new WebView(context_receive);

        wb.getSettings().setJavaScriptEnabled(true);
        wb.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wb.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        wb.loadUrl("http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid="+queue_bus.get(0).getRid()+"&sec="+queue_bus.get(0).getDirectParam());
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            new getBoard().execute(html);
//            new getBoard().execute(new String[]{html, "捷運新埔站", "1"});
        }
    }

    private class getBoard extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(final String... params) {
            Log.i("AmyBus", "doInBackground");
            final Document doc = Jsoup.parse(params[0]);

//          指定站的狀況
            Element element_map = doc.getElementById("map");
            for(Element element_target : element_map.getElementsMatchingText(queue_bus.get(0).getOnBus())){
                if("center".equals(element_target.className())) {
                    return queue_bus.get(0).getNumber() + " - " + queue_bus.get(0).getDirectText() + " " + element_target.parent().parent().parent().children().first().children().first().text() + " 到 " + queue_bus.get(0).getOnBus();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("AmyBus", "onPostExecute");
            super.onPostExecute(s);
            Log.i("AmyBus", "s="+s);
            if(s != null && !"".equals(s)) {
                Log.i("AmyBus", "setTextViewText");


                setText(s);

                queue_bus.remove(0);

                if(queue_bus.size()>0) showBusTime();

//                initialUpdateViews(s);

//                Intent intent = new Intent(mContext, MyWidget.class);
//                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
//// since it seems the onUpdate() is only fired on that:
////                int[] ids = {widgetId};
//                int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new omponentName(mContext, MyWidget.class));
//                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//                Common.getInstance().sendBroadcast(intent);
//                Log.i("AmyBus", "sendBroadcast");

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}