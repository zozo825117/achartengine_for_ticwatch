/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.achartmotion;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.R;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.chartdemo.demo.chart.ZozoXYChartBuilder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fusion.FusionTask;
import com.fusion.Orientation;
import com.fusion.types.Fquaternion;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.List;


public class MotionXYChartBuilder extends Activity {
    /**
     * The main dataset that includes all the series that go into a chart.
     */
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    /**
     * The main renderer that includes all the renderers customizing a chart.
     */
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    /**
     * The most recently added series.
     */
    //private XYSeries mCurrentSeries;
    /**
     * The most recently created renderer, customizing the current series.
     */
    //private XYSeriesRenderer mCurrentRenderer;
/*  *//** Button for creating a new series of data. *//*
  //private Button mNewSeries;*/
    /**
     * Button for adding entered data to the current series.
     */
    Button mEnableButton;
    /**
     * Button for adding entered data to the current series.
     */
    Button mDisableButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mStopButton;
    /**
     * Button for adding entered data to the current series.
     */
    Button mYplusButton;
    /**
     * Button for adding entered data to the current series.
     */
    Button mYdecButton;
    /**
     * Button for adding entered data to the current series.
     */
    Button mXplusButton;
    /**
     * Button for adding entered data to the current series.
     */
    Button mXdecButton;
    /**
     * Button for adding entered data to the current series.
     */
    //private Button mClearButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mSaveButton;
    /* Edit text field for entering the Y value of the data to be added. */
    private EditText mSeries;

    /**
     * The chart view that displays the data.
     */
    private GraphicalView mChartView;

    boolean D = true;
    boolean M = true;
    boolean UseOrientation = false;

    private static final String TAG = "MotionXYChartBuilder";

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;

    double x_index = 0;
    double X_MIN = 0;
    double X_MAX = 300;
    double Y_MIN = -3;
    double Y_MAX = 3;
    float Y_min_buf = -3, Y_max_buf = 3;

    boolean EnableSerise1 = false, EnableSerise4 = false, InitSerise1 = false, InitSerise2 = false;
    double Serise1StartIndex,Serise2StartIndex;
    private XYSeries xyseries1, xyseries2, xyseries3,xyseries4, xyseries5, xyseries6;//数据
    XYSeriesRenderer datarenderer1, datarenderer2, datarenderer3,datarenderer4, datarenderer5, datarenderer6;
    //motion
    private SensorManager sm;
    private Sensor sensor;
    //toast
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Toast toast = null;
    private final static Object synObj = new Object();

    private float[] accelerometerValues = new float[3];
    private float magneticFieldValues[] = new float[3];
    private float GyroscopeValues[] = new float[3];

    private boolean UsedFusion;

    float interval;
    float fin_vel[] = new float[3];
    float init_vel[] = new float[3];
    float distance[] = new float[3];

     final int SensorDelay = SensorManager.SENSOR_DELAY_GAME; //SENSOR_DELAY_UI

