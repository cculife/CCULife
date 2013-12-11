package org.zankio.cculife.CCUService.SourceSwitcher;

import org.zankio.cculife.CCUService.Source.ISource;

public interface ISwitcher {
    public ISource getSource();
    public void closeSource();
    public void openSource();
}
