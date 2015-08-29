package org.zankio.cculife.ui;

import android.annotation.TargetApi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;

import android.content.DialogInterface;
import android.content.res.Resources;

import android.os.Bundle;

import org.zankio.cculife.R;

@TargetApi(11)
public class AutoLoginAlert extends DialogFragment {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Resources resources = getResources();
    return new AlertDialog.Builder(getActivity())
      .setTitle(resources.getString(R.string.autologin_alert_title))
      .setMessage(resources.getString(R.string.autologin_alert_message))
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          // Do nothing here
        }
      })
      .create();
  }
}
