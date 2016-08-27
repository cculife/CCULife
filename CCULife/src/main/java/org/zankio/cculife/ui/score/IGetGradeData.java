package org.zankio.cculife.ui.score;


import org.zankio.ccudata.sourcequery.model.Grade;

import rx.Observable;

public interface IGetGradeData {
    Observable<Grade> getGrade(int i);
}
