package org.zankio.cculife.CCUService.kiki.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.kiki.database.KikiDatabaseHelper;

public abstract class DatabaseBaseSource<T> extends BaseSource<T> {
    private static KikiDatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    protected DatabaseBaseSource(BaseRepo context, SourceProperty property) {
        super(context, property);
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


    @Override
    public abstract T fetch(String type, Object... arg) throws Exception;
}
