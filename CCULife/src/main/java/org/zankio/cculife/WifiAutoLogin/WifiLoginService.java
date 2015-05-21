package org.zankio.cculife.WifiAutoLogin;

import android.app.IntentService;

import android.content.Intent;

import org.zankio.cculife.Debug;

public class WifiLoginService extends IntentService {
  public  WifiLoginService() {
    super("WifiLoginService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Debug debug = new Debug();
    WifiAccount account = WifiAccount.getInstance(this);
    LoginWifi login = new LoginWifi(account.getUsername(), account.getPassword());
    login.login(this);
  }
}
