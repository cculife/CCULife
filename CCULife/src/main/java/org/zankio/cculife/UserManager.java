package org.zankio.cculife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.zankio.cculife.ui.LoginActivity;

import java.util.HashMap;

public class UserManager {


    private final static String KEY_ISLOGIN = "IsLogined";
    private final static String PREF_NAME = "CCULIFE_SECU";
    public final static String KEY_USERNAME = "UserName";
    public final static String KEY_PASSWORD = "Password";
    private static final String KEY_SAVE = "Save";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private String UserName = null;
    private String Password = null;
    private boolean isLogined = false;
    private boolean save = false;

    private onLoginStateChangedListener onLoginStateChangedListener;

    private static UserManager Instance = null;

    public static UserManager getInstance() {
        if(Instance == null) throw new IllegalArgumentException("UserManager is Uninitialized");
        return Instance;
    }

    public static UserManager getInstance(Context context){
        if(Instance == null) Instance = new UserManager(context.getApplicationContext());
        return Instance;
    }

    /**
     * 管理帳號密碼資料
     * @param context Context
     */
    public UserManager(Context context){
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.save = preferences.getBoolean(KEY_SAVE, false);
        this.editor = preferences.edit();
    }


    /**
     * 儲存使用者的帳號密碼
     * @param username 帳號
     * @param password 密碼
     * @param save 是否儲存或是僅在這一次中使用
     */
    public void createLoginSession(String username, String password, boolean save) {

        if (this.save == save) {
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_ISLOGIN, true);
            editor.putBoolean(KEY_SAVE, true);
            editor.commit();
        } else {
            UserName = username;
            Password = password;
            isLogined = true;
        }

        if(onLoginStateChangedListener != null) onLoginStateChangedListener.onLoginStateChanged(isLogined());
    }

    public String getUserName() {
        return save ? preferences.getString(KEY_USERNAME, null) : UserName;
    }

    public String getPassword() {
        return save ? preferences.getString(KEY_PASSWORD, null) : Password;
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> result = new HashMap<>();

        result.put(KEY_USERNAME, save ? preferences.getString(KEY_USERNAME, null) : UserName);
        result.put(KEY_PASSWORD, save ? preferences.getString(KEY_PASSWORD, null) : Password);

        return result;
    }

    /**
     * 清除儲存的帳號密碼資料
     */
    private void clearSession(){
        UserName = Password = null;
        isLogined = false;
        save = false;

        editor.clear();
        editor.commit();

        if(onLoginStateChangedListener != null) onLoginStateChangedListener.onLoginStateChanged(isLogined());
    }

    /**
     * 開啟LoginActivity
     */
    public void Login(){
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void toggleLogin() {
        if (isLogined()) clearSession(); else Login();
    }

    /**
     * 確認是否已儲存帳號密碼資料
     * @return 是否已儲存
     */
    public boolean isLogined(){
        return save || isLogined;
    }

    public boolean isSave() {
        return save;
    }

    public UserManager.onLoginStateChangedListener getOnLoginStateChangedListener() {
        return onLoginStateChangedListener;
    }

    /**
     * 設定當登入資訊改變時的Callback
     * @param onLoginStateChangedListener UserManager.onLoginStateChangedListener
     */
    public void setOnLoginStateChangedListener(UserManager.onLoginStateChangedListener onLoginStateChangedListener) {
        this.onLoginStateChangedListener = onLoginStateChangedListener;
    }


    public interface onLoginStateChangedListener {
        void onLoginStateChanged(boolean isLogined);
    }
}
