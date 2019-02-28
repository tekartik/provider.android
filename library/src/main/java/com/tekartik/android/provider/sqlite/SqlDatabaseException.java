package com.tekartik.android.provider.sqlite;

import android.database.SQLException;

/**
 * The only exception we throw
 *
 * @author Alex
 */
public class SqlDatabaseException extends IllegalArgumentException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Type type;

    public SqlDatabaseException(Type type, String detailMessage) {
        super(detailMessage);
        this.type = type;
    }

    public SqlDatabaseException(Type type, String detailMessage, Throwable th) {
        super(detailMessage, th);
        this.type = type;
    }

    public SqlDatabaseException(Throwable th) {
        super(th);
    }

    public static boolean isNoSuchTable(Exception e) {
        return (e.getMessage().contains("no such table"));
    }

    public static boolean isNoSuchColumn(Exception e) {
        return (e.getMessage().contains("no such column"));
    }

    public boolean isNoSuchTable() {
        Throwable throwable = getCause();
        if (throwable instanceof SQLException) {
            return throwable.getMessage().contains("no such table");
        }
        return false;
    }

    public enum Type {
        FATAL_ERR, NOT_FOUND
    }
}