    final  int[] SeriesColor = {
            Color.TRANSPARENT,
            Color.GREEN,
            Color.YELLOW,
            Color.RED,
            Color.CYAN,
            Color.WHITE,
            Color.MAGENTA
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    FusionTask fusiontask = new FusionTask();

    private Sensor msensor,asensor,gsensor;

    /**
     * Enable or disable the add data to series widgets
     *
     * @param enabled the enabled state
     */
    private void setSeriesWidgetsEnabled(boolean enabled) {
        if (D) {
            Log.i(TAG, "setSeriesWidgetsEnabled");
        }

    }

    public boolean requestPermission() {
        //判断当前Activity是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "please give me the permission", Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);

                if(D)Log.i(TAG, "ActivityCompat.requestPermissions");
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQ_CODE: {
                // 如果请求被拒绝，那么通常grantResults数组为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功，进行相应操作
                    // createFile("hello.txt");
                    //CreateFilePermissionFlag = true;
                    if(D)Log.i(TAG, "PackageManager.PERMISSION_GRANTED  ok");
                } else {
                    //申请失败，可以继续向用户解释。
                    //CreateFilePermissionFlag = false;
                    if(D)Log.i(TAG, "PackageManager.PERMISSION_GRANTED  refused");
                }
                return;
            }
        }
    }

    public static void showMessage(final Context act, final String msg) {
        showMessage(act, msg, Toast.LENGTH_SHORT);
    }

    public static void showMessage(final Context act, final String msg,
                                   final int len) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (synObj)
                        {
                            if (toast != null) {
                                toast.cancel();
                                //toast.setText(msg);
                                //toast.setDuration(len);
                                toast = Toast.makeText(act, msg, len);
                            } else {
                                toast = Toast.makeText(act, msg, len);
                            }
                            toast.show();
                        }
                    }
                });
            }
        }).start();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D) {
            Log.i(TAG, "onCreate");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xy_zozo_chart);

        // the top part of the UI components for adding new data points
        // mX = (EditText) findViewById(R.id.xValue);
        // mY = (EditText) findViewById(R.id.yValue);
        // mAdd = (Button) findViewById(R.id.add);

        mSeries = (EditText) findViewById(R.id.SeriesValue);
        mEnableButton = (Button) findViewById(R.id.enable_button);
        mDisableButton = (Button) findViewById(R.id.disable_button);
        mStopButton = (Button) findViewById(R.id.stop_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mYplusButton = (Button) findViewById(R.id.zoom_y_plus_button);
        mYdecButton = (Button) findViewById(R.id.zoom_y_dec_button);
        mXplusButton = (Button) findViewById(R.id.zoom_x_plus_button);
        mXdecButton = (Button) findViewById(R.id.zoom_x_dec_button);

        // set some properties on the main renderer
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));

        mRenderer.setXLabels(15);
        mRenderer.setYLabels(15);
        mRenderer.setShowGrid(true);
        mRenderer.setMargins(new int[]{20, 30, 15, 0});
        mRenderer.setZoomButtonsVisible(true);

        mRenderer.setPointSize(10);

        mRenderer.setChartTitle("zozo motion test");
        mRenderer.setXTitle("time");
        mRenderer.setYTitle("acc data");

        mRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        mRenderer.setAxisTitleTextSize(20);
        mRenderer.setChartTitleTextSize(25);
        mRenderer.setLabelsTextSize(15);
        mRenderer.setLegendTextSize(20);

        mRenderer.setXAxisMin(X_MIN);
        mRenderer.setXAxisMax(X_MAX);
        mRenderer.setYAxisMin(Y_MIN);
        mRenderer.setYAxisMax(Y_MAX);
        mRenderer.setAxesColor(Color.LTGRAY);
        mRenderer.setLabelsColor(Color.LTGRAY);

        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        // enable the chart click events
        mRenderer.setClickEnabled(true);
        mRenderer.setSelectableBuffer(10);

        mRenderer.setZoomInLimitX(100);
        mRenderer.setZoomInLimitY(5);
        //mRenderer.setZoomEnabled(true);
        //mRenderer.setPanEnabled(true);

        layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        //boolean enabled = mDataset.getSeriesCount() > 0;
        //setSeriesWidgetsEnabled(enabled);

        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //获得传感器信息
        List<Sensor> Sensorlist= sm.getSensorList(Sensor.TYPE_ALL);
        String text=" 你的手机总共拥有传感器的个数为："+Sensorlist.size() +" 个 \n\n";
        if(D) Log.i(TAG,text);
        for (Sensor sensor : Sensorlist) {
            Log.i("Sensor", sensor.getName()+sensor.getType());
            text=" 名称: "+sensor.getName()+"\n 能量: "+sensor.getPower()+"\n 分辨率: "+ sensor.getResolution()
            +"\n 厂商: "+sensor.getVendor()+"\n 版本: "+sensor.getVersion()+"\n 最小延时: "+sensor.getMinDelay()
            + "\n最大跨度: "+sensor.getMaximumRange()+"\n----------------------------------------------------\n";
            if(D) Log.i(TAG,text);
            if(sensor.getType() == 1)
            {
                asensor  = sensor;
            }else if(sensor.getType() == 2){
                msensor = sensor;
            }else if(sensor.getType() == 4){
                gsensor = sensor;
            }

        }


        mEnableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (D) Log.i(TAG, "mEnableButton onClick");
                int x = 0;
                try {
                    x = Integer.parseInt(mSeries.getText().toString());
                } catch (NumberFormatException e) {
                    mSeries.requestFocus();
                    return;
                }

                // add a new data point to the current series
                switch (x) {
                    case 1:
                        if(EnableSerise1) {
                            datarenderer1.setColor(SeriesColor[1]);
                        }else {
                            enableAccWave();
                        }
                        break;
                    case 2:
                        if(EnableSerise1) {
                            datarenderer2.setColor(SeriesColor[2]);
                        }else {
                            enableAccWave();
                        }
                        break;
                    case 3:{
                        if(EnableSerise1) {
                            datarenderer3.setColor(SeriesColor[3]);
                        }else {
                            enableAccWave();
                        }
                        break;
                    }
                    case 4:{
                        if(EnableSerise4) {
                            datarenderer4.setColor(SeriesColor[4]);
                        }else {
                            if(EnableSerise1)enableOrientationWave();
                        }
                    }
                    case 5:{
                        if(EnableSerise4) {
                            datarenderer5.setColor(SeriesColor[5]);
                        }else {
                            if(EnableSerise1)enableOrientationWave();
                        }
                    }
                    case 6:{
                        if(EnableSerise4) {
                            datarenderer6.setColor(SeriesColor[6]);
                        }else {
                            if(EnableSerise1)enableOrientationWave();
                        }
                        break;
                    }
                    case 8:{
                        UsedFusion = true;
                        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorDelay);
                        if(fusiontask.EnableMag)
                            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorDelay);
                        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorDelay);
                        magneticFieldValues = null;
                        accelerometerValues = null;
                        GyroscopeValues = null;
                        fusiontask.Fusion_task_init();

                        init_vel[0] = init_vel[1] = init_vel[2] = 0;

                        enableAccWave();
                        enableOrientationWave();
                        break;
                    }
                    default:
                        break;
                }

                mStopButton.setText(R.string.stop_button);
                mSaveButton.setEnabled(false);

                //
                mSeries.setText("");
                mSeries.requestFocus();
                // repaint the chart such as the newly added point to be visible
                mChartView.repaint();

            }
        });

        mDisableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (D) {
                    Log.i(TAG, "mEnableButton onClick");
                }
                int x = 0;

                try {
                    x = Integer.parseInt(mSeries.getText().toString());
                } catch (NumberFormatException e) {
                    mSeries.requestFocus();
                    return;
                }

                // add a new data point to the current series
                switch (x) {
                    case 1:{
                        datarenderer1.setColor(SeriesColor[0]);
                        break;
                    }
                    case 2:{
                        datarenderer2.setColor(SeriesColor[0]);
                        break;
                    }
                    case 3:{
                        datarenderer3.setColor(SeriesColor[0]);
                        break;
                    }
                    case 4:{
                        datarenderer4.setColor(SeriesColor[0]);
                        break;
                    }
                    case 5:{
                        datarenderer5.setColor(SeriesColor[0]);
                        break;
                    }
                    case 6:{
                        datarenderer6.setColor(SeriesColor[0]);
                        break;
                    }
                    case 8:{
                        UsedFusion = false;
                        sm.unregisterListener(myAccelerometerListener);
                        magneticFieldValues = null;
                        accelerometerValues = null;
                        GyroscopeValues = null;
                        break;
                    }
                    default:
                        break;
                }
                mSeries.setText("");
                mSeries.requestFocus();
                // repaint the chart such as the newly added point to be visible
                mChartView.repaint();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, mStopButton.getText().toString());
                if (mStopButton.getText().toString().equals(getString(R.string.stop_button))) {
                    //stopTimer();
                    sm.unregisterListener(myAccelerometerListener);
                    mStopButton.setText(R.string.start_button);
                    mSaveButton.setEnabled(true);
                } else if (mStopButton.getText().toString().equals("START")) {
                    if (EnableSerise1) {
                        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorDelay);
                    }
                    if (EnableSerise4) {
                        if(!UseOrientation)
                            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorDelay);
                        else
                        {
                            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorDelay);
                            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorDelay);
                            magneticFieldValues = null;
                            accelerometerValues = null;
                        }
                    }
                    mStopButton.setText(R.string.stop_button);
                    mSaveButton.setEnabled(false);
                }
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    sm.unregisterListener(myAccelerometerListener);
                    mStopButton.setText(R.string.start_button);

                // save
                if (requestPermission()) {

                    if(D)Log.i(TAG, "getSDPath" + FileStoreTools.getSDPath());
                    //
                    String str[] = new String[1000];
                    //Get the text file based on folder

                    File file = new File(FileStoreTools.getSDPath() + File.separator + "zozo_file");
                    if(D)Log.i(TAG, "file full = " + file + "file path=" + file.getAbsolutePath());

                    //x_index
                    String save_str;
                    for (double i = 0, j = 0; i < x_index; i++) {
                        double y;

                        save_str = "";

                        if(i > Serise1StartIndex){
                            save_str = saveStringFormat(xyseries1 , (int)i);
                            save_str = save_str + saveStringFormat(xyseries2 , (int)(i));
                            save_str = save_str + saveStringFormat(xyseries3 , (int)(i));

                        }
                        if(i > Serise2StartIndex)
                        {
                            save_str = save_str + saveStringFormat(xyseries4 , (int)(i-Serise2StartIndex));
                            save_str = save_str + saveStringFormat(xyseries5 , (int)(i-Serise2StartIndex));
                            save_str = save_str + saveStringFormat(xyseries6 , (int)(i-Serise2StartIndex));
                        }

                        if(D)Log.i(TAG, "save str="+ save_str);

                        FileStoreTools.saveFile(save_str, file.getPath(), "_motion_xy_char.txt");
                    }
                    if(D)Log.i(TAG, "saveFile has done");
                }
                if (mDataset != null) {
                    mDataset.clear();
                    mChartView.repaint();
                    xyseries1.clear();
                    xyseries2.clear();
                    xyseries3.clear();
                    xyseries4.clear();
                    xyseries5.clear();
                    xyseries6.clear();
                }
            }
        });

        mYplusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double y_max, y_min , delta;
                if (EnableSerise1 | EnableSerise4) {
                    y_max = mRenderer.getYAxisMax();
                    y_min = mRenderer.getYAxisMin();

                    delta =Math.abs(y_max - y_min);
                    if(D)Log.d(TAG, "YPlus delta = "+""+delta);
                    if(delta >=mRenderer.getZoomInLimitY())
                    {

                            y_max -= (delta*0.1/2);
                            y_min += (delta*0.1/2);

                        if (y_min < y_max) {
                            mRenderer.setYAxisMax(y_max);
                            mRenderer.setYAxisMin(y_min);
                            Y_MAX = y_max;
                            Y_MIN = y_min;
                        }
                        mChartView.repaint();
                        showMessage(MotionXYChartBuilder.this,"YPlus Y  axis= "+""+delta);

                    }

                }
            }
        });
        mYdecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double y_max, y_min , delta;
                if (EnableSerise1 | EnableSerise4) {
                    y_max = mRenderer.getYAxisMax();
                    y_min = mRenderer.getYAxisMin();

                    delta =Math.abs(y_max - y_min);
                    if(D)Log.d(TAG, "Ydec delta = "+""+delta);
                    //if(delta >=100)
                    {
                            y_max += (delta*0.1/2);
                            y_min -= (delta*0.1/2);

                        if (y_min < y_max) {
                            mRenderer.setYAxisMax(y_max);
                            mRenderer.setYAxisMin(y_min);
                            Y_MAX = y_max;
                            Y_MIN = y_min;
                        }
                        mChartView.repaint();
                        showMessage(MotionXYChartBuilder.this,"Ydec Y  axis= "+""+delta);
                    }

                }

            }
        });
        mXplusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double x_max, x_min,delta;
                if (EnableSerise1 | EnableSerise4) {
                    x_max = mRenderer.getXAxisMax();
                    x_min = mRenderer.getXAxisMin();

                    delta = Math.abs(Math.abs(x_max) - Math.abs(x_min));
                    if(D)Log.d(TAG, "Xplus delta = "+""+delta);
                    if(delta >=mRenderer.getZoomInLimitX())
                    {
                            x_max -= (delta*0.1/2);
                            x_min += (delta*0.1/2);

                        if (x_min < x_max) {
                            mRenderer.setXAxisMax(x_max);
                            mRenderer.setXAxisMin(x_min);
                            X_MAX = x_max;
                            X_MIN = x_min;
                        }
                        mChartView.repaint();
                    }
                    showMessage(MotionXYChartBuilder.this,"XPlus X axis= "+""+delta);

                }

            }
        });
        mXdecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double x_max, x_min,delta;
                if (EnableSerise1 | EnableSerise4) {
                    x_max = mRenderer.getXAxisMax();
                    x_min = mRenderer.getXAxisMin();

                    delta =Math.abs(Math.abs(x_max) - Math.abs(x_min));
                    if(D)Log.d(TAG, "Xdec delta = "+""+delta);
                    //if(delta >=100)
                    {

                        x_max += (delta*0.1/2);
                        x_min -= (delta*0.1/2);

                        if (x_min < x_max) {
                            mRenderer.setXAxisMax(x_max);
                            mRenderer.setXAxisMin(x_min);
                            X_MAX = x_max;
                            X_MIN = x_min;
                        }
                        mChartView.repaint();
                        showMessage(MotionXYChartBuilder.this,"Xdec X axis= "+""+delta);
                    }
                }

            }
        });

        if (mChartView != null) {
                mChartView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // handle the click event on the chart
                        SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                        if (seriesSelection == null) {
                            showMessage(MotionXYChartBuilder.this, "No chart element");
                        } else {
                            // display information of the clicked point
                            showMessage(
                                    MotionXYChartBuilder.this,
                                    "Chart element in series index " + seriesSelection.getSeriesIndex()
                                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                                    + seriesSelection.getValue());

                            if(D)Log.i(TAG, "Chart element in series index " + seriesSelection.getSeriesIndex()
                                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                                    + seriesSelection.getValue());
                        }
                    }
                });
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }//oncreate end

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (mChartView != null && mStopButton.getText().equals(getString(R.string.stop_button))){
            if (EnableSerise1) {
                sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorDelay);
            }
            if (EnableSerise4) {
                if(!UseOrientation)
                    sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorDelay);
                else
                {
                    sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorDelay);
                    sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorDelay);
                    magneticFieldValues = null;
                    accelerometerValues = null;
                }
            }
            mChartView.repaint();
        }
    }


    private void waveUpdataRoutine(int type ,float mx, float my, float mz) {

        if (EnableSerise1 && type == Sensor.TYPE_LINEAR_ACCELERATION) {
            xyseries1.add(x_index, mx);
            if (mx > Y_max_buf) {
                Y_max_buf = mx;
            } else if (mx < Y_min_buf) {
                Y_min_buf = mx;
            }

            xyseries2.add(x_index, my);
            if (my > Y_max_buf) {
                Y_max_buf = my;
            } else if (my < Y_min_buf) {
                Y_min_buf = my;
            }
            xyseries3.add(x_index, mz);
            if (mz > Y_max_buf) {
                Y_max_buf = mz;
            } else if (mz < Y_min_buf) {
                Y_min_buf = mz;
            }
        }

        if (EnableSerise4 && type == Sensor.TYPE_ORIENTATION) {
            xyseries4.add(x_index, mx);
            xyseries5.add(x_index, my);
            xyseries6.add(x_index, mz);
        }

/*        if (x_index * 1.2 > X_MAX) {
            X_MAX *= 1.2;//
            //X_MIN = X_MAX - 100;
            mRenderer.setXAxisMax(X_MAX);
        }*/
        if (x_index  >= X_MAX) {
            X_MAX += 300/4;//
            X_MIN += 300/4;
            mRenderer.setXAxisMax(X_MAX);
            mRenderer.setXAxisMin(X_MIN);
        }

        if(EnableSerise4 && type == Sensor.TYPE_ORIENTATION)
        {
            x_index += 1;
            mChartView.postInvalidate();
        }
        else if(!EnableSerise4)
        {
            x_index += 1;
            mChartView.postInvalidate();
        }

        //WaveUpdataThread tr = new WaveUpdataThread();
        //Thread thread = new Thread(tr);
        //thread.start();

    }
    class WaveUpdataThread implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // 使用postInvalidate可以直接在线程中更新界面
                mChartView.postInvalidate();
            }
        }
    }        //mChartView.postInvalidate();

    private void enableAccWave() {
        if (!InitSerise1) {
            InitSerise1 = true;
            EnableSerise1 = true;
            // acc_x
            datarenderer1 = new XYSeriesRenderer();
            datarenderer1.setDisplayChartValues(false);
            datarenderer1.setColor(SeriesColor[1]);
            datarenderer1.setPointStyle(PointStyle.POINT);
            mRenderer.addSeriesRenderer(datarenderer1);
            Serise1StartIndex = x_index;

            xyseries1 = new XYSeries("lacc_x");
            mDataset.addSeries(0, xyseries1);

            //acc y
            datarenderer2 = new XYSeriesRenderer();
            datarenderer2.setDisplayChartValues(false);
            datarenderer2.setColor(SeriesColor[2]);
            datarenderer2.setPointStyle(PointStyle.POINT);
            mRenderer.addSeriesRenderer(datarenderer2);

            xyseries2 = new XYSeries("lacc_y");
            mDataset.addSeries(1, xyseries2);

            //acc z
            datarenderer3 = new XYSeriesRenderer();
            datarenderer3.setDisplayChartValues(false);
            datarenderer3.setColor(SeriesColor[3]);
            datarenderer3.setPointStyle(PointStyle.POINT);
            mRenderer.addSeriesRenderer(datarenderer3);

            xyseries3 = new XYSeries("lacc_z");
            mDataset.addSeries(2, xyseries3);

        } else {

        }

        /*
        * 最常用的一个方法 注册事件
        * 参数1 ：SensorEventListener监听器
        * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
        * 参数3 ：模式 可选数据变化的刷新频率
        * */
        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorDelay);
    }
    private void enableOrientationWave() {
        if (!InitSerise2) {
            InitSerise2 = true;
            EnableSerise4 = true;
            // yaw
            datarenderer4 = new XYSeriesRenderer();
            datarenderer4.setDisplayChartValues(false);
            datarenderer4.setColor(SeriesColor[4]);
            datarenderer4.setPointStyle(PointStyle.POINT);
            mRenderer.addSeriesRenderer(datarenderer4);
            Serise2StartIndex = x_index;

            xyseries4 = new XYSeries("yaw");
            mDataset.addSeries(3, xyseries4);

            //pitch
            datarenderer5 = new XYSeriesRenderer();
            datarenderer5.setDisplayChartValues(false);
            datarenderer5.setColor(SeriesColor[5]);
            datarenderer5.setPointStyle(PointStyle.POINT);
            mRenderer.addSeriesRenderer(datarenderer5);
            xyseries5 = new XYSeries("pitch");
            mDataset.addSeries(4, xyseries5);

            // roll
            datarenderer6 = new XYSeriesRenderer();
            datarenderer6.setDisplayChartValues(false);
            datarenderer6.setColor(SeriesColor[6]);
            datarenderer6.setPointStyle(PointStyle.POINT);
            mRenderer.addSeriesRenderer(datarenderer6);

            xyseries6 = new XYSeries("roll");
            mDataset.addSeries(5, xyseries6);

        } else {
            if (!EnableSerise4) {
                mDataset.addSeries(3, xyseries4);
                mDataset.addSeries(4, xyseries5);
                mDataset.addSeries(5, xyseries6);
                EnableSerise4 = true;
            }
        }

        /*
        * 最常用的一个方法 注册事件
        * 参数1 ：SensorEventListener监听器
        * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
        * 参数3 ：模式 可选数据变化的刷新频率
        * */
        if(!UseOrientation)
            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorDelay);
        else
        {
            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorDelay);
            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorDelay);
            magneticFieldValues = null;
            accelerometerValues = null;
        }
    }
    private void disableAccWave(){
        if (EnableSerise1) {
            datarenderer1.setColor(SeriesColor[0]);
            datarenderer2.setColor(SeriesColor[0]);
            datarenderer3.setColor(SeriesColor[0]);
        }
    }
    private void disableOrientationWave(){
        if (EnableSerise4) {
            datarenderer4.setColor(SeriesColor[0]);
            datarenderer5.setColor(SeriesColor[0]);
            datarenderer6.setColor(SeriesColor[0]);
        }
    }
    private String saveStringFormat(XYSeries series,int index){
        double y;
        y = series.getY( index);
        String str ="" + y;
        str = ZozoXYChartBuilder.formatStr(str, 7) + ", ";
        if(D)Log.i(TAG, "xyseries str=" + str );
        return str;
    }
    /*
     * SensorEventListener接口的实现，需要实现两个方法
     * 方法1 onSensorChanged 当数据变化的时候被触发调用
     * 方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
     * */
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp,dT,tg,ta,tm;
    private final float EPSILON = 2.22E-16F;

    final SensorEventListener myAccelerometerListener = new SensorEventListener() {

        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent) {
            //if(M)Log.i(TAG, "onSensorChanged");
/*            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
*//*                if(D)Log.i(TAG, "TYPE_ACCELEROMETER");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];


                if(D)Log.i(TAG, "\n accel_x " + X_lateral);
                if(D)Log.i(TAG, "\n accel_y " + Y_longitudinal);
                if(D)Log.i(TAG, "\n accel_z " + Z_vertical);*//*
            }*/
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
/*                if(D)Log.i(TAG, "TYPE_GRAVITY");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];

                waveUpdataRoutine(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                if(D)Log.i(TAG, "\n gaccel_x " + X_lateral);
                if(D)Log.i(TAG, "\n gaccel_y " + Y_longitudinal);
                if(D)Log.i(TAG, "\n gaccel_z " + Z_vertical);*/
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
/*                Log.i(TAG, "TYPE_LINEAR_ACCELERATION");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];


                Log.i(TAG, "\n Laccel_x " + X_lateral);
                Log.i(TAG, "\n Laccel_y " + Y_longitudinal);
                Log.i(TAG, "\n Laccel_z " + Z_vertical);*/
                //if(M)Log.d(TAG, "TYPE_LINEAR_ACCELERATION");
                //waveUpdataRoutine(Sensor.TYPE_LINEAR_ACCELERATION,sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
/*                Log.i(TAG, "TYPE_ORIENTATION");

                //图解中已经解释三个值的含义
                float X_heading = sensorEvent.values[0];
                float Y_pitch = sensorEvent.values[1];
                float Z_roll = sensorEvent.values[2];


                Log.i(TAG, "\n heading " + X_heading);
                Log.i(TAG, "\n pitch " + Y_pitch);
                Log.i(TAG, "\n roll " + Z_roll);*/
                //if(M)Log.d(TAG, "TYPE_ORIENTATION");
                //waveUpdataRoutine(Sensor.TYPE_ORIENTATION,sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                accelerometerValues = sensorEvent.values;
                if(M)Log.d(TAG, "test2 --"+"TYPE_ACCELEROMETER=" + (sensorEvent.timestamp - ta));
                ta = sensorEvent.timestamp;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = sensorEvent.values;
                if(false)Log.d(TAG, "TYPE_MAGNETIC_FIELD = " + (sensorEvent.timestamp - tm));
                tm = sensorEvent.timestamp;
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            {
                GyroscopeValues = sensorEvent.values;
                if(M)Log.d(TAG, "test2 --"+"TYPE_GYROSCOPE = " + (sensorEvent.timestamp - tg));
                tg = sensorEvent.timestamp;

            }

            if(UseOrientation)
            {
                if(accelerometerValues != null && magneticFieldValues != null)
                {
                    float[] values = new float[3];
                    float[] R = new float[9];
                    SensorManager.getRotationMatrix(R, null, accelerometerValues,
                            magneticFieldValues);
                    values = SensorManager.getOrientation(R, values);
                    if(M)Log.d(TAG, "getOrientation");
                    waveUpdataRoutine(Sensor.TYPE_ORIENTATION,values[0], values[1], values[2]);
                    accelerometerValues = null;
                    magneticFieldValues = null;
                }



            }
            if(UsedFusion)
            {
                if(fusiontask.EnableMag){
                    if(accelerometerValues != null && magneticFieldValues != null
                            && GyroscopeValues != null){
                        String str1 = "accel = "+accelerometerValues[0] + "=="+accelerometerValues[1]
                                + "=="+accelerometerValues[2] + "\n";

                        String str2 = "mag = "+magneticFieldValues[0] + "=="+magneticFieldValues[1]
                                + "2="+magneticFieldValues[2] + "\n";
                        String str3 ;

                        if(fusiontask.Fusion_Task(accelerometerValues,magneticFieldValues,GyroscopeValues))
                        {
                            if(M)Log.d(TAG, "test3 --"+"Fusion_Task = " + (sensorEvent.timestamp - timestamp));
                            timestamp = sensorEvent.timestamp;
                            if(M) Log.d(TAG, str1 + str2);
                            str1 = "test3 --"+"liner accel = "+fusiontask.thisSV_9DOF_GBY_KALMAN.fAccGl[0] + "=="+fusiontask.thisSV_9DOF_GBY_KALMAN.fAccGl[1]
                                    + "=="+fusiontask.thisSV_9DOF_GBY_KALMAN.fAccGl[2] + "\n";
                            str2 = "test3 --"+"roll = "+fusiontask.thisSV_9DOF_GBY_KALMAN.fPhiPl + "pitch="+fusiontask.thisSV_9DOF_GBY_KALMAN.fThePl
                                    + "yaw="+fusiontask.thisSV_9DOF_GBY_KALMAN.fPsiPl+"compass="+fusiontask.thisSV_9DOF_GBY_KALMAN.fRhoPl+ "\n";

                            if(M) Log.d(TAG, str1 + str2);
                        }
                        magneticFieldValues = null;
                        accelerometerValues = null;
                        GyroscopeValues = null;
                    }
                }else {
                    if(accelerometerValues != null && GyroscopeValues != null)
                    {
/*                        String str1 = "accel = "+accelerometerValues[0] + "=="+accelerometerValues[1]
                                + "=="+accelerometerValues[2] + "\n";

                        String str2 ;
                        String str3 = "Gyro = "+GyroscopeValues[0] + "=="+GyroscopeValues[1]
                                + "=="+GyroscopeValues[2] + "\n";*/
/*                        float[] Q = new float[9];
                        float[] AG = new float[3];
                        // This timestep's delta rotation to be multiplied by the current rotation
                        // after computing it from the gyro sample data.
                        if (timestamp != 0) {
                            final float dT = (sensorEvent.timestamp - timestamp) * NS2S;
                            // Axis of the rotation sample, not normalized yet.
                            float axisX = GyroscopeValues[0];
                            float axisY = GyroscopeValues[1];
                            float axisZ = GyroscopeValues[2];
                            // Calculate the angular speed of the sample
                            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
                            // Normalize the rotation vector if it's big enough to get the axis
                            if (omegaMagnitude > EPSILON) {
                                axisX /= omegaMagnitude;
                                axisY /= omegaMagnitude;
                                axisZ /= omegaMagnitude;
                            }
                            // Integrate around this axis with the angular speed by the timestep
                            // in order to get a delta rotation from this sample over the timestep
                            // We will convert this axis-angle representation of the delta rotation
                            // into a quaternion before turning it into the rotation matrix.
                            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                            deltaRotationVector[0] = sinThetaOverTwo * axisX;
                            deltaRotationVector[1] = sinThetaOverTwo * axisY;
                            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                            deltaRotationVector[3] = cosThetaOverTwo;
                        }
                        timestamp = sensorEvent.timestamp;
                        //float[] deltaRotationMatrix = new float[9];
                        SensorManager.getRotationMatrixFromVector(Q,GyroscopeValues);

                        //str1 = "Q0="+Q[0]+"Q1="+Q[1]+"Q2="+Q[2]+"Q3="+Q[3]+"\n";
                        AG[0] = Q[0] * accelerometerValues[0] + Q[3] * accelerometerValues[1] + Q[6] * accelerometerValues[2];
                        AG[1] = Q[1] * accelerometerValues[0] + Q[4] * accelerometerValues[1] + Q[7] * accelerometerValues[2];
                        AG[2] = Q[2] * accelerometerValues[0] + Q[5] * accelerometerValues[1] + Q[8] * accelerometerValues[2];
                        str2 = "AG0="+ AG[0]+"AG1="+ AG[1]+"AG2="+ AG[2] + "\n";*/
                        //if(M) Log.d(TAG, str3 + str2);//str1

/*                        for(int i=0;i < accelerometerValues.length;i++)
                        {
                            accelerometerValues[i] = accelerometerValues[i] / SensorManager.STANDARD_GRAVITY;
                        }
*//*                        accelerometerValues[0] = 1;
                        accelerometerValues[1] = 0;
                        accelerometerValues[2] = 0;*//*
                                str3 = "Gyro = "+GyroscopeValues[0] + "=="+GyroscopeValues[1]
                                + "=="+GyroscopeValues[2] + "\n";
                        //if(M) Log.d(TAG, str3);//str1
                        for(int i=0;i < GyroscopeValues.length;i++)
                        {
                            GyroscopeValues[i] = (float) Math.toDegrees(GyroscopeValues[i]);
                            //GyroscopeValues[i] = 0;
                        }
                        str1 = "accel = "+accelerometerValues[0] + "=="+accelerometerValues[1]
                                + "=="+accelerometerValues[2] + "\n";


                         str3 = "Gyro = "+GyroscopeValues[0] + "=="+GyroscopeValues[1]
                                + "=="+GyroscopeValues[2] + "\n";*/
                        //if(M) Log.d(TAG, str3);//str1
                        if(fusiontask.Fusion_Task(accelerometerValues,magneticFieldValues,GyroscopeValues))
                        {
                            if(M)Log.d(TAG, "test2 --"+"Fusion_Task = " + (sensorEvent.timestamp - timestamp));
                                timestamp = sensorEvent.timestamp;
                            //if(M) Log.d(TAG, str1 + str3);//str1

                            //interval=timestamp/(1E9F); 	//nanosecond to seconds
                            interval=1.677E-2F; 	//nanosecond to seconds

                            for(int i=0;i<3;i++)
                            {
                                fin_vel[i] = init_vel[i] + (fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[i] * sm.GRAVITY_EARTH *interval);
                                distance[i] += (init_vel[i]*interval)+0.5f*fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[i]* sm.GRAVITY_EARTH *interval*interval;
                                init_vel[i] = fin_vel[i];
                            }

                            waveUpdataRoutine(Sensor.TYPE_LINEAR_ACCELERATION,fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[0],
                                    fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[1], fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[2]);

/*                            waveUpdataRoutine(Sensor.TYPE_ORIENTATION,fusiontask.thisSV_6DOF_GY_KALMAN.fPhiPl/100,
                                    fusiontask.thisSV_6DOF_GY_KALMAN.fThePl/100,fusiontask.thisSV_6DOF_GY_KALMAN.fPsiPl/100);*/
                            waveUpdataRoutine(Sensor.TYPE_ORIENTATION,fin_vel[0],fin_vel[1],fin_vel[2]);

/*                            waveUpdataRoutine(Sensor.TYPE_ORIENTATION,fusiontask.thisAccel.fGsAvg[0],fusiontask.thisAccel.fGsAvg[1],
                                    fusiontask.thisAccel.fGsAvg[2]);*/


                            String str1 = "liner accel = "+fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[0] + "=="+fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[1]
                                    + "=="+fusiontask.thisSV_6DOF_GY_KALMAN.fAccGl[2] + "\n";
                            String str2 = "roll = "+fusiontask.thisSV_6DOF_GY_KALMAN.fPhiPl + "pitch="+fusiontask.thisSV_6DOF_GY_KALMAN.fThePl
                                    + "yaw="+fusiontask.thisSV_6DOF_GY_KALMAN.fPsiPl+"compass="+fusiontask.thisSV_6DOF_GY_KALMAN.fRhoPl+ "\n";

                            if(false) Log.d(TAG, str1 + str2);
                        }
                        accelerometerValues = null;
                        GyroscopeValues = null;

                    }
                }


            }

        }

        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if(D)Log.i(TAG, "onAccuracyChanged");
        }
    };

    @Override
    public void onPause() {
        if(D)Log.i(TAG, "onPause");
        /*
          * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
          * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
          * */
        sm.unregisterListener(myAccelerometerListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        System.out.println("destory");
        super.onDestroy();
    }

}