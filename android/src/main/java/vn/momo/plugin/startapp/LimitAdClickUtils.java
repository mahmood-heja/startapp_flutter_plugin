package vn.momo.plugin.startapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

//used for limit user click on ads
//hide ad when user click on ads twice in 24hr
public class LimitAdClickUtils {
    public static final String IS_BLOCKED_KEY = "is_blocked", LAST_TIME_CLICKED_KEY = "last_time_clicked";


    static private int lastClickIn24h(Context context) {
        long lastClickTime = getSharedPreferences(context).getLong(LAST_TIME_CLICKED_KEY, -1);

        if (lastClickTime < 0)
            return 0;

        long currentTimeMillis = getCurrentTimeMillis();

        //check if last click is after 24hr
        //so re count from zero
        if (currentTimeMillis - lastClickTime >= 86400000) {
            getSharedPreferences(context).edit().putBoolean(IS_BLOCKED_KEY, false).apply();
            return 0;
        }

        return 1;
    }

    static public boolean onAdClick(Context context) {
        int numberOfClicks = lastClickIn24h(context);
        numberOfClicks++;

        getSharedPreferences(context).edit().putLong(LAST_TIME_CLICKED_KEY, getCurrentTimeMillis()).apply();
        if (numberOfClicks == 2) {
            //stop view banner when user clicks on ads twice in 24hr
            getSharedPreferences(context).edit().putBoolean(IS_BLOCKED_KEY, true).apply();
            return false;
        }

        return true;
    }

    static public boolean userIsBlocked(Context context){
        return  getSharedPreferences(context).getBoolean(IS_BLOCKED_KEY, false);
    }

    static private long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    static private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("banner", Context.MODE_PRIVATE);
    }

}
