package org.zankio.cculife.ui.course.schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.zankio.ccudata.kiki.model.TimeTable;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.view.LessonView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import rx.Subscriber;


public class TimeTableWeekFragment extends BaseMessageFragment {
    private LinearLayout[] week;
    private static final float minuteToPixel = 1.5f;
    private static final int[] colors = {0x3333B5E5, 0x33AA66CC, 0x3399CC00, 0x33FFBB33, 0x33FF4444};

    private TimeTable timeTable;
    private Calendar firstClass;
    private IGetTimeTableData courseDataContext;
    Subscriber<TimeTable> subscriber;

    public static int randomColor(){
        return colors[new Random().nextInt(colors.length)];
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            courseDataContext = (IGetTimeTableData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetTimeTableData");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstClass = new GregorianCalendar(0, 0, 0);
        firstClass.set(Calendar.HOUR, 7);
        firstClass.set(Calendar.MINUTE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_timetable_week, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        week = new LinearLayout[]{
                null,
                (LinearLayout) view.findViewById(R.id.weekMonday),
                (LinearLayout) view.findViewById(R.id.weekTuesday),
                (LinearLayout) view.findViewById(R.id.weekWednesday),
                (LinearLayout) view.findViewById(R.id.weekThursday),
                (LinearLayout) view.findViewById(R.id.weekFriday),
                null
        };

        LinearLayout index = (LinearLayout) view.findViewById(R.id.weekIndex);
        TextView indexNode;
        for (int i = 1; i <= 14; i++) {
            indexNode = new TextView(getContext());
            indexNode.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (60 * minuteToPixel)));
            indexNode.setText(String.format(Locale.US, "%d", i));
            indexNode.setGravity(Gravity.CENTER_HORIZONTAL);
            if(i % 2 == 0) indexNode.setBackgroundColor(0x11000000);
            index.addView(indexNode);
        }
        courseDataContext.getTimeTable().subscribe(new Subscriber<TimeTable>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                message().show(e.getMessage());
            }

            @Override
            public void onNext(TimeTable timeTable) {
                TimeTableWeekFragment.this.timeTable = timeTable;
                updateTimeTable();
            }
        });
    }

    public void updateTimeTable() {
        if(timeTable == null) return;

        for (LinearLayout aWeek : week) {
            if (aWeek != null)
                aWeek.removeAllViews();
        }

        for (int i = 0; i < timeTable.days.length; i++) {
            if(week[i] == null) continue;

            TimeTable.Day day = timeTable.days[i];
            for (int j = 0; j < day.classList.size(); j++) {
                TimeTable.Class mClass;
                TimeTable.Class prevClass;
                TimeTable.Class tmpClass;

                LinearLayout.LayoutParams layoutParams;
                LessonView lessonView;

                mClass = day.classList.get(j);
                if (mClass.userAdd == 1) continue;

                prevClass = null;
                for (int k = j - 1; k >= 0; k--) {
                    tmpClass = day.classList.get(k);
                    if (tmpClass.userAdd != 1) {
                        prevClass = tmpClass;
                        break;
                    }
                }

                lessonView = new LessonView(getContext());
                lessonView.setClassName(mClass.name);
                lessonView.setClassRoom(mClass.classroom);
                lessonView.setClassTeacher(mClass.teacher);
                lessonView.setStartTime(mClass.start);
                lessonView.setEndTime(mClass.end);
                lessonView.setBackgroundColor(mClass.color);

                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getTimeDiffInMinute(mClass.start, mClass.end) * minuteToPixel));

                if(prevClass == null)
                    layoutParams.setMargins(0, (int) (getTimeDiffInMinute(firstClass, mClass.start) * minuteToPixel), 0, 0);
                else
                    layoutParams.setMargins(0, (int) (getTimeDiffInMinute(prevClass.end, mClass.start) * minuteToPixel), 0, 0);

                lessonView.setLayoutParams(layoutParams);
                //new GregorianCalendar().clear();
                week[i].addView(lessonView);
            }
        }


    }

    private int getTimeDiffInMinute(Calendar a, Calendar b) {
        return (int)((b.getTimeInMillis() - a.getTimeInMillis()) / 1000 / 60);
    }

}
