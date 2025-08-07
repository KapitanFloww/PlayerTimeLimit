package ptl.ajneb97.utils;

import ptl.ajneb97.managers.MessageManager;

import java.util.Calendar;

public class UtilsTime {

    public static String getTime(long segundos, MessageManager msgManager) {
        long esperatotalmin = segundos / 60;
        long esperatotalhour = esperatotalmin / 60;
        long esperatotalday = esperatotalhour / 24;
        if (segundos > 59) {
            segundos = segundos - 60 * esperatotalmin;
        }
        String time = segundos + msgManager.getTimeSeconds();
        if (esperatotalmin > 59) {
            esperatotalmin = esperatotalmin - 60 * esperatotalhour;
        }
        if (esperatotalmin > 0) {
            time = esperatotalmin + msgManager.getTimeMinutes() + " " + time;
        }
        if (esperatotalhour > 24) {
            esperatotalhour = esperatotalhour - 24 * esperatotalday;
        }
        if (esperatotalhour > 0) {
            time = esperatotalhour + msgManager.getTimeHours() + " " + time;
        }
        if (esperatotalday > 0) {
            time = esperatotalday + msgManager.getTimeDays() + " " + time;
        }

        return time;
    }

    // Returns the milliseconds until the next time restart
    public static long getNextResetMillis(String resetTimeHour) {
        long currentMillis = System.currentTimeMillis();
        String[] sep = resetTimeHour.split(":");
        String hour = sep[0];
        if (hour.startsWith("0")) {
            hour = hour.charAt(1) + "";
        }
        String minute = sep[1];
        if (minute.startsWith("0")) {
            minute = minute.charAt(1) + "";
        }

        final var calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentMillis);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        calendar.set(Calendar.MINUTE, Integer.valueOf(minute));
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() >= currentMillis) {
            //It's not yet time to restart the day.
            return calendar.getTimeInMillis();
        } else {
            //The reset time has already passed today.
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            return calendar.getTimeInMillis();
        }
    }
}
