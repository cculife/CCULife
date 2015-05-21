package org.zankio.cculife.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import org.zankio.cculife.R;
import org.zankio.cculife.WifiAutoLogin.WifiAccount;

public class WifiLoginActivity extends SherlockActivity {

    private String mUsername;
    private String mPassword;
    private boolean mRemeber;

    private EditText mWifiUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_login);
        getSupportActionBar().setTitle("Login");

        mWifiUsernameView = (EditText) findViewById(R.id.wifi_username);

        mPasswordView = (EditText) findViewById(R.id.wifi_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.wifi_login_form);

        findViewById(R.id.wifi_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    public void attemptLogin() {

        mWifiUsernameView.setError(null);
        mPasswordView.setError(null);

        mUsername = mWifiUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mUsername)) {
            mWifiUsernameView.setError(getString(R.string.error_field_required));
            focusView = mWifiUsernameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
          WifiAccount account = WifiAccount.getInstance(getApplicationContext());
          account.setLoginInfo(mUsername, mPassword);
          finish();
        }
    }
}
