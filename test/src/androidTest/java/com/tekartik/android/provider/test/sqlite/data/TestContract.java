package com.tekartik.android.provider.test.sqlite.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.tekartik.android.provider.Provider;
import com.tekartik.android.provider.sqlite.SqlContract;


public class TestContract extends SqlContract {

    /**
     * Out contract
     */
    public static final String AUTHORITY = "com.tekartik.provider.test.sqlite";
    // The uri notified
    public static final Uri CONTENT_URI = Uri
            .parse(Provider.SCHEME + AUTHORITY);

    private TestContract() {
    }

    /**
     * Notes table contract
     */
    public static final class RecordLists implements BaseColumns {
        //public static final String IdField id = new IdField("rlId");

        /**
         * Path part for the Notes URI
         */
        public static final String PATH = "record_lists";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(Provider.SCHEME
                + AUTHORITY + "/" + PATH);
    }

    /**
     * Notes table contract
     */
    public static final class Records implements BaseColumns {

        public static final String FIELD_STRING_TEST = "test_string";
        /**
         * Path part for the Notes URI
         */
        public static final String PATH = "records";
        //public static final LongField testLong = new LongField("test_long");
        //public static final LinkField testLink = new LinkField(RecordLists.id, "lists_id");
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(Provider.SCHEME
                + AUTHORITY + "/" + PATH);

        // This class cannot be instantiated
        private Records() {
        }
    }

    public static final class ListRecords {
        /**
         * Path part for the Notes URI
         */
        public static final String PATH = "list_records";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(Provider.SCHEME
                + AUTHORITY + "/" + PATH);

        // This class cannot be instantiated
        private ListRecords() {
        }
    }

}
