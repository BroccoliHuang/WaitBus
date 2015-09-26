package tw.broccoli.amybus;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by broccoli on 15/9/24.
 */
public class BusDBHelper {
    public static Result requestAdd(Context cnx, Bus bus){
        DB db = new DB(cnx);
        Result result = db.addBus(bus);
        db.close();
        return result;
    }

    public static void requestDelete(Context cnx, Bus bus){
        DB db = new DB(cnx);
        db.deleteBus(bus);
        db.close();
    }

    public static List<Bus> getAllStop(Context cnx){
        DB db = new DB(cnx);
        Cursor cursor = db.getAllStop();
        List<Bus> list = new ArrayList();

        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                list.add(new Bus(cursor.getString(cursor.getColumnIndex(DB.BUS_NUMBER)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_RID)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_DIRECT_PARAM)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_DIRECT_TEXT)),
                                cursor.getString(cursor.getColumnIndex(DB.BUS_ONBUS))));
                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();

        return list;
    }
}
