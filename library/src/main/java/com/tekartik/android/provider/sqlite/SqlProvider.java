package com.tekartik.android.provider.sqlite;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


import com.tekartik.android.provider.Provider;
import com.tekartik.android.utils.LogUtils;

import java.util.ArrayList;

/**
 * 
 * adb -e shell setprop log.tag.tkProvider VERBOSE
 * 
 * @author alex
 * 
 */
public abstract class SqlProvider extends Provider implements
        SQLiteTransactionListener {

	static public final String TAG;
	static public final boolean LOGV;
	static {
		TAG = LogUtils.tag("SqlProvider");
		LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	}

	protected SqlDatabase db;

	public void setInMemoryDatabase() {
		db.setInMemory(true);
	}
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	private volatile boolean mNotifyChange;

	private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<Boolean>();

	private Boolean mIsCallerSyncAdapter;

	@Override
	public boolean onCreate() {
		if (LogUtils.isLoggable(TAG, Log.VERBOSE)) {
			Log.v(TAG, "onCreate() " + this);
		}
		// Context context = getContext();

		// Save global context
		// sContext = context;

		db = newDatabase();

		return true;
	}

	protected abstract SqlDatabase newDatabase();

	/**
	 * The equivalent of the {@link #insert} method, but invoked within a
	 * transaction.
	 */
	/*
	abstract protected Uri insertInTransaction(Uri uri, ContentValues values,
                                               boolean callerIsSyncAdapter);

	abstract protected int updateInTransaction(Uri uri, ContentValues values,
                                               String selection, String[] selectionArgs,
                                               boolean callerIsSyncAdapter);

	abstract protected int deleteInTransaction(Uri uri, String selection,
                                               String[] selectionArgs, boolean callerIsSyncAdapter);
                                               */
	/**
	 * The equivalent of the {@link #insert} method, but invoked within a
	 * transaction.
	 */
	protected Uri insertInTransaction(Uri uri, ContentValues values,
									  boolean callerIsSyncAdapter) {
		long id = SqlDatabase.INVALID_ID;
		Matcher.Target target = getMatcher().match(uri);
		if (target != null) {
			return insertInTransaction(target, uri, values, callerIsSyncAdapter);
		} else {
			throwUnsupportedUri(uri);
		}
		return getUri(uri, id);

	}

	/**
	 * The equivalent of the {@link #insert} method, but invoked within a
	 * transaction.
	 */
	protected Uri insertInTransaction(Matcher.Target target, Uri uri,
									  ContentValues values, boolean callerIsSyncAdapter) {
		long id = SqlDatabase.INVALID_ID;

		id = db.sqlInsert(target.getTable(), SqlDatabase.FIELD_NULL_HACK, values);

		return getUri(uri, id);

	}

	protected int updateInTransaction(Uri uri, ContentValues values,
									  String selection, String[] selectionArgs,
									  boolean callerIsSyncAdapter) {
		Matcher.Target target = getMatcher().match(uri);
		if (target != null) {
			return updateInTransaction(target, uri, values, selection,
					selectionArgs, callerIsSyncAdapter);
		} else {
			throwUnsupportedUri(uri);
			return 0;
		}

	}

	/**
	 * The equivalent of the {@link #update} method, but invoked within a
	 * transaction.
	 */
	protected int updateInTransaction(Matcher.Target target, Uri uri,
									  ContentValues values, String selection, String[] selectionArgs,
									  boolean callerIsSyncAdapter) {
		long id;
		String targetTable = target.getTable();
		if (target.byId) {
			id = ContentUris.parseId(uri);
			selection = Sql.buildIdSelection(selection,
					BaseColumns._ID, id);
			return db.sqlUpdate(targetTable, values, selection,
					selectionArgs);

		} else {
			return db.sqlUpdate(targetTable, values, selection,
					selectionArgs);
		}

	}

	// protected abstract int updateInTransaction(Uri uri, ContentValues values,
	// String selection, String[] selectionArgs, boolean callerIsSyncAdapter);

	protected int deleteInTransaction(Uri uri, String selection,
									  String[] selectionArgs, boolean callerIsSyncAdapter) {
		Matcher.Target target = getMatcher().match(uri);
		if (target != null) {
			return deleteInTransaction(target, uri, selection, selectionArgs,
					callerIsSyncAdapter);
		} else {
			throwUnsupportedUri(uri);
			return 0;
		}
	}

	/**
	 * The equivalent of the {@link #delete} method, but invoked within a
	 * transaction.
	 */
	protected int deleteInTransaction(Matcher.Target target, Uri uri, String selection,
									  String[] selectionArgs, boolean callerIsSyncAdapter) {
		long id;

		if (target.byId) {
			String targetTable = target.getTable();
			id = ContentUris.parseId(uri);
			selection = Sql.buildIdSelection(selection, BaseColumns._ID, id);
			return db.sqlDelete(target.getTable(), selection,
					selectionArgs);

		} else {
			return db.sqlDelete(target.getTable(), selection,
					selectionArgs);
		}

	}

	// protected abstract Cursor query(Uri uri, String[] projection, String
	// selection, String[] selectionArgs, String sortOrder, boolean
	// callerIsSyncAdapter);
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder,
						boolean callerIsSyncAdapter) {
		Matcher.Target target = getMatcher().match(uri);
		if (target != null) {
			return query(target, uri, projection, selection, selectionArgs,
					sortOrder, callerIsSyncAdapter);
		} else {
			throwUnsupportedUri(uri);
			return null;
		}
	}

	public Cursor query(Matcher.Target target, Uri uri, String[] projection,
						String selection, String[] selectionArgs, String sortOrder,
						boolean callerIsSyncAdapter) {
		long id;

		if (target.byId) {
			id = ContentUris.parseId(uri);
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(target.getTable());
			selection = Sql.buildIdSelection(selection, BaseColumns._ID, id);
			return db
					.query(qb, projection, selection, selectionArgs, sortOrder);

		} else {
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(target.getTable());
			return db
					.query(qb, projection, selection, selectionArgs, sortOrder);
		}

	}

	protected boolean isSyncToNetwork() {
		return (null == mIsCallerSyncAdapter) || (!mIsCallerSyncAdapter);
	}

	public abstract Uri getContentUri();
	protected void notifyChange(boolean syncToNetwork) {
		// Note that semantics are changed: notification is for CONTENT_URI, not
		// the specific Uri that was modified.
		getContext().getContentResolver().notifyChange(
				getContentUri(), null, syncToNetwork);

	}

	private boolean applyingBatch() {
		return mApplyingBatch.get() != null && mApplyingBatch.get();
	}

	private void beginTransaction() {
		db.beginTransactionWithListener(this);
	}

	private void commit() {
		db.setTransactionSuccessfull();
	}

	private void endTransaction() {
		db.endTransaction();
	}

	private void markAsChanged(Uri uri) {
		if (!mNotifyChange) {
			if (!Provider.uriIsNoChangeNotification(uri)) {
				mNotifyChange = true;
			}
		}
	}

	@Override
	final public Uri insert(Uri uri, ContentValues values) {

		Uri result = null;
		boolean applyingBatch = applyingBatch();
		boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);

		if (!applyingBatch) {
			beginTransaction();
			try {
				result = insertInTransaction(uri, values, isCallerSyncAdapter);
				if (result != null) {
					markAsChanged(uri);
				}
				commit();
			} finally {
				endTransaction();
			}

		} else {
			result = insertInTransaction(uri, values, isCallerSyncAdapter);
			if (result != null) {
				markAsChanged(uri);
			}
		}

		if (LOGV) {
			Log.v(TAG,
					String.format("%s insert(%s, %s)", result, uri,
							values.toString()));
		}

		return result;
	}

	@Override
	final public Cursor query(Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {

		boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);

		Cursor cursor = query(uri, projection, selection, selectionArgs,
				sortOrder, isCallerSyncAdapter);
		if (LogUtils.isLoggable(TAG, Log.VERBOSE)) {
			Log.v(TAG, String.format("%s query(%s, %s)", cursor.getCount(),
					uri, selection, null));
		}

		return cursor;
	}

	@Override
	final public int bulkInsert(Uri uri, ContentValues[] values) {
		int numValues = values.length;
		boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);

		beginTransaction();
		try {
			for (int i = 0; i < numValues; i++) {
				Uri result = insertInTransaction(uri, values[i],
						isCallerSyncAdapter);
				if (result != null) {
					markAsChanged(uri);
				}
				db.yieldIfContendedSafely();
			}
			commit();
		} finally {
			endTransaction();
		}
		return numValues;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
		int count = 0;
		boolean applyingBatch = applyingBatch();
		boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);

		if (!applyingBatch) {
			beginTransaction();
			try {
				count = updateInTransaction(uri, values, selection,
						selectionArgs, isCallerSyncAdapter);
				if (count > 0) {
					markAsChanged(uri);
				}
				commit();
			} finally {
				endTransaction();
			}

		} else {
			count = updateInTransaction(uri, values, selection, selectionArgs,
					isCallerSyncAdapter);
			if (count > 0) {
				markAsChanged(uri);
			}
		}

		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		boolean applyingBatch = applyingBatch();
		boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);

		if (!applyingBatch) {
			beginTransaction();
			try {
				count = deleteInTransaction(uri, selection, selectionArgs,
						isCallerSyncAdapter);
				if (count > 0) {
					markAsChanged(uri);
				}
				commit();
			} finally {
				endTransaction();
			}

		} else {
			count = deleteInTransaction(uri, selection, selectionArgs,
					isCallerSyncAdapter);
			if (count > 0) {
				markAsChanged(uri);
			}
		}
		return count;
	}

	protected boolean getIsCallerSyncAdapter(Uri uri) {

		boolean isCurrentSyncAdapter = Provider.uriIsCallerSyncAdapter(uri);
		if (mIsCallerSyncAdapter == null || mIsCallerSyncAdapter) {
			mIsCallerSyncAdapter = isCurrentSyncAdapter;
		}
		return isCurrentSyncAdapter;

	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		beginTransaction();
		try {
			mApplyingBatch.set(true);
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				final ContentProviderOperation operation = operations.get(i);
				if (i > 0 && operation.isYieldAllowed()) {
					db.yieldIfContendedSafely();
				}
				results[i] = operation.apply(this, results, i);
			}
			commit();
			return results;
		} finally {
			mApplyingBatch.set(false);
			endTransaction();
		}
	}

	@Override
	public void onBegin() {
		mIsCallerSyncAdapter = null;
	}

	@Override
	public void onCommit() {
		if (mNotifyChange) {
			mNotifyChange = false;
			// We sync to network if the caller was not the sync adapter
			notifyChange(isSyncToNetwork());
		}
	}

	@Override
	public void onRollback() {
		// not used
	}

	@Override
	public void shutdown() {
		db.close();
	}

	static public Uri getUri(Uri uri, long id) {
		if (id != SqlDatabase.INVALID_ID) {
			return ContentUris.withAppendedId(uri, id);
		}
		return null;
	}

	abstract protected Matcher getMatcher();
}
