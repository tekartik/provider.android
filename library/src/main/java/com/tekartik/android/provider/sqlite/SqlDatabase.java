package com.tekartik.android.provider.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;
import android.os.Build;
import android.util.Log;

import com.tekartik.android.utils.LogUtils;

public abstract class SqlDatabase {

    public static final String FIELD_NULL_HACK = "_nullHack"; // String?
    static public final String TAG;
    static public long INVALID_ID = -1L;
    static public boolean LOGV;

    static {
        TAG = LogUtils.tag("SqlDatabase");
        LOGV = Log.isLoggable(TAG, Log.VERBOSE);
    }

    boolean inMemory;
    Context context;
    int transactionDebugCount = 0;
    boolean transactionDebugSuccess = false;
    // never access directly unless for close
    private DatabaseHelper dbHelper;
    private SQLiteDatabase onOpeningDatabase; // only valid at the beginning

    public SqlDatabase(Context context) {
        // Do not use ApplicationContext as it is wrong during testing
        this.context = context; //.getApplicationContext();

        if (LOGV) {
            Log.d(TAG, "SqlDatabase::SqlDatabase()");
        }
    }

    static public void sqlExecute(SQLiteDatabase db, String sql) {
        if (LOGV) {
            Log.v(TAG, sql);
        }
        db.execSQL(sql);
    }

