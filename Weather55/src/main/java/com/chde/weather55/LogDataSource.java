package com.chde.weather55;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LogDataSource {
/* Хранение в логе
    Хранятся последние 24 записи.
 */
    private SQLiteDatabase database;
    private LogSQLHelper dbHelper;
    private String[] allColumns = { LogSQLHelper.COLUMN_ID, LogSQLHelper.COLUMN_TEMP, LogSQLHelper.COLUMN_TIME };

    public LogDataSource(Context context) {
        dbHelper = new LogSQLHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public LogRecord createLogRecord(LogRecord logRecord) {
        //boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
        Cursor cursor2del = database.query(false, LogSQLHelper.TABLE_WEATHERLOG,
                allColumns, null, null, null, null, LogSQLHelper.COLUMN_TIME+ " desc", "23");

        long earliestId;
        if (cursor2del!=null && cursor2del.moveToLast()) {
            earliestId = cursor2del.getLong(0);
            // Храним только 24 записи.
            int deleted = database.delete(LogSQLHelper.TABLE_WEATHERLOG, LogSQLHelper.COLUMN_ID + " <" + Long.toString(earliestId), null);
            //Log.d("CHDE", "delete from  " + LogSQLHelper.TABLE_WEATHERLOG + " where id_ <= " + Long.toString(earliestId)+ "    deleted "+Integer.toString(deleted));
        }
        cursor2del.close();


        // Не удаляем, а заменяем
        ContentValues values = new ContentValues();
        values.put(LogSQLHelper.COLUMN_TEMP, logRecord.getStringT());
        values.put(LogSQLHelper.COLUMN_TIME, logRecord.getStringTime());

        long insertId = database.insert(LogSQLHelper.TABLE_WEATHERLOG, null, values);
        Cursor cursor = database.query(LogSQLHelper.TABLE_WEATHERLOG,
                allColumns, LogSQLHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        LogRecord newLogRecord = cursorToLogRecord(cursor);
        cursor.close();
        return newLogRecord;
    }

    public void deleteLogRecord(LogRecord logRecord) {
        long id = logRecord.getId();
        System.out.println("LogRecord deleted with id: " + id);
        database.delete(LogSQLHelper.TABLE_WEATHERLOG, LogSQLHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<LogRecord> getAllLogRecords() {
        List<LogRecord> logRecords = new ArrayList<LogRecord>();

        Cursor cursor = database.query(LogSQLHelper.TABLE_WEATHERLOG,
                allColumns, null, null, null, null, LogSQLHelper.COLUMN_TIME);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LogRecord logRecord = cursorToLogRecord(cursor);
            logRecords.add(logRecord);
            cursor.moveToNext();
        }
        cursor.close();

        return logRecords;
    }

    private LogRecord cursorToLogRecord(Cursor cursor) {
        LogRecord logRecord = new LogRecord();
        logRecord.setId(cursor.getLong(0));
        logRecord.setT(cursor.getString(1));
        logRecord.setTime(cursor.getString(2));
        return logRecord;
    }
} 
