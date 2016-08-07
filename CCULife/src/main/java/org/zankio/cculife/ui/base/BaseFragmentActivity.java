package org.zankio.cculife.ui.base;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.zankio.cculife.CCUService.portal.Portal;
import org.zankio.cculife.CCUService.portal.service.BasePortal;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.SettingsActivity;
import org.zankio.cculife.ui.base.helper.Message;

public class BaseFragmentActivity extends AppCompatActivity {
    private final Message mMessage = new Message(
            this,
            R.id.message,
            R.id.loading,
            R.id.message_panel,
            R.id.list
    );

    protected BasePortal ssoService = null;
    private boolean toolbarInited;
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
                if (ssoService != null) showInBrowser(ssoService);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    public android.support.v7.app.ActionBar getSupportActionBar() {
        if (!toolbarInited) initToolbar();
        ActionBar actionBar = super.getSupportActionBar();

        if (actionBar == null) throw new RuntimeException("initial Toolbar fail");
        return actionBar;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarInited = true;
    }

    public void setSSOService (BasePortal Service) {
        this.ssoService = Service;
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

    public void showInBrowser(final BasePortal ssoService) {
        new showInBrowserAsyncTask(ssoService).execute();
    }

    public class showInBrowserAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Void> {

        private BasePortal ssoService;
        private Toast toast;
        public showInBrowserAsyncTask(BasePortal ssoService) {
            this.ssoService = ssoService;
        }

        @Override
        protected void onError(Exception e, String msg) {
            toast = makeToast("載入失敗", toast);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            toast = makeToast("載入中...", toast);
        }

        @Override
        protected Void _doInBackground(Void... params) throws Exception {
            Portal portal;
            String launchURL[];

            portal = new Portal(BaseFragmentActivity.this);
            //portal.init();
            launchURL = portal.getSSOPortal(ssoService);

            if (launchURL == null) throw new Exception();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(launchURL[0])));

            for (int i = 1; i < launchURL.length; i++) {
                Thread.sleep(1000);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(launchURL[i])));
            }

            return null;
        }

        @Override
        protected void _onPostExecute(Void aVoid) {
            toast.cancel();
        }

    }

    public Message message() { return mMessage; }
}
