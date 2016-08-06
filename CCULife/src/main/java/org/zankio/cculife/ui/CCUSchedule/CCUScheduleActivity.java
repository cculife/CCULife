package org.zankio.cculife.ui.CCUSchedule;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUSchedule;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.base.BaseActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CCUScheduleActivity extends BaseActivity {

    private ScheduleAdapter adapter;
    private ListView listView;
    private int TODAY_DAY_OF_YEAR = -1;
    private int TODAY_YEAR = -1;
    private int last_scroll_item = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccu_schedule);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new ScheduleAdapter();

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        TODAY_DAY_OF_YEAR = today.get(Calendar.DAY_OF_YEAR);
        TODAY_YEAR = today.get(Calendar.YEAR);

        new LoadDataAsyncTask().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("scroll_item", listView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        last_scroll_item = savedInstanceState.getInt("scroll_item", -1);
    }

    public class LoadDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, CCUSchedule.Schedule[]> {

        private CCUSchedule schedule = null;

        @Override
        protected void onError(Exception e, String msg) {
            showMessage(msg);
        }

        @Override
        protected void _onPostExecute(CCUSchedule.Schedule[] result) {
            if (result == null || result.length == 0) {
                showMessage("沒有日程");
                return;
            }


            int i, length = 0;
            for (i = 0; i < result.length; i++) {
                length += result[i].list.length;
            }
            CCUSchedule.Item[] list = new CCUSchedule.Item[length + result.length];

            i = 0;
            for (CCUSchedule.Schedule s : result) {
                CCUSchedule.Item header = schedule.new Item();
                header.Title = s.Name;
                header.Date = s.list[0].Date;
                list[i++] = header;

                for (CCUSchedule.Item item : s.list) {
                    list[i++] = item;
                }
            }

            adapter.setItems(list);
            hideMessage();

            scrollToNow(list);
        }

        @Override
        protected CCUSchedule.Schedule[] _doInBackground(Void... params) throws Exception {
            schedule = new CCUSchedule(CCUScheduleActivity.this);
            return schedule.getScheduleList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ccuschedule, menu);
        return true;
    }

    public void scrollToNow(CCUSchedule.Item[] list) {
        Calendar now = Calendar.getInstance();
        now = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        int i;
        for (i = list.length - 1; i >= 0; i--) {
            if (now.after(list[i].Date)) break;
        }
        if (last_scroll_item == -1)
            last_scroll_item = i >= 0 ? i + 1 : 0;
        listView.setSelection(last_scroll_item);
    }


    public class ScheduleAdapter extends BaseAdapter {
        private CCUSchedule.Item[] items;

        public void setItems(CCUSchedule.Item[] items) {
            this.items = items;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.length;
        }

        @Override
        public Object getItem(int position) {
            return items == null ? null : items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(CCUScheduleActivity.this);

            View view;
            if (convertView == null) view = inflater.inflate(R.layout.item_ccu_schedule, parent, false);
            else view = convertView;

            TextView dateView = (TextView) view.findViewById(R.id.Date),
                    titleView = (TextView) view.findViewById(R.id.Title);

            int background = 0;
            CCUSchedule.Item item = (CCUSchedule.Item) getItem(position);
            String date = item.toDateString();


            // is today
            if (item.isToday(TODAY_YEAR, TODAY_DAY_OF_YEAR))
                background = ContextCompat.getColor(inflater.getContext(), R.color.Today);

            // date is same as prev item
            if (position != 0 && item.Date.compareTo(items[position - 1].Date) == 0)
                date = "";

            view.setBackgroundColor(background);
            dateView.setText(date);
            titleView.setText(item.Title);

            return view;
        }
    }
}
