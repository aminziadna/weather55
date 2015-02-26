package com.chde.weather55;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

public class WeatherWidget extends AppWidgetProvider implements java.util.Observer{
    public static final int GRAPH_HEIGHT = 25;
    public static final int GRAPH_WIDTH = 120;
    float dpScale;
    public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
    public TempModel curTemp = new TempModel();

    public WeatherWidget() {
        super();
        //Log.d("CHDE", "Weather widget constructed" );
        curTemp.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        //Log.d("CHDE", "Observer update fired" );
        buildWidget();

    }

    private void buildWidget()
    {
        if (curTemp.context != null)
        {
            dpScale = DpToPixels(1, curTemp.context);
            RemoteViews remoteViews = new RemoteViews(curTemp.context.getPackageName(), R.layout.main);
            Intent active = new Intent(curTemp.context, WeatherWidget.class);
            active.setAction(ACTION_WIDGET_RECEIVER);
            active.putExtra("msg", "Update");
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(curTemp.context, 0, active, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_refresh, actionPendingIntent);

            remoteViews.setTextViewText(R.id.widget_textview, curTemp.getTextTemp() + "°");

            String refreshTime = (new SimpleDateFormat("HH:mm")).format(Calendar.getInstance().getTime());
            remoteViews.setTextViewText(R.id.widget_timetext, refreshTime);




            Bitmap mBitmap = buildGraph();
            remoteViews.setImageViewBitmap(R.id.widget_graphImage, mBitmap);


            AppWidgetManager.getInstance(curTemp.context).updateAppWidget(new ComponentName(curTemp.context, WeatherWidget.class), remoteViews);


            /*if (records!=null && !records.isEmpty())
            {
                Toast.makeText(curTemp.context, (records.get(records.size()-1)).getStringT()+" at "+
                                (records.get(records.size()-1)).getStringTime(), Toast.LENGTH_LONG).show();
            }*/
        }
    }

    private Bitmap buildGraph()
    {

        Paint paintGraph = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGraph.setStyle(Paint.Style.STROKE);
        paintGrid.setStyle(Paint.Style.STROKE);

        paintGrid.setColor(Color.GRAY);
        paintGraph.setColor(Color.RED);
        paintGraph.setStrokeWidth(2);
        paintGrid.setStrokeWidth(1);

        Bitmap bmp = Bitmap.createBitmap(toPx(GRAPH_WIDTH), toPx(GRAPH_HEIGHT), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(bmp);

        List<LogRecord> records = curTemp.getLogValues();

        if (records!=null) {
            float minT = (Collections.min(records)).getT();
            float maxT = (Collections.max(records)).getT();
            if (maxT == minT) maxT++;
            float yScale = GRAPH_HEIGHT / (maxT - minT); // Сколько dp в одном градусе
            yScale = yScale * dpScale;           // Сколько px в одном градусе
            Path tPath = new Path();
            tPath.moveTo(0, 0);
            int curHr = 0;

            for (LogRecord rec : records) {
                tPath.lineTo(toPx(curHr), yScale * (maxT - rec.getT()));
                curHr += 5;
            }

            mCanvas.drawPath(tPath, paintGraph);
        }

        mCanvas.drawLine(0, toPx(GRAPH_HEIGHT*0.33f), toPx(GRAPH_WIDTH), toPx(GRAPH_HEIGHT*0.33f), paintGrid);
        mCanvas.drawLine(0, toPx(GRAPH_HEIGHT*0.66f), toPx(GRAPH_WIDTH), toPx(GRAPH_HEIGHT*0.66f), paintGrid);
        mCanvas.drawLine(toPx(GRAPH_WIDTH*0.25f), 0, toPx(GRAPH_WIDTH*0.25f), toPx(GRAPH_HEIGHT), paintGrid);
        mCanvas.drawLine(toPx(GRAPH_WIDTH*0.5f), 0, toPx(GRAPH_WIDTH*0.5f), toPx(GRAPH_HEIGHT), paintGrid);
        mCanvas.drawLine(toPx(GRAPH_WIDTH*0.75f), 0, toPx(GRAPH_WIDTH*0.75f), toPx(GRAPH_HEIGHT), paintGrid);
        return bmp;
    }

    @Override
    public void onReceive(Context con, Intent intent) {
        //Log.d("CHDE", "OnReceive fired" );
        final String action = intent.getAction();
        if (ACTION_WIDGET_RECEIVER.equals(action)) {
            String msg = "Updated";
            try {
                msg = intent.getStringExtra("msg");
            } catch (NullPointerException e) {
                Log.e("Error", "msg = null");
            }
            curTemp.manualUpdate = true;
            curTemp.context = con;
            curTemp.loadTemp();
            //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
        super.onReceive(con, intent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //Log.d("CHDE", "Widget OnEnable fired" );
    }

    @Override
    public void onUpdate(Context con, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.d("CHDE", "onUpdate fired" );
        curTemp.context = con;
        curTemp.manualUpdate = false;
        curTemp.loadTemp();
        //appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    private int toPx(float dp)
    {
        return (int)(dp*dpScale);
    }
    public static float DpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float PixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
}