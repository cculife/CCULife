package org.zankio.ccudata.ecourse.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.ecourse.database.EcourseDatabaseHelper;

public abstract class DatabaseBaseSource<TArgument, TData> extends BaseSource<TArgument, TData> {
    private static EcourseDatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    protected DatabaseBaseSource(Repository context) {
        setContext(context);
        if (databaseHelper == null) databaseHelper = new EcourseDatabaseHelper(context.getContext());
    }

    protected SQLiteDatabase getDatabase() {
        if (database == null) database = databaseHelper.getWritableDatabase();
        return database;
    }

    public static void clearData(Context context) {
        if (databaseHelper == null) databaseHelper = new EcourseDatabaseHelper(context);
        if (database == null) database = databaseHelper.getWritableDatabase();

        database.delete(EcourseDatabaseHelper.TABLE_ECOURSE_ANNOUNCE, null, null);
        database.delete(EcourseDatabaseHelper.TABLE_ECOURSE_SCORE, null, null);
        database.delete(EcourseDatabaseHelper.TABLE_ECOURSE, null, null);
        database.delete(EcourseDatabaseHelper.TABLE_ECOURSE_CLASSMATE, null, null);
    }

}
