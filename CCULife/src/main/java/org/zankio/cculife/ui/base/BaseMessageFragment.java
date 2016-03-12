package org.zankio.cculife.ui.base;

import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.helper.Message;

public class BaseMessageFragment extends DebugFragment {
    private final Message mMessage;
    public BaseMessageFragment() {
        mMessage = new Message(
                this,
                R.id.message,
                R.id.loading,
                R.id.message_panel,
                R.id.list
        );
    }

    public Message message() { return mMessage; }
}
