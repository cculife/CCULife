package org.zankio.cculife.ui.base.helper;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

public class Message {
    private final Fragment fragment;
    private final int ID_TEXTVIEW_MESSAGE;
    private final int ID_VIEW_LOADING;
    private final int ID_VIEW_MESSAGE_PANEL;
    private final int ID_VIEW_CONTENT;

    public Message(
            @NonNull Fragment fragment,
            int id_textview_message,
            int id_view_loading,
            int id_view_message_panel,
            int id_view_content
    ) {
        this.fragment = fragment;
        this.ID_TEXTVIEW_MESSAGE = id_textview_message;
        this.ID_VIEW_LOADING = id_view_loading;
        this.ID_VIEW_MESSAGE_PANEL = id_view_message_panel;
        this.ID_VIEW_CONTENT = id_view_content;
    }

    public void hide() {
        View view = fragment.getView();
        View currentView;
        if (view == null) return;

        currentView = view.findViewById(ID_VIEW_CONTENT);
        if (currentView != null)
            currentView.setVisibility(View.VISIBLE);

        currentView = view.findViewById(ID_VIEW_MESSAGE_PANEL);
        if (currentView != null)
            currentView.setVisibility(View.GONE);
    }

    public void show(String message) {
        show(message, false);
    }

    public void show(String message, boolean loading) {
        View view = fragment.getView();
        View currentView;
        if (view == null) return;

        currentView = view.findViewById(ID_TEXTVIEW_MESSAGE);
        if (currentView != null)
            ((TextView) currentView).setText(message);

        currentView = view.findViewById(ID_VIEW_LOADING);
        if (currentView != null)
            currentView.setVisibility(loading ? View.VISIBLE : View.GONE);

        currentView = view.findViewById(ID_VIEW_MESSAGE_PANEL);
        if (currentView != null)
            currentView.setVisibility(View.VISIBLE);

        currentView = view.findViewById(ID_VIEW_CONTENT);
        if (currentView != null)
            currentView.setVisibility(View.GONE);
    }
}
