package com.tekartik.android.provider.sqlite;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

public class Matcher {
	UriMatcher uriMatcher;
	SparseArray<Target> targets;

	String authority;
	
	public UriMatcher getUriMatcher() {
		return uriMatcher;
	}

	public Matcher(String authority) {
		this.authority = authority;
		targets = new SparseArray<Target>();
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	}

	public class Target {
		public Target(int code, String tableNames) {
			this(code, tableNames, false);
		}

		public Target(int code, String tableNames, boolean byId) {
			this.code = code;
			this.tableNames = tableNames;
			this.byId = byId;
		}

		public int code;
		private String tableNames;
		public String getTable() {
			return tableNames;
		}
		public boolean byId;
	}

	public void add(String path, int code, String tableNames) {
		uriMatcher.addURI(authority, path, code);
		Target target = new Target(code, tableNames);
		targets.put(code, target);
	}

	public void addById(String path, int code, String table) {
		uriMatcher.addURI(authority, path + "/#", code);
		Target target = new Target(code, table, true);
		targets.put(code, target);
	}

	public Target match(Uri uri) {
		int code = uriMatcher.match(uri);
		return targets.get(code);
	}
}
