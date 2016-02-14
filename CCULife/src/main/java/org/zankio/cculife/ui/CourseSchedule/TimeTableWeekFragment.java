package org.zankio.cculife.ui.CourseSchedule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.kiki.model.TimeTable;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.ecourse.BaseMessageFragment;
import org.zankio.cculife.view.LessionView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;


public class TimeTableWeekFragment extends BaseMessageFragment
        implements IOnUpdateListener<TimeTable> {
    private LinearLayout[] week;
    private float minuteToPixel = 1.5f;
    private static int[] colors = {0x3333B5E5, 0x33AA66CC, 0x3399CC00, 0x33FFBB33, 0x33FF4444};

    private TimeTable timeTable;
    private Calendar firstClass;
    private IGetTimeTableData courseDataContext;
    private boolean loading;

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
                (LinearLayout) view.findViewById(R.id.weekTueday),
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
            indexNode.setText(String.format("%d", i));
            indexNode.setGravity(Gravity.CENTER_HORIZONTAL);
            if(i % 2 == 0) indexNode.setBackgroundColor(0x11000000);
            index.addView(indexNode);
        }
        this.loading = courseDataContext.getTimeTable(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onComplete(String type) { }

    @Override
    public void onNext(String type, TimeTable timeTable, BaseSource source) {
        this.loading = false;
        this.timeTable = timeTable;
        updateTimeTable();
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        this.loading = false;
        showMessage(err.getMessage());
    }

    public void updateTimeTable() {
        if(timeTable == null) return;

        for (int i = 0; i < week.length; i++) {
            if(week[i] != null)
                week[i].removeAllViews();
        }

        for (int i = 0; i < timeTable.days.length; i++) {
            if(week[i] == null) continue;

            TimeTable.Day day = timeTable.days[i];
            for (int j = 0; j < day.classList.size(); j++) {
                TimeTable.Class mClass;
                LinearLayout.LayoutParams layoutParams;
                LessionView lessionView;

                mClass = day.classList.get(j);

                lessionView = new LessionView(getContext());
                lessionView.setClassName(mClass.name);
                lessionView.setClassRoom(mClass.classroom);
                lessionView.setClassTeacher(mClass.teacher);
                lessionView.setStartTime(mClass.start);
                lessionView.setEndTime(mClass.end);
                lessionView.setBackgroundColor(mClass.color);

                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getTimeDiffInMinute(mClass.start, mClass.end) * minuteToPixel));

                if(j == 0)
                    layoutParams.setMargins(0, (int) (getTimeDiffInMinute(firstClass, mClass.start) * minuteToPixel), 0, 0);
                else
                    layoutParams.setMargins(0, (int) (getTimeDiffInMinute(day.classList.get(j - 1).end, mClass.start) * minuteToPixel), 0, 0);

                lessionView.setLayoutParams(layoutParams);
                //new GregorianCalendar().clear();
                week[i].addView(lessionView);
            }
        }


    }

    private int getTimeDiffInMinute(Calendar a, Calendar b) {
        return (int)((b.getTimeInMillis() - a.getTimeInMillis()) / 1000 / 60);
    }

}
