package org.zankio.ccudata.sourcequery;


import android.content.Context;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.sourcequery.source.remote.Authenticate;
import org.zankio.ccudata.sourcequery.source.remote.GradesInquiriesSource;

public class ScoreQuery extends Repository {

    public ScoreQuery(Context context) { super(context); }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[]{
                new Authenticate(),
                new GradesInquiriesSource(),
        };
    }
}
