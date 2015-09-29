package tw.broccoli.amybus;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by broccoli on 15/9/24.
 */
public class BusDBHelper {
    public static Result requestAdd(Bus bus){
        DB db = new DB(Common.getAppContext());
        Result result = db.addBus(bus);
        db.close();
        return result;
    }

    public static void requestUpdateAlarm(Bus bus){
        DB db = new DB(Common.getAppContext());
        db.updateBusAlarm(bus);
        db.close();
    }

    public static void requestDelete(Bus bus){
        DB db = new DB(Common.getAppContext());
        db.deleteBus(bus);
        db.close();
    }

    public static List<Bus> getAllStop(){
        DB db = new DB(Common.getAppContext());
        Cursor cursor = db.getAllStop();
        List<Bus> list = new ArrayList();

        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                list.add(new Bus(cursor.getString(cursor.getColumnIndex(DB.BUS_NUMBER)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_RID)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_DIRECT_PARAM)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_DIRECT_TEXT)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_ONBUS)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_ALARM))));
                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();

        return list;
    }

    public static Alarm getAlarm(Bus bus){
        DB db = new DB(Common.getAppContext());
        Cursor cursor = db.getAlarm(bus);
        String alarmString = null;

        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            alarmString = cursor.getString(cursor.getColumnIndex(DB.BUS_ALARM));
        }

        cursor.close();
        db.close();

        if(alarmString != null){
            return Alarm.getAlarm(alarmString);
        }
        return null;
    }
}
