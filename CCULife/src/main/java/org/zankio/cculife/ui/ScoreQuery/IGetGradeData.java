package org.zankio.cculife.ui.ScoreQuery;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.sourcequery.model.Grade;

public interface IGetGradeData {
    void getGrade(int i, IOnUpdateListener<Grade> listener);
}
