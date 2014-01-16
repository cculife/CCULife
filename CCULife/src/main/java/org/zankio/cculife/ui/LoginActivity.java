package org.zankio.cculife.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.R;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Net;

import java.io.IOException;

public class LoginActivity extends SherlockActivity {

    private UserLoginTask mAuthTask = null;

    private String mStudentId;
    private String mPassword;
    private boolean mRemeber;

    private EditText mStudentIdView;
    private EditText mPasswordView;
    private CheckBox mRemeberView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        mStudentIdView = (EditText) findViewById(R.id.studentid);

        mRemeberView = (CheckBox) findViewById(R.id.remeber);

        mPasswordView = (EditText) findViewById(R.id.password);
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

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mStudentIdView.setError(null);
        mPasswordView.setError(null);

        mStudentId = mStudentIdView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mRemeber = mRemeberView.isChecked();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mStudentId)) {
            mStudentIdView.setError(getString(R.string.error_field_required));
            focusView = mStudentIdView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        public String message = null;
        @Override
        protected Boolean doInBackground(Void... params) {
            String cookie;

            try {

                Connection cnt = Jsoup.connect("http://portal.ccu.edu.tw/").timeout(Net.CONNECT_TIMEOUT);
                cnt.get();
                cookie = cnt.response().cookies().get("ccuSSO");

                if (cookie == null) { message = "程式錯誤!!"; return false; }
                cnt.cookie("ccuSSO", cookie)
                   .url("http://portal.ccu.edu.tw/login_check.php")
                   .data("acc", mStudentId)
                   .data("pass", mPassword);
                   //.data("authcode", "請輸入右邊文字");
                cnt.followRedirects(false);
                cnt.post();

                String location = cnt.response().header("Location");
                if (location != null && location.equals("http://portal.ccu.edu.tw/sso_index.php")) {
                    return true;
                }
            } catch (IOException e) {
                message = "網路問題";
                return false;
            }
            message = getString(R.string.error_incorrect_password);
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SessionManager.getInstance(LoginActivity.this)
                        .createLoginSession(mStudentId, mPassword, mRemeber);
                setResult(RESULT_OK);
                finish();
            } else {
                mPasswordView.setError(message);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
