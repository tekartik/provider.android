package com.tekartik.android.provider.test.sqlite.data;

import android.net.Uri;

import com.tekartik.android.provider.sqlite.Matcher;
import com.tekartik.android.provider.sqlite.SqlDatabase;
import com.tekartik.android.provider.sqlite.SqlProvider;

public class TestProvider extends SqlProvider {

    static final int RECORDS = 1;
    static final int RECORD_ID = 2;
    static final int RECORD_LISTS = 3;
    static final int RECORD_LIST_ID = 4;

    static final int LIST_RECORDS = 5;
    static final int LIST_RECORD_ID = 6;

    static final Matcher sMatcher = new Matcher(TestContract.AUTHORITY);

    static {
        sMatcher.add(TestContract.RecordLists.PATH, RECORD_LISTS, TestDatabase.Tables.RECORD_LIST
        );
        sMatcher.addById(TestContract.RecordLists.PATH, RECORD_LIST_ID, TestDatabase.Tables.RECORD_LIST);
        // sMatcher.add(Records.PATH, RECORDS, Tables.RECORD);
        sMatcher.add(TestContract.Records.PATH, RECORDS, TestDatabase.Tables.RECORD);
        sMatcher.addById(TestContract.Records.PATH, RECORD_ID, TestDatabase.Tables.RECORD);
		/*
		sMatcher.add(TestContract.ListRecords.PATH, TestDatabase.sListRecordsView,
				LIST_RECORDS, LIST_RECORD_ID);
				*/
    }

    // private TestDatabase getTestDatabase() {
    // return (TestDatabase) db;
    // }

    @Override
    protected SqlDatabase newDatabase() {
        if (db != null) {
            throw new RuntimeException("db already created");
        }
        SqlDatabase db = new TestDatabase(getContext());
        return db;
    }

    @Override
    public Uri getContentUri() {
        return TestContract.CONTENT_URI;
    }

    // @Override
    // public Cursor query(Uri uri, String[] projection, String selection,
    // String[] selectionArgs, String sortOrder, boolean callerIsSyncAdapter) {
    // Target target = sMatcher.matchQuery(uri);
    // // if (target.code == LIST_RECORDS) {
    // // SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    // // qb.setTables(target.tableNames);
    // // qb.appendWhere(Records.testLink.joinWhere());
    // // // getColumnName(TestDatabase.sRecordsTable) + " = " // +
    // RecordLists.id.getColumnName(TestDatabase.sRecordListsTable));
    // // return db.query(qb, projection, selection, selectionArgs, sortOrder);
    // // } else {
    // return super.query(target, uri, projection, selection, selectionArgs,
    // sortOrder, callerIsSyncAdapter);
    // }
    // }

    @Override
    protected Matcher getMatcher() {
        return sMatcher;
    }
}
