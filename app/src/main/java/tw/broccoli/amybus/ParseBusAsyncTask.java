package tw.broccoli.amybus;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;

/**
 * Created by broccoli on 15/10/1.
 */
public class ParseBusAsyncTask extends AsyncTask<Map<String, Object>, Void, String> {
    public static final String KEY_BUS = "key_bus";
    public static final String KEY_URL = "key_url";

    @Override
    protected String doInBackground(final Map<String, Object>... params) {
        final Document doc = Jsoup.parse(String.valueOf(params[0].get(KEY_URL)));

//        指定站的狀況
        Element element_map = doc.getElementById("map");
        if(element_map != null) {
            for (Element element_target : element_map.getElementsMatchingText(((Bus)params[0].get(KEY_BUS)).getOnBus())) {
                if ("center".equals(element_target.className())) {
                    return ((Bus)params[0].get(KEY_BUS)).getNumber() + " - " + ((Bus)params[0].get(KEY_BUS)).getDirectText() + " " + element_target.parent().parent().parent().children().first().children().first().text() + " 到 " + ((Bus)params[0].get(KEY_BUS)).getOnBus();
                }
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        Log.i("AmyBus", "onPostExecute");
        super.onPostExecute(s);
        if(s != null && !"".equals(s)) {
            Intent intent = new Intent(WidgetService.CompleteReceiver.PARSE_COMPLETE);
            intent.putExtra(WidgetService.CompleteReceiver.RETURN_TEXT, s);
            Common.getAppContext().sendBroadcast(intent);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
