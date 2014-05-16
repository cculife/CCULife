package org.zankio.cculife;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CCUSchedule {
    //

    public Context context;

    public CCUSchedule(Context context) {
        this.context = context;
    }

    public String[] SCHEDULE_TITLE = {"102學年度", "103學年度"};
    public String[] SCHEDULE_FILE = {"102schedule", "103schedule"};

    private String getScheduleRawDate(String fileName) {
        InputStream is;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public Schedule[] getScheduleList(){
        Pattern pattern;
        Matcher matcher;
        Calendar date = null;
        List<Item> list;
        Schedule[] result = new Schedule[SCHEDULE_TITLE.length];

        pattern = Pattern.compile("^(\\d{0,4})[ \t]+(\\d{0,2})[ \t]+(\\d{0,2})[ \t]+([一二三四五六日]?)[ \t]*([^\n" +
                "]+)", Pattern.MULTILINE);

        for (int i = 0; i < SCHEDULE_TITLE.length; i++) {
            result[i] = new Schedule();
            result[i].Name = SCHEDULE_TITLE[i];

            matcher = pattern.matcher(getScheduleRawDate(SCHEDULE_FILE[i]));

            list = new ArrayList<Item>();

            while (matcher.find()) {
                Item item = new Item();
                if (!"".equals(matcher.group(1))) {
                    int year = Integer.parseInt(matcher.group(1));
                    int month = Integer.parseInt(matcher.group(2));
                    int day = Integer.parseInt(matcher.group(3));
                    date = new GregorianCalendar(year, month - 1, day);
                }
                String title = matcher.group(5);

                item.Title = title;
                item.Date = date;
                list.add(item);
            }

            result[i].list = list.toArray(new Item[list.size()]);
        }


        return result;
    }

    public class Schedule {
        public String Name;
        public Item[] list;
    }

    public class Item {
        public Calendar Date;
        public String Title;
    }
}
