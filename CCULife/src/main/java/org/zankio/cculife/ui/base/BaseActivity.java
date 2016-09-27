package org.zankio.cculife.ui.base;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.zankio.ccudata.portal.Portal;
import org.zankio.ccudata.portal.model.PortalData;
import org.zankio.cculife.R;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.ccu.service.portal.PortalService;
import org.zankio.cculife.ui.SettingsActivity;
import org.zankio.cculife.ui.base.helper.Message;

public abstract class BaseActivity extends AppCompatActivity {
    private final Message mMessage = new Message(
            this,
            R.id.message,
            R.id.loading,
            R.id.message_panel,
            R.id.list
    );

    private boolean toolbarInited = false;
    protected PortalData portalData = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_show_in_browser:
                Portal portal = new Portal();

                UserManager userManager = UserManager.getInstance();
                portal.user()
                        .username(userManager.getUsername())
                        .password(userManager.getPassword());

                if (portalData != null)
                    PortalService.openPortal(this, portal, portalData);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public android.support.v7.app.ActionBar getSupportActionBar() {
        if (!toolbarInited) initToolbar();
        ActionBar actionBar = super.getSupportActionBar();

        if (actionBar == null) throw new RuntimeException("initial Toolbar fail");
        return actionBar;
    }

    protected void initToolbar() {
        if (toolbarInited) return;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarInited = true;
    }

    public void setSSOService (PortalData Service) {
        this.portalData = Service;
    }

    public Toast makeToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
        return toast;
    }

    public Toast makeToast(String msg, Toast toast) {
        if (toast == null) return makeToast(msg);

        toast.setText(msg);
        toast.show();

        return toast;
    }

    public Message message() { return mMessage; }
}
