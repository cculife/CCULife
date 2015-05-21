package org.zankio.cculife.WifiAutoLogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class WifiAccount {
  private String username, password;
  private Context context;
  private SharedPreferences preferences;
  private Editor pref_editor;
  private static WifiAccount instance;

  public final static String PREF_NAME = "CCULIFE_WIFI_PREF",
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
    this.username = preferences.getString(KEY_USERNAME, null);
    this.password = preferences.getString(KEY_PASSWORD, null);
  }

  public void setLoginInfo(String username, String password) {
    this.username = username;
    this.password = password;
    pref_editor.putString(KEY_USERNAME, username);
    pref_editor.putString(KEY_PASSWORD, password);
    pref_editor.commit();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
