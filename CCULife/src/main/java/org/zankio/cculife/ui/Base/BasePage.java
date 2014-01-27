package org.zankio.cculife.ui.Base;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.zankio.cculife.R;

public abstract class BasePage {
    protected LayoutInflater inflater;
    protected View PageView;
    protected View MainView;
    protected View messagePanel;
    protected TextView messageView;
    protected ProgressBar messageLoaging;
    protected ImageView messageIcon;

    public BasePage(LayoutInflater inflater){
        this.inflater = inflater;
    }

    protected abstract View createView();
    public abstract void initViews();

    public View getMainView() {
        return (PageView != null) ? PageView.findViewById(R.id.content) : null;
    }

    public View getView() {
        if(PageView == null) {
            PageView = createView();
            MainView = getMainView();


            if(PageView != null) {
                messagePanel = PageView.findViewById(R.id.message_panel);
                messageView = (TextView) PageView.findViewById(R.id.message);
                messageLoaging = (ProgressBar) PageView.findViewById(R.id.loading);
                messageIcon = (ImageView) PageView.findViewById(R.id.icon);
            }

            initViews();
        }
        return PageView;
    }

    public void hideMessage() {
        if (MainView != null) MainView.setVisibility(View.VISIBLE);

        if (messagePanel != null) messagePanel.setVisibility(View.GONE);
    }

    public void showMessage(String msg) {
        if (MainView != null && messagePanel != null && messageView != null) {
            messageLoaging.setVisibility(View.GONE);
            messageIcon.setVisibility(View.GONE);
            messageView.setText(msg);
            messagePanel.setVisibility(View.VISIBLE);
            MainView.setVisibility(View.GONE);
        }
    }

    public void showMessage(String msg, boolean loading) {
        if (MainView != null && messagePanel != null && messageView != null) {
            messageLoaging.setVisibility(loading ? View.VISIBLE : View.GONE);
            messageIcon.setVisibility(loading ? View.GONE : View.VISIBLE);
            messageView.setText(msg);
            messagePanel.setVisibility(View.VISIBLE);
            MainView.setVisibility(View.GONE);
        }
    }

    public void showMessage(String msg, int resId) {
        if (MainView != null && messagePanel != null && messageView != null) {
            messageLoaging.setVisibility(View.GONE);
            messageIcon.setVisibility(View.VISIBLE);
            messageIcon.setImageResource(resId);
            messageView.setText(msg);
            messagePanel.setVisibility(View.VISIBLE);
            MainView.setVisibility(View.GONE);
        }
    }

}
