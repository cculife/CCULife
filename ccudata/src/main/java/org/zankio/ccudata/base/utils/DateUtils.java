package org.zankio.ccudata.base.utils;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtils {
    public static Calendar parse(String date) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(date);
        for (DateGroup group : groups) {

            // date not the whold string
            if (!group.getText().equals(date))
                return null;

            List<Date> dates = group.getDates();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dates.get(0));
            return calendar;
        }
        return null;
    }

    public static String normalizeDateString(String format, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        Calendar calendar = parse(date);
        if (calendar == null)
            return date;
        else
            return dateFormat.format(calendar.getTime());

    }
}
