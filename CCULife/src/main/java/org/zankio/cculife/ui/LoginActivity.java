package org.zankio.cculife.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import org.zankio.ccudata.base.exception.LoginErrorException;
import org.zankio.ccudata.base.model.AuthData;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.source.local.DatabaseBaseSource;
import org.zankio.ccudata.ecourse.source.remote.Authenticate;
import org.zankio.ccudata.portal.Portal;
import org.zankio.cculife.R;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.ui.base.BaseActivity;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Observable;
import rx.subjects.Subject;

public class LoginActivity extends BaseActivity {

    private String mStudentId;
    private String mPassword;
    private boolean mRemember;

    private EditText mStudentIdView;
    private EditText mPasswordView;
    private CheckBox mRememberView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        mStudentIdView = (EditText) findViewById(R.id.studentid);

        mRememberView = (CheckBox) findViewById(R.id.remeber);

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
/*        if (mAuthTask != null) {
            return;
        }*/

        mStudentIdView.setError(null);
        mPasswordView.setError(null);

        mStudentId = mStudentIdView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mRemember = mRememberView.isChecked();

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

            Subject
                    .create((Observable.OnSubscribe<Response<Boolean, AuthData>>) subscriber -> {
                        Portal portal = new Portal();
                        Ecourse ecourse = new Ecourse(this);

                        portal
                                .fetch(org.zankio.ccudata.portal.source.Authenticate.request(mStudentId, mPassword))
                                .subscribe(
                                        subscriber::onNext,
                                        throwable ->
                                                ecourse
                                                    .fetch(Authenticate.request(mStudentId, mPassword))
                                                    .subscribe(
                                                            subscriber::onNext,
                                                            subscriber::onError,
                                                            subscriber::onCompleted
                                                    )
                                );
                    })
                    .doOnTerminate(() -> showProgress(false))
                    .subscribe(
                            response -> {
                                Boolean success = response.data();
                                if (success != null && success) {
                                    DatabaseBaseSource.clearData(LoginActivity.this);
                                    org.zankio.ccudata.kiki.source.local.DatabaseBaseSource.clearData(LoginActivity.this);
                                    UserManager.getInstance(LoginActivity.this)
                                            .createLoginSession(mStudentId, mPassword, mRemember);

                                    setResult(RESULT_OK);

                                    finish();
                                }
                            }, e -> {
                                Throwable throwable = ExceptionUtils.extraceException(e);
                                mPasswordView.setError(throwable.getMessage());
                                mPasswordView.requestFocus();
                            }
                    );
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

}
