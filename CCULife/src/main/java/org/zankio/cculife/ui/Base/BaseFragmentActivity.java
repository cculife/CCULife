package org.zankio.cculife.ui.Base;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import org.zankio.cculife.CCUService.Portal;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.SettingsActivity;

public class BaseFragmentActivity extends SherlockFragmentActivity {

    protected View MainView;
    protected View errorPanel;
    protected TextView errorMsg;
    protected String ssoID = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_show_in_browser:
                if (ssoID != null) showInBrowser(ssoID);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSSOID (String id) {
        this.ssoID = id;
    }

    public void setErrorView(){
        this.MainView = findViewById(R.id.content);
        this.errorPanel = findViewById(R.id.error_panel);
        errorMsg = (TextView) findViewById(R.id.error_message);
    }

    public void setErrorView(int mainview){
        this.MainView = findViewById(mainview);
        this.errorPanel = findViewById(R.id.error_panel);
        errorMsg = (TextView) findViewById(R.id.error_message);
    }

    public void setErrorView(View MainView, View errorPanel){

        this.MainView = MainView;
        this.errorPanel = errorPanel;
        if (errorPanel != null) errorMsg = (TextView) errorPanel.findViewById(R.id.error_message);
    }

    public void hideErrorMessage() {
        if (MainView != null) MainView.setVisibility(View.VISIBLE);

        if (errorPanel != null) errorPanel.setVisibility(View.GONE);
    }

    public void showErrorMessage(String msg) {
        if (MainView != null && errorPanel != null && errorMsg != null) {
            errorMsg.setText(msg);
            errorPanel.setVisibility(View.VISIBLE);
            MainView.setVisibility(View.GONE);
        }
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

    public void showInBrowser(final String ssoID) {
        new showInBrowserAsyncTask(ssoID).execute();
    }

    public class showInBrowserAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Void> {
        private String ssoID;
        private Toast toast;

        public showInBrowserAsyncTask(String id) {
            this.ssoID = id;
        }

        @Override
        protected void onError(String msg) {
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
            String launchURL;

            portal = new Portal(BaseFragmentActivity.this);
            portal.init();
            launchURL = portal.getSSOPortal(ssoID);

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(launchURL)));

            //Todo remove? Hotfix 140.123.30.107 NoResponse
            if (launchURL.startsWith("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/library/SSO/Query_grade/")) {
                Thread.sleep(500);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php")));
            }
            return null;
        }

        @Override
        protected void _onPostExecute(Void aVoid) {
            toast.cancel();
        }
    }
}
