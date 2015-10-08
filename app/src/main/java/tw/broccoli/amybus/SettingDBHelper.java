package tw.broccoli.amybus;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by broccoli on 15/9/24.
 */
public class SettingDBHelper {
    public static int getAndUpdateTitleBackground(){
        DB db = new DB(Common.getAppContext());
        Cursor cursor = db.getTitleBackground();
        int titleBackground = 0;

        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            titleBackground = Integer.valueOf(cursor.getString(cursor.getColumnIndex(DB.SETTING_TITLE_BACKGROUND)));
        }

        db.updateTitleBackground(titleBackground);

        cursor.close();
        db.close();

        return titleBackground;
    }
}
