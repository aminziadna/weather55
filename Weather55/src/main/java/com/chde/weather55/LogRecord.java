package com.chde.weather55;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chernyaev_de on 19.01.2015.
 */
public class LogRecord implements Comparator<LogRecord>, Comparable<LogRecord> {
    private long id;
    private String t;
    private String logtime;

    @Override
    public int compareTo(LogRecord rec2) {
        return (int)((this.getT()- rec2.getT())*10);
    }

    @Override
    public int compare(LogRecord rec1, LogRecord rec2) {
        return (int)((this.getT()- rec2.getT())*10);
    }

    public LogRecord() {
        super();
    }

    public LogRecord(String t, String logtime) {
        this.t = t;
        this.logtime = logtime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStringT() {
        return t;
    }

    public float getT() {
        return Float.parseFloat(t);
    }

    public String getStringTime() {
        return logtime;
    }

    public Date getTime() throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyyMMdd;HH:mm", Locale.ENGLISH);
        Date date;
        try {
            date = format.parse(logtime);
        }
        catch (ParseException ex)
        {
            return null;
        }
        return date;
    }

     public void setT(String temp) {
        this.t = temp;
    }

    public void setTime(String time) {
        this.logtime = time;
    }


    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return logtime+":"+t;
    }
}
