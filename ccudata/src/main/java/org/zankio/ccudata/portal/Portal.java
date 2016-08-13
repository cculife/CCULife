package org.zankio.ccudata.portal;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.User;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.portal.source.Authenticate;
import org.zankio.ccudata.portal.source.SSOLoginSource;

public class Portal extends Repository{
    private User user = new User(this);

    public Portal() { super(null); }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[] {
                new Authenticate(),
                new SSOLoginSource()
        };
    }

    public User user(){
        return user;
    }
}
