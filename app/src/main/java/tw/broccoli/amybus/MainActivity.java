package tw.broccoli.amybus;

import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final static String BUS_DIRECT_PARAM_AND_TEXT = "bus_direct_param_and_text";

    private RecyclerView mRecyclerView = null;
    private RecyclerViewAdapter mRecyclerViewAdapter = null;
    private FloatingActionButton mFloatingActionButton = null;

    private List<Bus> mListBus = null;
    private Bus mAddBus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mFloatingActionButton = (FloatingActionButton)findViewById(R.id.floatingactionbutton);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    removeBus(position);
                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    removeBus(position);
                                }
                            }
                        });

        mRecyclerView.addOnItemTouchListener(swipeTouchListener);

        mRecyclerViewAdapter = new RecyclerViewAdapter();

        mListBus = BusDBHelper.getAllStop(MainActivity.this);
        mRecyclerViewAdapter.setListBus(mListBus);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getAllBus().execute();
            }
        });
    }

    private void insertBus(Bus bus){
        BusDBHelper.requestAdd(MainActivity.this, bus);

        if(mListBus == null) mListBus = new ArrayList();
        mRecyclerViewAdapter = new RecyclerViewAdapter();

        mListBus.add(bus);
        mRecyclerViewAdapter.setListBus(mListBus);
        mRecyclerViewAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    private void removeBus(int position){
        if(mListBus == null) return;
        BusDBHelper.requestDelete(MainActivity.this, mListBus.get(position));

        mListBus.remove(position);
        mRecyclerViewAdapter.setListBus(mListBus);
        mRecyclerViewAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    private class getAllBus extends AsyncTask<Void, Void, List<String[]>> {
        @Override
        protected List<String[]> doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://e-bus.tpc.gov.tw/").get();
                Element element_allbus = doc.getElementsByClass("bus").first();

                List<String[]> list = new ArrayList();

                for(Element element_bus : element_allbus.select("li")){
                    String href = element_bus.child(0).attr("href");
                    list.add(new String[]{element_bus.text(), href.substring(href.indexOf("rid=") + 4, href.indexOf("&sec="))});
                }
                return list;
            }catch(IOException ioe){
                return null;
            }
        }

        @Override
        protected void onPostExecute(final List<String[]> list) {
            if(list==null) return;
            super.onPostExecute(list);

            List<String> dialogBusNumber = new ArrayList<>();
            for(int temp = 0 ; temp<list.size() ; temp++){
                dialogBusNumber.add(list.get(temp)[0]);
            }

            new MaterialDialog.Builder(MainActivity.this)
                    .title("公車們")
                    .items(dialogBusNumber.toArray(new String[]{}))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            mAddBus = new Bus(text.toString(), list.get(which)[1], null, null, null);
                            new getDirect().execute();
                        }
                    })
                    .positiveText("取消")
                    .show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private class getDirect extends AsyncTask<Void, Void, Map<String, List<String[]>>> {
        @Override
        protected Map<String, List<String[]>> doInBackground(final Void... params) {
            try {
                Map<String, List<String[]>> map = new HashMap();
                List<String[]> list = new ArrayList();

                Document doc = Jsoup.connect("http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid=" + mAddBus.getRid() + "&sec=0").get();

                Element direct = doc.getElementsByClass("route_view_wrapper").first().child(0).child(0).child(0);

                list.clear();
                list.add(new String[]{direct.child(0).attr("href").substring(direct.child(0).attr("href").indexOf("&sec=")+5), direct.child(0).text()});
                list.add(new String[]{direct.child(1).attr("href").substring(direct.child(1).attr("href").indexOf("&sec=")+5), direct.child(1).text()});

                map.put(BUS_DIRECT_PARAM_AND_TEXT, list);

                return map;
            }catch(IOException ioe){
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Map<String, List<String[]>> map) {
            if(map==null) return;
            super.onPostExecute(map);

            List<String> dialogDirect = new ArrayList<>();
            for(int temp = 0 ; temp<map.get(BUS_DIRECT_PARAM_AND_TEXT).size() ; temp++){
                dialogDirect.add(map.get(BUS_DIRECT_PARAM_AND_TEXT).get(temp)[1]);
            }

            new MaterialDialog.Builder(MainActivity.this)
                    .title("方向")
                    .items(dialogDirect.toArray(new String[]{}))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            mAddBus.setDirectParam(map.get(BUS_DIRECT_PARAM_AND_TEXT).get(which)[0]);
                            mAddBus.setDirectText(text.toString());

                            loadHtmlSource(mAddBus);
                        }
                    })
                    .positiveText("取消")
                    .show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private void loadHtmlSource(Bus bus){
        final WebView wb = new WebView(MainActivity.this);

        wb.getSettings().setJavaScriptEnabled(true);
        wb.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wb.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        wb.loadUrl("http://e-bus.tpc.gov.tw/NTPCRoute/Tw/Map?rid="+bus.getRid()+"&sec="+bus.getDirectParam());
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void showHTML(String html) {
            new getAllStop().execute(new String[]{html});
        }
    }

    private class getAllStop extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            final Document doc = Jsoup.parse(params[0]);

//          指定站的狀況
            Element element_map = doc.getElementById("map");
            List<String> list = new ArrayList();
            for(Element element_stop : element_map.getElementsByAttributeValue("class", "center")) {
                list.add(element_stop.text());
            }
            return list.toArray(new String[]{});
        }

        @Override
        protected void onPostExecute(String[] stringArray) {
            if(stringArray==null) return;
            super.onPostExecute(stringArray);

            new MaterialDialog.Builder(MainActivity.this)
                    .title("方向")
                    .items(stringArray)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            mAddBus.setOnBus(text.toString());
                            insertBus(mAddBus);
                        }
                    })
                    .positiveText("取消")
                    .show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
