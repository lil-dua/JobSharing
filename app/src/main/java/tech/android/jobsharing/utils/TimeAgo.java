package tech.android.jobsharing.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import tech.android.jobsharing.R;

public class TimeAgo {
    private Context context;

    public TimeAgo(Context context) {
        this.context = context;
    }

    public String covertTimeToText(String dataDate) {

        String convTime = null;

        String prefix = "";
        String suffix = context.getString(R.string.ago);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);
            Date nowTime = new Date();
            long dateDiff = nowTime.getTime() - pasTime.getTime();
            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);
            if (second < 60) {
                convTime = second + " "  + context.getString(R.string.seconds) + " " + suffix;
            } else if (minute < 60) {
                convTime = minute + " "  + context.getString(R.string.minutes) + " " +suffix;
            } else if (hour < 24) {
                convTime = hour + " "  + context.getString(R.string.hours) + " " + suffix;
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " "  + context.getString(R.string.years) + " " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " "  + context.getString(R.string.months) + " " + suffix;
                } else {
                    convTime = (day / 7) + " "  + context.getString(R.string.week) + " " + suffix;
                }
            } else if (day < 7) {
                convTime = day + " " + context.getString(R.string.days) + " "+suffix;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convTime;
    }

}