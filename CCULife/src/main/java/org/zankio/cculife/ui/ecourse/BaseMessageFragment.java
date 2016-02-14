package org.zankio.cculife.ui.ecourse;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.zankio.cculife.R;

public class BaseMessageFragment extends Fragment {

    public void hideMessage() {
        View view = getView();
        if (view == null) return;

        view.findViewById(R.id.list).setVisibility(View.VISIBLE);
        view.findViewById(R.id.message_panel).setVisibility(View.GONE);
    }

    public void showMessage(String msg) {
        showMessage(msg, false);
    }

    public void showMessage(String msg, boolean loading) {
        View view = getView();
        if (view == null) return;

        //view.findViewById(R.id.icon).setVisibility(loading ? View.GONE : View.VISIBLE);
        ((TextView) view.findViewById(R.id.message)).setText(msg);
        view.findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.message_panel).setVisibility(View.VISIBLE);
        view.findViewById(R.id.list).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        Log.d("F", "onStop");
        super.onStop();
    }
}
