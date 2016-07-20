package org.zankio.ccudata.kiki.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.kiki.database.KikiDatabaseHelper;

public abstract class DatabaseBaseSource<TArgument, TData> extends BaseSource<TArgument, TData> {
    private static KikiDatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    protected DatabaseBaseSource(Repository context) {
        setContext(context);
        if (databaseHelper == null) databaseHelper = new KikiDatabaseHelper(context.getContext());
    }

    protected SQLiteDatabase getDatabase() {
        if (database == null) database = databaseHelper.getWritableDatabase();
        return database;
    }

    public static void clearData(Context context) {
        if (databaseHelper == null) databaseHelper = new KikiDatabaseHelper(context);
        if (database == null) database = databaseHelper.getWritableDatabase();

        database.delete(KikiDatabaseHelper.TABLE_TIMETABLE, null, null);
    }
}
