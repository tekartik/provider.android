package com.tekartik.android.provider.test.sqlite.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.tekartik.android.provider.Provider;


public class TestContract {

	private TestContract() {
	}

	/**
	 * Here the contract is here -------------------------
	 */
	public static final String AUTHORITY = "com.tekartik.provider.test.sqlite";

	// The uri notified
	public static final Uri CONTENT_URI = Uri
			.parse(Provider.SCHEME + AUTHORITY);

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

		// This class cannot be instantiated
		private Records() {
		}

		public static final String FIELD_STRING_TEST = "test_string";
		//public static final LongField testLong = new LongField("test_long");
		//public static final LinkField testLink = new LinkField(RecordLists.id, "lists_id");
		/**
		 * Path part for the Notes URI
		 */
		public static final String PATH = "records";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(Provider.SCHEME
				+ AUTHORITY + "/" + PATH);
	}

	public static final class ListRecords {
		// This class cannot be instantiated
		private ListRecords() {
		}

		/**
		 * Path part for the Notes URI
		 */
		public static final String PATH = "list_records";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(Provider.SCHEME
				+ AUTHORITY + "/" + PATH);
	}

}
