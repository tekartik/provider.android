package com.tekartik.android.provider.sqlite;

import java.util.Locale;

public class Sql {
    public static final String OR = " OR ";
    public static final String EQUALS = " = ?";
    public static final String VALUE_TRUE = " > 0";
    public static final String IS_NULL = " IS NULL";
    public static final String NOT_NULL = " NOT NULL";
    public static final String AND = " AND ";
    public static final String COMMA = ", ";
    public static final String DOT = ".";

    static public StringBuilder appendColumn(StringBuilder sb, String table, String columnName) {
        sb.append(table);
        sb.append('.');
        sb.append(columnName);
        return sb;
    }

    static public StringBuilder appendColumnAs(StringBuilder sb, String table, String columnName) {
        appendColumn(sb, table, columnName);
        sb.append(" AS ");
        sb.append(columnName);
        return sb;
    }

    static public StringBuilder appendNextColumnAs(StringBuilder sb, String table, String columnName) {
        sb.append(", ");
        return appendColumnAs(sb, table, columnName);
    }

    static public String fixWhereClause(String whereClause) {
        if (whereClause == null) {
            return "1 = 1";
        } else {
            return whereClause;
        }
    }

    static public String buildIdSelection(String selection, String idColumnName, long id) {
        String idSelection = idColumnName + " = " + id;
        if (selection == null) {
            selection = idSelection;
        } else {
            selection = String.format((Locale) null, "(%s) AND (%s)", selection, idSelection);
        }
        return selection;
    }
}
