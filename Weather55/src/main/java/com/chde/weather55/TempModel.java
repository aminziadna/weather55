package com.chde.weather55;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chernyaev_de on 15.01.2015.
 */
public class TempModel extends java.util.Observable {

    public Context context;
    public boolean manualUpdate = false;

    private List<LogRecord> logValues;
    private LogDataSource datasource;
    private String textTemp;

    public TempModel() {
        this.textTemp = "--";
    }

    public void loadTemp(){
        datasource = new LogDataSource(context);
        datasource.open();
        new DownloadStrTask().execute("http://myxa.opsb.ru/graphs.html");
    }

    public String getTextTemp() {
        return textTemp;
    }

    public void setTextTemp(String curTemp) {
        this.textTemp = curTemp;
        setChanged();
        notifyObservers(curTemp);
    }

    public List<LogRecord> getLogValues() {
        return logValues;
    }

    public void setLogValues(List<LogRecord> logValues) {
        this.logValues = logValues;
    }

    class DownloadStrTask extends AsyncTask<String, Void, String> {

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                String contentAsString = istream2String(is);
                int tempPos = contentAsString.indexOf("<h2>")+34;
                //Log.d("CHDE", "The response is: " + contentAsString);

                String result = contentAsString.substring(tempPos);
                String curTime = (new SimpleDateFormat("yyyyMMdd;HH:mm")).format(Calendar.getInstance().getTime());
                if (!manualUpdate) {
                    datasource.createLogRecord(new LogRecord(result, curTime));
                }
                setLogValues(datasource.getAllLogRecords());

                return result;
            }
            catch (IOException ex)
            {
                return "??";
            }
            finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String istream2String(InputStream stream) throws IOException  {
            Reader isReader = new InputStreamReader(stream, "KOI8_R");
            BufferedReader reader = new BufferedReader(isReader);
            String line;
            while((line = reader.readLine()) != null)
            {
                if (line.contains("<h2>"))  break;
            }
            return line;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "!!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setTextTemp(result);
            datasource.close();
        }
    }
}


