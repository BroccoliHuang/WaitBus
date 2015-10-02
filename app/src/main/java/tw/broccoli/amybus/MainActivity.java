package tw.broccoli.amybus;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.Callback{
    private final static String BUS_DIRECT_PARAM_AND_TEXT = "bus_direct_param_and_text";

    private ImageView mImageViewBackground = null;
    private AppBarLayout mAppBarLayout = null;
    private Toolbar mToolbar = null;

    private ImageView mImageViewCollapsing = null;
    private RecyclerView mRecyclerView = null;
    private RecyclerViewAdapter mRecyclerViewAdapter = null;
    private ImageView mImageviewAdd = null;
    private ImageView mImageViewHanging = null;
    private MaterialDialog mMaterialDialogProgress = null;

    private List<Bus> mListBus = null;
    private Bus mAddBus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mImageViewBackground = (ImageView)findViewById(R.id.main_activity_background);
        mAppBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mImageViewCollapsing = (ImageView)findViewById(R.id.imageview_collapsing);
        mImageviewAdd = (ImageView)findViewById(R.id.imageview_add);
        mImageViewHanging = (ImageView)findViewById(R.id.imageview_hanging);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);

        //為了讓RecyclerView可以android:layout_height="wrap_content"
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mRecyclerView, LinearLayoutManager.VERTICAL, false);
        layoutManager.setOverScrollMode(ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));


        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int y) {
                int height = appBarLayout.getHeight() - mToolbar.getHeight();
                mImageViewBackground.setAlpha(1 - Float.valueOf(y + height) / Float.valueOf(height));
            }
        });

        int[] titleImage = {R.mipmap.main_title_ya, R.mipmap.main_title_pipi, R.mipmap.main_title_looksky};
        mImageViewCollapsing.setImageResource(titleImage[new Random().nextInt(titleImage.length)]);

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

        mRecyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this);

        mListBus = BusDBHelper.getAllStop();
        mRecyclerViewAdapter.setListBus(mListBus);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        resetImageViewHanging();

        mImageviewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getAllBus().execute();
            }
        });

        initToolbar();
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle("Amy Bus");
        collapsingToolbar.setCollapsedTitleGravity(Gravity.RIGHT);
        collapsingToolbar.setCollapsedTitleTextColor(Color.BLACK);
        collapsingToolbar.setExpandedTitleGravity(Gravity.LEFT | Gravity.BOTTOM);
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
    }

    private void insertBus(Bus bus){
        BusDBHelper.requestAdd(bus);

        if(mListBus == null) mListBus = new ArrayList();
        mRecyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this);

        mListBus.add(bus);
        mRecyclerViewAdapter.setListBus(mListBus);
        mRecyclerViewAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        resetImageViewHanging();
    }

    private void removeBus(int position){
        if(mListBus == null) return;
        BusDBHelper.requestDelete(mListBus.get(position));

        mListBus.remove(position);
        mRecyclerViewAdapter.setListBus(mListBus);
        mRecyclerViewAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        resetImageViewHanging();
    }

    private void resetImageViewHanging(){
        if(mListBus==null || mListBus.size()==0){
            mImageViewHanging.setVisibility(View.GONE);
        }else{
            mImageViewHanging.setVisibility(View.VISIBLE);
        }
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
                    list.add(new String[]{element_bus.text(), href.substring(href.indexOf("../") + 3, href.indexOf("&sec="))});
                }
                return list;
            }catch(IOException ioe){
                return null;
            }
        }

        @Override
        protected void onPostExecute(final List<String[]> list) {
            showProgressDialog(false);
            if(list==null) return;
            super.onPostExecute(list);

            List<String> dialogBusNumber = new ArrayList<>();
            for(int temp = 0 ; temp<list.size() ; temp++){
                dialogBusNumber.add(list.get(temp)[0]);
            }

            new MaterialDialog.Builder(MainActivity.this)
                    .typeface("erh_feng.ttc", "erh_feng.ttc")
                    .title("公車們")
                    .items(dialogBusNumber.toArray(new String[]{}))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            mAddBus = new Bus(text.toString(), list.get(which)[1], null, null, null, "");
                            new getDirect().execute();
                        }
                    })
                    .negativeText("取消")
                    .negativeColorRes(android.R.color.black)
                    .show();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog(true);
            super.onPreExecute();
        }
    }

    private class getDirect extends AsyncTask<Void, Void, Map<String, List<String[]>>> {
        @Override
        protected Map<String, List<String[]>> doInBackground(final Void... params) {
            try {
                Map<String, List<String[]>> map = new HashMap();
                List<String[]> list = new ArrayList();

                Log.i("AmyBus", "http://e-bus.tpc.gov.tw/" + mAddBus.getRid() + "&sec=0");
                Document doc = Jsoup.connect("http://e-bus.tpc.gov.tw/" + mAddBus.getRid() + "&sec=0").get();

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
            showProgressDialog(false);
            if(map==null) return;
            super.onPostExecute(map);


            List<String> dialogDirect = new ArrayList<>();
            for(int temp = 0 ; temp<map.get(BUS_DIRECT_PARAM_AND_TEXT).size() ; temp++){
                dialogDirect.add(map.get(BUS_DIRECT_PARAM_AND_TEXT).get(temp)[1]);
            }

            new MaterialDialog.Builder(MainActivity.this)
                    .typeface("erh_feng.ttc", "erh_feng.ttc")
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
                    .negativeText("取消")
                    .negativeColorRes(android.R.color.black)
                    .show();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog(true);
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

        wb.loadUrl("http://e-bus.tpc.gov.tw/" + bus.getRid() + "&sec=" + bus.getDirectParam());
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
            showProgressDialog(false);
            if(stringArray==null) return;
            super.onPostExecute(stringArray);

            new MaterialDialog.Builder(MainActivity.this)
                    .typeface("erh_feng.ttc", "erh_feng.ttc")
                    .title("在哪一站等車呢?")
                    .items(stringArray)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if(text.toString().contains("(") || text.toString().contains(")")){
                                Toast.makeText(MainActivity.this, "目前不支援有包含括弧的站名", Toast.LENGTH_LONG).show();
                            }else{
                                mAddBus.setOnBus(text.toString());
                                insertBus(mAddBus);
                                dialog.dismiss();
                            }
                        }
                    })
                    .negativeText("取消")
                    .negativeColorRes(android.R.color.black)
                    .autoDismiss(false)
                    .show();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog(true);
            super.onPreExecute();
        }
    }

    String materialDialogHowLong_minute = null;
    @Override
    public void onAlarm(final int position) {
        MaterialDialog materialDialogHowLong = new MaterialDialog.Builder(MainActivity.this)
                .typeface("erh_feng.ttc", "erh_feng.ttc")
                .title("轉鬧鐘囉")
                .content("幾分鐘內到站要提醒妳呢?")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                .input(R.string.dialog_abundant, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        NumberFormat formatter = NumberFormat.getInstance();
                        ParsePosition pos = new ParsePosition(0);
                        formatter.parse(input.toString(), pos);
                        if(input.length() == pos.getIndex()){
                            dialog.setContent("幾分鐘內到站要提醒妳呢?");
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        }else{
                            dialog.setContent("只能輸入數字");
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        materialDialogHowLong_minute = dialog.getInputEditText().getText().toString();

                        Integer[] defaultChoice = new Integer[]{};
                        Alarm tempAlarm = BusDBHelper.getAlarm(mListBus.get(position));
                        if (tempAlarm != null) {
                            if (!"".equals(tempAlarm.getRing()) && tempAlarm.getRing() != null && tempAlarm.getVibrate()) {
                                defaultChoice = new Integer[]{0, 1};
                            } else if (!"".equals(tempAlarm.getRing()) && tempAlarm.getRing() != null) {
                                defaultChoice = new Integer[]{0};
                            } else if (tempAlarm.getVibrate()) {
                                defaultChoice = new Integer[]{1};
                            }
                        }

                        new MaterialDialog.Builder(MainActivity.this)
                                .typeface("erh_feng.ttc", "erh_feng.ttc")
                                .title("怎麼提醒妳呢?")
                                .items(new String[]{"鈴聲", "震動"})
                                .itemsCallbackMultiChoice(defaultChoice, new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                        return true;
                                    }
                                })
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        String string_ring_and_vibrate = "";
                                        for (int temp = 0; temp < dialog.getSelectedIndices().length; temp++) {
                                            string_ring_and_vibrate += dialog.getSelectedIndices()[temp];
                                        }

                                        Bus bus_update = BusDBHelper.getAllStop().get(position);
                                        bus_update.setAlarm(new Alarm(materialDialogHowLong_minute, (string_ring_and_vibrate.contains("0") ? "default" : ""), (string_ring_and_vibrate.contains("1") ? true : false)));
                                        BusDBHelper.requestUpdateAlarm(bus_update);

                                        mRecyclerViewAdapter.notifyDataSetChanged();
                                        mRecyclerView.requestLayout();

                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNeutral(MaterialDialog dialog) {
                                        dialog.clearSelectedIndices();
                                    }
                                })
                                .alwaysCallMultiChoiceCallback()
                                .positiveText("確定")
                                .neutralText("清除")
                                .autoDismiss(false)
                                .positiveColorRes(android.R.color.black)
                                .neutralColorRes(android.R.color.black)
                                .show();
                    }
                })
                .positiveText("確定")
                .negativeText("取消")
                .positiveColorRes(android.R.color.black)
                .negativeColorRes(android.R.color.black)
                .show();
        materialDialogHowLong.getActionButton(DialogAction.POSITIVE).setEnabled(false);
    }

    private void showProgressDialog(boolean show) {
        if(show){
            if(mMaterialDialogProgress == null){
                mMaterialDialogProgress = new MaterialDialog.Builder(MainActivity.this)
                        .title("等我一下")
                        .customView(R.layout.dialog_progress, true)
                        .build();
            }
            mMaterialDialogProgress.show();
        }else{
            mMaterialDialogProgress.dismiss();
        }
    }
}
