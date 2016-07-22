package org.zankio.ccudata.ecourse.model;

import org.zankio.ccudata.kiki.Kiki;

public class KikiSemesterData {
    public Kiki kiki;
    public int year;
    public int term;

    public KikiSemesterData(Kiki kiki, int year, int term) {
        this.kiki = kiki;
        this.year = year;
        this.term = term;
    }
}