    @SuppressLint("NewApi")
    public static int updateWithOnConflict(SQLiteDatabase db, String table,
                                           ContentValues values, String selection, String[] selectionArgs,
                                           int conflictAlgorithm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            // V8
            return db.updateWithOnConflict(table, values, selection,
                    selectionArgs, conflictAlgorithm);
        } else {
            return db.update(table, values, selection, selectionArgs);
        }
    }

    static protected String toString(Object[] columns) {
        if (columns == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(128);

        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(columns[i]);
        }
        return sb.toString();
    }

    public Context getContext() {
        return context;
    }

    void setInMemory(boolean inMemory) {
        if (isOpened()) {
            throw new RuntimeException(
                    "can't change database setting 'in memory' when already opened");
        }

        this.inMemory = inMemory;
    }

    private boolean isOpened() {
        return dbHelper != null;
    }

    private DatabaseHelper getHelper() {
        if (dbHelper == null) {
            synchronized (this) {
                if (dbHelper == null) {
                    dbHelper = newDatabaseHelper();
                }
            }
        }
        return dbHelper;
    }

    abstract public DatabaseHelper newDatabaseHelper();

    /**
     * Only called during testing
     *
     * @throws SqlDatabaseException
     */
    public void close() {
        if (dbHelper != null) {
            if (LOGV) {
                Log.i(TAG, "Closing database");
            }
            try {
                dbHelper.close();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            dbHelper = null;
        }

    }

    public SQLiteDatabase getWritableDatabase() {
        if (onOpeningDatabase != null) {
            return onOpeningDatabase;
        }
        return getHelper().getWritableDatabase();
    }

    protected SQLiteDatabase getReadableDatabase() {
        if (onOpeningDatabase != null) {
            return onOpeningDatabase;
        }
        return getHelper().getReadableDatabase();
    }

    public void sqlExecute(String sql) {
        if (LOGV) {
            Log.v(TAG, sql);
        }
        getWritableDatabase().execSQL(sql);
    }

    public long sqlInsert(String table, String nullColumnHack,
                          ContentValues values) throws SqlDatabaseException {

        try {
            long id = getWritableDatabase().insertOrThrow(table,
                    nullColumnHack, values);
            if (LOGV) {
                Log.d(TAG,
                        "INSERT " + id + " INTO " + table + ": "
                                + values.toString());
            }
            return id;
        } catch (SQLException e) {
            if (LOGV) {
                Log.i(TAG,
                        "INSERT failed INTO " + table + ": "
                                + values.toString());
            }
            throw new SqlDatabaseException(e);
        }
    }

    public int sqlUpdate(String table, ContentValues values, String selection,
                         String[] selectionArgs) throws SqlDatabaseException {
        return sqlUpdate(table, values, selection, selectionArgs, //
                // SQLiteDatabase.CONFLICT_NONE);
                0);

    }

    public int sqlUpdate(String table, ContentValues values, String selection,
                         String[] selectionArgs, int conflictAlgorithm)
            throws SqlDatabaseException {
        try {
            int count = updateWithOnConflict(getWritableDatabase(), table,
                    values, selection, selectionArgs, conflictAlgorithm);
            if (LOGV) {
                Log.i(TAG, "UPDATE " + count + " INTO " + table + " WHERE "
                        + selection + " " + toString(selectionArgs) + ": "
                        + values.toString());
            }
            return count;
        } catch (SQLException e) {
            if (LOGV) {
                Log.i(TAG, "UPDATE failed INTO " + table + " WHERE "
                        + selection + " " + toString(selectionArgs) + ": "
                        + values.toString());
            }
            throw new SqlDatabaseException(e);
        }
    }

    public int sqlDelete(String table, String whereClause, String[] whereArgs)
            throws SqlDatabaseException {
        whereClause = Sql.fixWhereClause(whereClause);
        try {
            int count = getWritableDatabase().delete(table, whereClause,
                    whereArgs);
            if (LOGV) {
                Log.i(TAG, "DELETE " + count + " FROM " + table + " WHERE "
                        + whereClause + " " + toString(whereArgs));
            }
            return count;
        } catch (SQLException e) {
            if (LOGV) {
                Log.i(TAG, "DELETE failed FROM " + table + " WHERE "
                        + whereClause + " " + toString(whereArgs));
            }
            throw new SqlDatabaseException(e);
        }
    }

    /**
     * Common query
     *
     * @param qb
     * @param projectionIn
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param sortOrder
     * @return
     * @throws SqlDatabaseException
     */
    public Cursor query(SQLiteQueryBuilder qb, String[] projectionIn,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String sortOrder) throws SqlDatabaseException {
        try {
            Cursor cursor = qb.query(getWritableDatabase(), projectionIn,
                    selection, selectionArgs, groupBy, having, sortOrder);
            if (LOGV)
                Log.i(TAG,
                        "QUERY " + cursor.getCount() + " \'" + qb.getTables()
                                + "\' (" + toString(projectionIn) + ") WHERE "
                                + selection + ", " + toString(selectionArgs));
            return cursor;
        } catch (SQLException e) {
            throw new SqlDatabaseException(e);
        }
    }

    /**
     * Common query
     *
     * @param qb
     * @param projectionIn
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     * @throws SqlDatabaseException
     */
    public Cursor query(SQLiteQueryBuilder qb, String[] projectionIn,
                        String selection, String[] selectionArgs, String sortOrder)
            throws SqlDatabaseException {
        return query(qb, projectionIn, selection, selectionArgs, null, null,
                sortOrder);
    }

    public Cursor select(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy) {
        if (LOGV)
            Log.i(TAG, "SELECT \'" + table + "\' (" + toString(columns)
                    + ") WHERE " + selection + ", " + toString(selectionArgs));
        return getReadableDatabase().query(table, columns, selection,
                selectionArgs, groupBy, having, orderBy);

    }

    public Cursor select(String sql, String[] selectionArgs) {
        if (LOGV) {
            Log.i(TAG, sql + " " + toString(selectionArgs));
        }
        try {
            return getReadableDatabase().rawQuery(sql, selectionArgs);
        } catch (SQLiteException e) {
            if (SqlDatabaseException.isNoSuchTable(e)) {
                if (LOGV) {
                    Log.i(TAG, e.getMessage());
                }
                return null;
            }
            throw e;
        }
    }

    /**
     * require a statement such as SELECT COUNT(*) FROM
     *
     * @param sql
     * @param selectionArgs
     * @return
     */
    public long sqlCount(String sql, String[] selectionArgs) {
        Cursor cursor = select(sql, selectionArgs);
        if ((cursor == null) || (cursor.getCount() == 0)
                || (!cursor.moveToFirst())) {
            return 0;
        }
        return cursor.getLong(0);
    }

    /*
     *
     * try {
     *
     * } finally { cursor.close(); }
     */
    public Cursor select(boolean distinct, String table, String[] columns,
                         String selection, String[] selectionArgs, String groupBy,
                         String having, String orderBy, String limit) {
        if (LOGV) {
            String msg = "SELECT \'" + table + "\' (" + toString(columns) + ")";
            if (selection != null) {
                msg += " WHERE " + selection;
            }
            if (selectionArgs != null) {
                msg += ", " + toString(selectionArgs);
            }
            if (groupBy != null) {
                msg += " GROUPBY " + groupBy;
            }
            if (having != null) {
                msg += " HAVING " + having;
            }
            if (orderBy != null) {
                msg += " ORDERBY " + orderBy;
            }
            if (limit != null) {
                msg += " LIMIT " + limit;
            }

            Log.i(TAG, msg);
        }
        return getReadableDatabase().query(distinct, table, columns, selection,
                selectionArgs, groupBy, having, orderBy, limit);

    }

    public synchronized void beginTransactionWithListener(
            SQLiteTransactionListener listener) {
        if (listener != null) {
            getWritableDatabase().beginTransactionWithListener(listener);
        } else {
            getWritableDatabase().beginTransaction();
        }
        if (transactionDebugCount++ == 0) {
            // transactionChange = 0;
            if (LOGV) {
                Log.v(TAG, "BEGIN TRANSACTION");
            }
        }

        transactionDebugSuccess = false;
    }

    // private boolean LOGD = LogUtils.isLoggable(TAG, Log.DEBUG);

    public void beginTransaction() {
        beginTransactionWithListener(null);
    }

    // /**
    // * when db is not set yet
    // *
    // * @param db
    // */
    // static public void beginTransaction(SQLiteDatabase db) {
    // db.beginTransaction();
    // if (LOGV) {
    // Log.v(TAG, "INITIAL BEGIN TRANSACTION");
    // }
    // }
    //
    // static public void setTransactionSuccessfull(SQLiteDatabase db) {
    // db.setTransactionSuccessful();
    // if (LOGV) {
    // Log.v(TAG, "INITIAL SUCCESS TRANSACTION");
    // }
    // }
    //
    // public static void endTransaction(SQLiteDatabase db) {
    // if (LOGV) {
    // Log.d(TAG, "INITIAL END TRANSACTION");
    // }
    // db.endTransaction();
    // }

    public void inTransaction(Runnable runnable) {
        beginTransaction();
        try {
            runnable.run();
            setTransactionSuccessfull();
        } finally {
            endTransaction();
        }
    }

    public synchronized void endTransaction() {
        if (--transactionDebugCount == 0) {
            if (LOGV)
                Log.i(TAG, "END TRANSACTION "
                        + (transactionDebugSuccess ? "commit" : "rollback"));
        }

        getWritableDatabase().endTransaction();
    }

    public void setTransactionSuccessfull() {
        transactionDebugSuccess = (transactionDebugCount == 1);
        getWritableDatabase().setTransactionSuccessful();
    }

    /**
     * Raw sql
     *
     * @param sql
     * @return
     */
    public SQLiteStatement compileStatement(String sql) {
        if (LOGV) {
            Log.v(TAG, "compiling: " + sql);
        }
        return getWritableDatabase().compileStatement(sql);
    }

    public void yieldIfContendedSafely() {
        getWritableDatabase().yieldIfContendedSafely();
    }

    /**
     * To override
     *
     * @author alex
     */
    public abstract class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, int version) {
            // The trick for sqlite db in memory is to have a null name
            super(context, inMemory ? null : name, null, version);
        }

        public abstract void onCreate();

        public void onOpen() {
        }

        public abstract void onUpgrade(int oldVersion, int newVersion);

        // Default implementation crashes, good
        public void onDowngrade(int oldVersion, int newVersion) {
            super.onDowngrade(onOpeningDatabase, oldVersion, newVersion);
        }

        @Override
        public final void onCreate(SQLiteDatabase db) {
            onOpeningDatabase = db;
            onCreate();
            onOpeningDatabase = null;
        }

        @Override
        final public void onUpgrade(SQLiteDatabase db, int oldVersion,
                                    int newVersion) {
            onOpeningDatabase = db;
            onUpgrade(oldVersion, newVersion);
            onOpeningDatabase = null;
        }

        @Override
        public final void onOpen(SQLiteDatabase db) {
            onOpeningDatabase = db;
            onOpen();
            onOpeningDatabase = null;
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onOpeningDatabase = db;
            onDowngrade(oldVersion, newVersion);
            onOpeningDatabase = null;
        }
    }

}
