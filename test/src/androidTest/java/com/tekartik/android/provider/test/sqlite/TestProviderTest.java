package com.tekartik.android.provider.test.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.tekartik.android.provider.sqlite.SqlDatabase;
import com.tekartik.android.provider.test.sqlite.data.TestContract;
import com.tekartik.android.provider.test.sqlite.data.TestProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by alex on 13/09/17.
 */
@RunWith(AndroidJUnit4.class)
public class TestProviderTest extends ProviderTestCase2<TestProvider> {

    public TestProviderTest() {
        super(TestProvider.class, TestContract.AUTHORITY);
    }

    @Before
    public void setUp() throws Exception {
        // quick debug
        SqlDatabase.LOGV = true;
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }


    int getRecordCount() {
        Cursor cursor = getProvider().query(TestContract.Records.CONTENT_URI, null, null,
                null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Test
    public void empty() throws Exception {
        assertEquals(0, getRecordCount());
    }

    @Test
    public void insert() {
        ContentValues cv = new ContentValues();
        cv.put(TestContract.Records.FIELD_STRING_TEST, "some_content");
        getProvider().insert(TestContract.Records.CONTENT_URI, cv);
        assertEquals(1, getRecordCount());
        getProvider().insert(TestContract.Records.CONTENT_URI, cv);
        assertEquals(2, getRecordCount());
    }
}
