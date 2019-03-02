package com.tekartik.android.provider.test.sqlite;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.rule.provider.ProviderTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tekartik.android.provider.sqlite.SqlDatabase;
import com.tekartik.android.provider.test.sqlite.data.TestContract;
import com.tekartik.android.provider.test.sqlite.data.TestProvider;
import com.tekartik.android.utils.CursorUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.tekartik.android.provider.test.sqlite.data.TestContract.Records.FIELD_STRING_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by alex on 13/09/17.
 */
@RunWith(AndroidJUnit4.class)
public class TestProviderTest {

    final ProviderTestRule providerTestRule = new ProviderTestRule
            .Builder(TestProvider.class, TestContract.AUTHORITY).build();

    ContentResolver getContentResolver() {
        return providerTestRule.getResolver();
    }

    @Before
    public void setUp() throws Exception {
        // quick debug
        SqlDatabase.LOGV = true;
        getContentResolver().call(TestContract.CONTENT_URI, TestContract.METHOD_USE_IN_MEMORY_DATABASE, null, null);
    }

    int getRecordCount() {
        Cursor cursor = getContentResolver().query(TestContract.Records.CONTENT_URI, null, null,
                null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Test
    public void empty() throws Exception {
        assertThat(getRecordCount(), is(0));
    }

    @Test
    public void insert() {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_STRING_TEST, "some_content");
        getContentResolver().insert(TestContract.Records.CONTENT_URI, cv);
        assertThat(getRecordCount(), is(1));
        getContentResolver().insert(TestContract.Records.CONTENT_URI, cv);
        assertThat(getRecordCount(), is(2));
    }

    @Test
    public void update() {
        assertThat(getRecordCount(), is(0));
        ContentValues cv = new ContentValues();
        cv.put(FIELD_STRING_TEST, "some_content");
        Uri uri = getContentResolver().insert(TestContract.Records.CONTENT_URI, cv);
        cv = new ContentValues();
        cv.put(FIELD_STRING_TEST, "new_content");
        int updated = getContentResolver().update(uri, cv, null, null);
        assertThat(updated, is(1));
        cv = CursorUtils.getFirstContentValuesAndClose(getContentResolver().query(uri, null, null, null, null));
        assertThat(cv.getAsString(FIELD_STRING_TEST), is("new_content"));
    }

    @Test
    public void delete() {
        assertThat(getRecordCount(), is(0));
        ContentValues cv = new ContentValues();
        cv.put(FIELD_STRING_TEST, "some_content");
        Uri uri = getContentResolver().insert(TestContract.Records.CONTENT_URI, cv);
        assertThat(getRecordCount(), is(1));
        getContentResolver().delete(uri, null, null);
        assertThat(getRecordCount(), is(0));
    }

}
