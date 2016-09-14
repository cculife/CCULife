package org.zankio.cculife.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.zankio.cculife.R;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.ui.ccu.calendar.CCUScheduleActivity;
import org.zankio.cculife.ui.course.schedule.CourseTimeTableActivity;
import org.zankio.cculife.ui.score.ScoreQueryActivity;
import org.zankio.cculife.ui.base.BaseActivity;
import org.zankio.cculife.ui.ecourse.CourseActivity;
import org.zankio.cculife.ui.transport.TransportActivity;

public class HomeActivity extends BaseActivity {

    private static final int ACTIVITY_LOGIN = 1;

    private CCUService loadServices;
    private UserManager sessionManager;
    private CCUService[] ccuServices;

    private void initMenu() {
        ccuServices = new CCUService[]{
                new CCUService(CourseActivity.class, getString(R.string.ecourse), R.drawable.ecourse, true)
                , new CCUService(CourseTimeTableActivity.class, getString(R.string.timetable), R.drawable.timetable, true)
                , new CCUService(ScoreQueryActivity.class, getString(R.string.score_query), R.drawable.score, true)
                , new CCUService(CCUScheduleActivity.class, getString(R.string.schedule), R.drawable.ccuschedule)
                , new CCUService(TransportActivity.class, getString(R.string.transport), R.drawable.trans)
                , new CCUService(SettingsActivity.class, getString(R.string.setting), R.drawable.setting)
                //, new CCUService(null, "飲食", null)
                //, new CCUService(null, "選課", null, true)
                //, new CCUService(null, "工讀生", null, true)
                //, new CCUService(null, "Wifi 自動連線", null)

        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initMenu();

        sessionManager = UserManager.getInstance(this);

        GridView serviceView = (GridView)findViewById(R.id.gridView);
        serviceView.setAdapter(new CCUServiceAdapter(ccuServices));
        serviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadServices = (CCUService)parent.getAdapter().getItem(position);
                if(loadServices.needLogin && !sessionManager.isLogined()) {
                    startActivityForResult(new Intent(HomeActivity.this, LoginActivity.class), ACTIVITY_LOGIN);
                } else if (loadServices.Activity != null) {
                    startActivity(new Intent(HomeActivity.this, loadServices.Activity));
                }
            }
        });
        // new Updater(this).checkUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_LOGIN) {
            if(resultCode == RESULT_OK && loadServices != null && loadServices.Activity != null)
                startActivity(new Intent(HomeActivity.this, loadServices.Activity));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public class CCUService {
        public String Title;
        public int Image;
        public Class<?> Activity;
        public boolean needLogin = false;
        public CCUService (Class<?> activity, String title, int image) {
            this(activity, title, image, false);
        }
        public CCUService (Class<?> activity, String title, int image, boolean needLogin) {
            this.Title = title;
            this.Activity = activity;
            this.Image = image;
            this.needLogin = needLogin;
        }

    }

    public class CCUServiceAdapter extends BaseAdapter {

        private CCUService[] services;

        public CCUServiceAdapter(CCUService[] services) {
            this.services = services;
        }

        @Override
        public int getCount() {
            return services.length;
        }

        @Override
        public Object getItem(int position) {
            return services[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
            CCUService service = services[position];

            if (convertView == null)
                convertView = inflater.inflate(R.layout.item_home, parent, false);

            ((TextView)convertView.findViewById(R.id.title)).setText(service.Title);
            ((ImageView)convertView.findViewById(R.id.icon)).setImageResource(service.Image);
            return convertView;
        }
    }

}
