package com.tekartik.android.provider.test.sqlite.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.tekartik.android.provider.sqlite.SqlDatabase;

public class TestDatabase extends SqlDatabase {

	/**
	 * public to allow changing
	 */
	static public String DB_NAME = "test.db";
	static public int DB_VERSION = 1;
/*
	static public Table sRecordsTable;
	static public Table sRecordListsTable;
	static public View sListRecordsView;

	static public ListDeleteTrigger sRecordListTrigger;
	*/

	static final class Tables {
		static final String RECORD = "Record";
		static final String RECORD_LIST = "RecordList";

	}

	static final class Views {
		static final String LIST_RECORD = "ListRecord";
	}

	/*
	static class ListDeleteTrigger extends Trigger.Delete {

		public ListDeleteTrigger() {
			super(new TableField.Link(sRecordsTable, Records.testLink,
					sRecordListsTable));
		}

	}
	*/

	static {
		/*
		Table table = new BaseTableWithNullHack(Tables.RECORD, Records.id);
		table.add(Records.test //
				, Records.testLong //
				, Records.testLink);

		sRecordsTable = table;

		table = new BaseTableWithNullHack(Tables.RECORD_LIST, RecordLists.id);
		// table.add( );

		sRecordListsTable = table;

		sListRecordsView = new View(Views.LIST_RECORD, Arrays.asList(
				sRecordListsTable, sRecordsTable));

		sRecordListTrigger = new ListDeleteTrigger();
		*/

	}

	class TestDatabaseHelper extends DatabaseHelper {

		TestDatabaseHelper() {
			super(getContext(), DB_NAME, DB_VERSION);
		}

		@Override
		public void onCreate() {
            inTransaction(new Runnable() {
                @Override
                public void run() {
                    sqlExecute("CREATE TABLE " + Tables.RECORD + " (" +
                           BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            TestContract.Records.FIELD_STRING_TEST + " TEXT)");


                }
            });

		}

		@Override
		public void onUpgrade(int oldVersion, int newVersion) {
			/*
			if (oldVersion == 1) {
				getDatabaseView(sListRecordsView).create();
				oldVersion = 2;
			}
			if (oldVersion == 2) {
				getDatabaseTrigger(sRecordListTrigger).create();
				oldVersion = 3;
			}
			*/
		}
	}

	public TestDatabase(Context context) {
		super(context);
	}

	@Override
	public DatabaseHelper newDatabaseHelper() {
		return new TestDatabaseHelper();
	}
}
