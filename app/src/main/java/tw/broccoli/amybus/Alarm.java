package tw.broccoli.amybus;

import android.util.Log;

/**
 * Created by Broccoli on 2015/9/27.
 */

public class Alarm{
    private String minute = "";
    private String ring = "";
    private boolean vibrate = false;

    public Alarm(String minute, String ring, boolean vibrate){
        if(minute!=null) this.minute = minute;
        if(ring!=null) this.ring = ring;
        this.vibrate = vibrate;
    }

    /**
     * Format
     * "minute=10,ring=path,vibrate=true"
     * "minute=5,ring=,vibrate=false"
     * "minute=,ring=,vibrate=false"
     *
     * @param alarm
     * @return
     */
    public static String getString(Alarm alarm){
        if(alarm != null) {
            Log.i("AmyBus", "getString = minute=" + alarm.getMinute() + ",ring=" + alarm.getRing() + ",vibrate=" + String.valueOf(alarm.getVibrate()));
            return "minute=" + alarm.getMinute() + ",ring=" + alarm.getRing() + ",vibrate=" + String.valueOf(alarm.getVibrate());
        }else{
            return "";
        }
    }
    public static Alarm getAlarm(String alarmString){
        Log.i("AmyBus", "alarmString="+alarmString);

        if(alarmString==null || "".equals(alarmString)) return null;

        String[] alarmSplit = alarmString.split(",");
        Log.i("AmyBus", "getAlarm = minute=" + alarmSplit[0].replace("minute=", "") + ",ring=" + alarmSplit[1].replace("ring=", "") + ",vibrate=" + alarmSplit[2].replace("vibrate=", ""));
        return new Alarm(
                alarmSplit[0].replace("minute=", ""),
                alarmSplit[1].replace("ring=", ""),
                Boolean.valueOf(alarmSplit[2].replace("vibrate=", ""))
        );
    }

//    private static String calcAlarmString(String alarmString, String key){
//        if(alarmString != null && !"".equals(alarmString)) {
//            if(alarmString.contains(key)){
//                String[] alarmSplit = alarmString.split(",");
//
//                return alarmString.substring(alarmString.indexOf(key)+key.length(), (alarmString.indexOf(",", alarmString.indexOf(key)) == -1 ? alarmString.length()-1 : alarmString.indexOf(key)-1));
//            }
//        }
//
//        return null;
//    }


    public String getMinute(){
        return this.minute;
    }

    public String getRing(){
        return this.ring;
    }

    public boolean getVibrate(){
        return this.vibrate;
    }

    public void setMinute(String minute){
        this.minute = minute;
    }

    public void setRing(String ring){
        this.ring = ring;
    }

    public void setVibrate(boolean vibrate){
        this.vibrate = vibrate;
    }
}