package org.zankio.cculife.WifiAutoLogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class WifiAccount {
  private boolean is_login;
  private String username, password;
  private Context context;
  private SharedPreferences preferences;
  private Editor pref_editor;
  private static WifiAccount instance;
  private WifiAccountStateChangeListener listener;

  public final static String PREF_NAME = "CCULIFE_WIFI_PREF",
                             KEY_IS_LOGIN = "wifi_is_login",
                             KEY_USERNAME = "wifi_username",
                             KEY_PASSWORD = "wifi_password";

  public static WifiAccount getInstance() {
    return instance;
  }

  public static WifiAccount getInstance(Context context) {
    if(instance == null) {
      instance = new WifiAccount(context);
    }
    return instance;
  }

  private WifiAccount(Context context) {
    this.context = context;
    preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    pref_editor = preferences.edit();
    this.is_login = preferences.getBoolean(KEY_IS_LOGIN, false);
    this.username = preferences.getString(KEY_USERNAME, null);
    this.password = preferences.getString(KEY_PASSWORD, null);
  }

  public boolean isLogin() {
    return is_login;
  }

  public void setLoginInfo(String username, String password) {
    this.is_login = true;
    this.username = username;
    this.password = password;
    pref_editor.putBoolean(KEY_IS_LOGIN, true);
    pref_editor.putString(KEY_USERNAME, username);
    pref_editor.putString(KEY_PASSWORD, password);
    pref_editor.commit();
    publishState();
  }

  public void clearLogin() {
    this.is_login = false;
    pref_editor.putBoolean(KEY_IS_LOGIN, false);
    pref_editor.putString(KEY_USERNAME, null);
    pref_editor.putString(KEY_USERNAME, null);
    publishState();
  }

  public String getUsername() {
    return is_login ? username : null;
  }

  public String getPassword() {
    return is_login ? password : null;
  }

  public void registerListener(WifiAccountStateChangeListener listener) {
    this.listener = listener;
  }

  public void unregisterListener() {
    this.listener = null;
  }

  private void publishState() {
    if(listener != null) {
      listener.onWifiAccountStateChange(is_login);
    }
  }

  public interface WifiAccountStateChangeListener {
    public void onWifiAccountStateChange(boolean isLogin);
  }
}
