package org.zankio.cculife.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.cculife.Debug;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseActivity;

import static org.zankio.cculife.utils.PackageUtils.getVersionName;

public class AboutActivity extends BaseActivity {

    private static final int debug_mode_click[] = {R.id.dbg_c, R.id.dbg_c2, R.id.dbg_u, R.id.dbg_u, R.id.dbg_c2, R.id.dbg_c};
    private int debug_mode_index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.dbg_c).setOnClickListener(dbg_click);
        findViewById(R.id.dbg_c2).setOnClickListener(dbg_click);
        findViewById(R.id.dbg_u).setOnClickListener(dbg_click);
        ((TextView)findViewById(R.id.title)).setText("CCULife v" + getVersionName(this));
    }

    public final View.OnClickListener dbg_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (debug_mode_index == debug_mode_click.length || v == null) return;

            if (v.getId() != debug_mode_click[debug_mode_index]) {
                debug_mode_index = 0;
                return;
            }

            debug_mode_index++;

            if (debug_mode_index == debug_mode_click.length) {
                Debug.debug = true;
                Toast.makeText(AboutActivity.this, "Debug Mode : On", Toast.LENGTH_SHORT).show();
            }


        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }
    
}
