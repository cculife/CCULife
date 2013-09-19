package org.zankio.cculife.ui.Base;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.zankio.cculife.R;

public abstract class BasePage {
    protected LayoutInflater inflater;
    protected View PageView;
    protected View MainView;
    protected View errorPanel;
    protected TextView errorMsg;

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
            initViews();

            if(PageView != null) {
                errorMsg = (TextView) PageView.findViewById(R.id.error_message);
                errorPanel = PageView.findViewById(R.id.error_panel);
            }

        }
        return PageView;
    }

    public void hideErrorMessage() {
        if (MainView != null) MainView.setVisibility(View.VISIBLE);

        if (errorPanel != null) errorPanel.setVisibility(View.GONE);
    }

    public void showErrorMessage(String msg) {
        if (MainView != null && errorPanel != null && errorMsg != null) {
            errorMsg.setText(msg);
            errorPanel.setVisibility(View.VISIBLE);
            MainView.setVisibility(View.GONE);
        }
    }

}
