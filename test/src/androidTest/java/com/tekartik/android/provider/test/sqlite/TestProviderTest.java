package com.tekartik.android.provider.test.sqlite;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.rule.provider.ProviderTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tekartik.android.provider.sqlite.SqlDatabase;
import com.tekartik.android.provider.test.sqlite.data.TestContract;
import com.tekartik.android.provider.test.sqlite.data.TestProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by alex on 13/09/17.
 */
@RunWith(AndroidJUnit4.class)
public class TestProviderTest {

    final ProviderTestRule providerTestRule = new ProviderTestRule
            .Builder(TestProvider.class, TestContract.AUTHORITY).build();

    ContentResolver getProvider() {
        return providerTestRule.getResolver();
        // return provider;
    }

    @Before
    public void setUp() throws Exception {
        // quick debug
        SqlDatabase.LOGV = true;
        getProvider().call(TestContract.CONTENT_URI, TestContract.METHOD_USE_IN_MEMORY_DATABASE, null, null);
        /*
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
        getProvider().setInMemoryDatabase();
        */
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
        assertThat(getRecordCount(), is(0));
    }

    @Test
    public void insert() {
        ContentValues cv = new ContentValues();
        cv.put(TestContract.Records.FIELD_STRING_TEST, "some_content");
        getProvider().insert(TestContract.Records.CONTENT_URI, cv);
        assertThat(getRecordCount(), is(1));
        getProvider().insert(TestContract.Records.CONTENT_URI, cv);
        assertThat(getRecordCount(), is(2));
    }

    static public class UnitTestProvider extends TestProvider {

        UnitTestProvider() {
            setInMemoryDatabase();
        }

    }
}
