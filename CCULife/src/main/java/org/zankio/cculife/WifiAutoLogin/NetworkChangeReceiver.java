package org.zankio.cculife.WifiAutoLogin;

import android.annotation.TargetApi;

import android.app.Notification;
import android.app.NotificationManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.wifi.WifiManager;
import android.net.NetworkInfo;

import android.preference.PreferenceManager;

import org.zankio.cculife.R;
import org.zankio.cculife.Debug;
import org.zankio.cculife.WifiAutoLogin.WifiLoginService;

public class NetworkChangeReceiver extends BroadcastReceiver {
  @Override
  @TargetApi(11)
  public void onReceive(final Context context, final Intent intent) {
    String action = intent.getAction();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Debug debug = new Debug();
    if(!prefs.getBoolean("autologin_enable", false)) {
      return;
    }
    WifiAccount account = WifiAccount.getInstance(context);
    if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
      WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
      NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
      NetworkInfo.State state = networkInfo.getState();

      if(state == NetworkInfo.State.CONNECTED) { // Network is connect
        String connectingSSID = manager.getConnectionInfo().getSSID().replace("\"", "");
        if(connectingSSID.equals("CCU")) {
          debug.info("Start service");
          context.startService(new Intent(context, WifiLoginService.class));
        }
      }
    }
  }
}
