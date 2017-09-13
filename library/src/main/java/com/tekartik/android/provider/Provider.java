package com.tekartik.android.provider;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.net.Uri;

public abstract class Provider extends ContentProvider {

	/**
	 * The scheme part for this provider's URI
	 */
	public static final String SCHEME = "content://";

	@SuppressLint("DefaultLocale")
	public static boolean readBooleanQueryParameter(Uri uri, String name,
                                                    boolean defaultValue) {
		final String flag = uri.getQueryParameter(name);
		return flag == null ? defaultValue : (!"false".equals(flag
				.toLowerCase()) && !"0".equals(flag.toLowerCase()));
	}

	public static boolean uriIsNoChangeNotification(Uri uri) {
		return readBooleanQueryParameter(uri, Contract.NO_CHANGE_NOTIFICATION,
				false);
	}

	public static boolean uriIsCallerSyncAdapter(Uri uri) {
		return readBooleanQueryParameter(uri, Contract.CALLER_IS_SYNCADAPTER,
				false);
	}

	public static void throwUnsupportedUri(Uri uri) {
		throw new UnsupportedUriException(uri + " not supported");
	}

	public static class UnsupportedUriException extends
            IllegalArgumentException {
		private static final long serialVersionUID = 1L;

		public UnsupportedUriException(String msg) {
			super(msg);
		}

	}

}
