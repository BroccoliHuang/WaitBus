package tw.broccoli.amybus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by broccoli on 15/9/24.
 */
public class DB extends SQLiteOpenHelper {
    private final static int DATABASE_VERSION = 1;

    /**stop資料表*/
    public final static String BUS_TABLE = "bus";
    public final static String BUS_ID = "_id";
    public final static String BUS_NUMBER = "bus_number";
    public final static String BUS_RID = "bus_rid";
    public final static String BUS_DIRECT_PARAM = "bus_direct_param";
    public final static String BUS_DIRECT_TEXT = "bus_direct_text";
    public final static String BUS_ONBUS = "bus_onbus";

    /**setting資料表*/
    public final static String SETTING_TABLE = "setting";
    public final static String SETTING_ID = "_id";


    public DB(Context context){
        super(context,context.getResources().getString(R.string.db_databases_name), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + BUS_TABLE + " (" +
                BUS_ID + " INTEGER primary key autoincrement, " +
                BUS_NUMBER + " TEXT," +
                BUS_RID + " TEXT," +
                BUS_DIRECT_PARAM + " TEXT," +
                BUS_DIRECT_TEXT + " TEXT," +
                BUS_ONBUS + " TEXT)");
        db.execSQL( "CREATE TABLE " + SETTING_TABLE + " (" +
                SETTING_ID + " INTEGER primary key autoincrement)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 使用SQL語法取得Cursor
     * 註:Widget只顯示widget設為1的row
     * @param sql 欲下達的SQL語法
     * @return widget欄位設為1的row群
     */
    Cursor getCursor(String sql){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(sql, null);
    }

    Result addBus(Bus bus) {
        Result result = new Result();
        if(!bus.isComplete()){
            if(result!=null){
                result.status = Result.FAILED;
                result.message = "要寫完整唷";
                return result;
            }
        }

        Cursor cursor = getCursor("SELECT * FROM "+BUS_TABLE+
                " WHERE ("+BUS_NUMBER+" = \""+bus.getNumber()+"\""+
                " AND "+BUS_RID+" = \""+bus.getRid()+"\""+
                " AND "+BUS_DIRECT_PARAM+" = \""+bus.getDirectParam()+"\""+
                " AND "+BUS_DIRECT_TEXT+" = \""+bus.getDirectText()+"\""+
                " AND "+BUS_ONBUS+" = \""+bus.getOnBus()+"\")");
        if(cursor.getCount()>0){
            cursor.close();

            result.status = Result.FAILED;
            result.message = "已加入過相同資料";

            return result;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        /*將新增的值放入ContentValues*/
        ContentValues cv = new ContentValues();
        cv.put(BUS_NUMBER, bus.getNumber());
        cv.put(BUS_RID, bus.getRid());
        cv.put(BUS_DIRECT_PARAM, bus.getDirectParam());
        cv.put(BUS_DIRECT_TEXT, bus.getDirectText());
        cv.put(BUS_ONBUS, bus.getOnBus());

        long rowId = db.insert(BUS_TABLE, null, cv);
        if(rowId == -1){
            result.status = Result.ERROR;
            result.message = "加入公車失敗";
            return result;
        }

        db.close();
        close();

        result.status = Result.SUCCESS;
        result.message = "加入公車成功";

        return result;
    }

    void deleteBus(Bus bus) {
        if(!bus.isComplete()) return;
//        getCursor("DELETE FROM "+BUS_TABLE+
//                " WHERE ("+BUS_NUMBER+" = \""+bus.getNumber()+"\""+
//                " AND "+BUS_RID+" = \""+bus.getRid()+"\""+
//                " AND "+BUS_DIRECT_PARAM+" = \""+bus.getDirectParam()+"\""+
//                " AND "+BUS_DIRECT_TEXT+" = \""+bus.getDirectText()+"\""+
//                " AND "+BUS_ONBUS+" = \""+bus.getOnBus()+"\")");

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(BUS_TABLE,
                BUS_NUMBER + "=? AND " + BUS_RID + "=? AND " + BUS_DIRECT_PARAM + "=? AND " + BUS_DIRECT_TEXT + "=? AND " + BUS_ONBUS + "=?",
                new String[]{bus.getNumber(), bus.getRid(), bus.getDirectParam(), bus.getDirectText(), bus.getOnBus()});
        db.close();
    }

    Cursor getAllStop(){
        return getCursor("SELECT * FROM " + BUS_TABLE);
    }
}
