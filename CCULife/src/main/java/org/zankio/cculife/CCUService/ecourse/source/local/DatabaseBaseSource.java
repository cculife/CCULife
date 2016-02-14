package org.zankio.cculife.CCUService.ecourse.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.database.EcourseDatabaseHelper;

public abstract class DatabaseBaseSource<T> extends BaseSource<T> {
    private static EcourseDatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    protected DatabaseBaseSource(BaseRepo context, SourceProperty property) {
        super(context, property);
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


    @Override
    public abstract T fetch(String type, Object... arg) throws Exception;
}
