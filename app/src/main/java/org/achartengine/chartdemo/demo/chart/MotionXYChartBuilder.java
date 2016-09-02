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
package org.achartengine.chartdemo.demo.chart;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.R;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.achartmotion.FileStoreTools;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

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
    private XYSeries mCurrentSeries;
    /**
     * The most recently created renderer, customizing the current series.
     */
    private XYSeriesRenderer mCurrentRenderer;
/*  *//** Button for creating a new series of data. *//*
  private Button mNewSeries;*/
    /**
     * Button for adding entered data to the current series.
     */
    private Button mEnableButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mDisableButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mStopButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mYplusButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mYdecButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mXplusButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mXdecButton;
    /**
     * Button for adding entered data to the current series.
     */
    private Button mClearButton;
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

    final boolean D = true;
    private static final String TAG = "MotionXYChartBuilder";

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;

    int ptr = 0;
    double x_index = 0;
    double X_MIN = 0;
    double X_MAX = 100;
    double Y_MIN = -3;
    double Y_MAX = 3;
    float Y_min_buf = -3, Y_max_buf = 3;

    boolean EnableSerise1 = false, EnableSerise2 = false, EnableSerise3 = false, InitSerise1 = false, InitSerise2 = false, InitSerise3 = false;
    double Serise1StartIndex,Serise2StartIndex,Serise3StartIndex;
    ;
    private XYSeries xyseries1, xyseries2, xyseries3;//数据
    private XYSeriesRenderer datarenderer1, datarenderer2, datarenderer3;
    private Timer mTimer;
    private TimerTask mTask;

    //motion
    private SensorManager sm;
    int AsensorType;
    int MsensorType;
    int GsensorType;
    int LsensorType;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
        //mRenderer.setZoomEnabled(true);
        //mRenderer.setPanEnabled(true);

        layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        //boolean enabled = mDataset.getSeriesCount() > 0;
        //setSeriesWidgetsEnabled(enabled);

        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //选取加速度感应器
        //AsensorType = Sensor.TYPE_ACCELEROMETER;
        //MsensorType = Sensor.TYPE_ORIENTATION;
        //GsensorType = Sensor.TYPE_GRAVITY;
        LsensorType = Sensor.TYPE_LINEAR_ACCELERATION;

    /*
     * 最常用的一个方法 注册事件
     * 参数1 ：SensorEventListener监听器
     * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
     * 参数3 ：模式 可选数据变化的刷新频率
     * */
//        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(AsensorType), SensorManager.SENSOR_DELAY_NORMAL);
//        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(MsensorType), SensorManager.SENSOR_DELAY_NORMAL);
//        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(GsensorType), SensorManager.SENSOR_DELAY_NORMAL);
//        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(LsensorType), SensorManager.SENSOR_DELAY_NORMAL);


        mEnableButton.setOnClickListener(new View.OnClickListener() {
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
                    case 1:
                        //Log.i(TAG, "getSeries" + mDataset.getSeries());
                        if (!InitSerise1) {
                            InitSerise1 = true;
                            EnableSerise1 = true;
                            datarenderer1 = new XYSeriesRenderer();
                            datarenderer1.setDisplayChartValues(false);
                            datarenderer1.setColor(Color.GREEN);
                            datarenderer1.setPointStyle(PointStyle.POINT);
                            mRenderer.addSeriesRenderer(datarenderer1);
                            Serise1StartIndex = x_index;

                            xyseries1 = new XYSeries("lacc_x");
                            mDataset.addSeries(0, xyseries1);
                        } else {
                            if (!EnableSerise1) {
                                mDataset.addSeries(0, xyseries1);
                                EnableSerise1 = true;
                            }
                        }
                        break;
                    case 2:
                        //Log.i(TAG, "getSeries" + mDataset.getSeries().toString());
                        if (!InitSerise2) {
                            InitSerise2 = true;
                            EnableSerise2 = true;
                            datarenderer2 = new XYSeriesRenderer();
                            datarenderer2.setDisplayChartValues(false);
                            datarenderer2.setColor(Color.YELLOW);
                            datarenderer2.setPointStyle(PointStyle.POINT);
                            mRenderer.addSeriesRenderer(datarenderer2);
                            Serise2StartIndex = x_index;

                            xyseries2 = new XYSeries("lacc_y");
                            mDataset.addSeries(1, xyseries2);

                        } else {
                            if (!EnableSerise2) {
                                mDataset.addSeries(1, xyseries2);
                                EnableSerise2 = true;
                            }
                        }
                        break;
                    case 3:
                        //Log.i(TAG, "getSeries" + mDataset.getSeries());
                        if (!InitSerise3) {
                            InitSerise3 = true;
                            EnableSerise3 = true;
                            datarenderer3 = new XYSeriesRenderer();
                            datarenderer3.setDisplayChartValues(false);
                            datarenderer3.setColor(Color.RED);
                            datarenderer3.setPointStyle(PointStyle.POINT);
                            mRenderer.addSeriesRenderer(datarenderer3);
                            Serise3StartIndex = x_index;

                            xyseries3 = new XYSeries("lacc_z");
                            mDataset.addSeries(2, xyseries3);

                        } else {
                            if (!EnableSerise3) {
                                mDataset.addSeries(2, xyseries3);
                                EnableSerise3 = true;
                            }
                        }
                        break;
                    default:
                        break;
                }

                mStopButton.setText("STOP");

/*            // time
            startTimer();*/

                //
                mSeries.setText("");
                mSeries.requestFocus();
                // repaint the chart such as the newly added point to be visible
                mChartView.repaint();

            /*
             * 最常用的一个方法 注册事件
             * 参数1 ：SensorEventListener监听器
             * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
             * 参数3 ：模式 可选数据变化的刷新频率
             * */
                sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
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
                    case 1:
                        if (EnableSerise1) {
                            EnableSerise1 = false;
                            mDataset.removeSeries(xyseries1);
                        }
                        break;
                    case 2:
                        if (EnableSerise2) {
                            EnableSerise2 = false;
                            mDataset.removeSeries(xyseries2);
                        }
                        break;
                    case 3:
                        if (EnableSerise3) {
                            EnableSerise3 = false;
                            mDataset.removeSeries(xyseries3);
                        }
                        break;
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
                if (mStopButton.getText().toString().equals("STOP")) {
                    //stopTimer();
                    sm.unregisterListener(myAccelerometerListener);
                    mStopButton.setText("START");
                } else if (mStopButton.getText().toString().equals("START")) {
                    if (EnableSerise1 | EnableSerise2 | EnableSerise3) {
                        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
                    }
                    mStopButton.setText("STOP");
                }
            }
        });

/*      mClearButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              //if(mStopButton.getText().toString().equals("STOP"))
              {
                  sm.unregisterListener(myAccelerometerListener);
                  mStopButton.setText("START");
              }
              if(mDataset != null)
              {
                  mDataset.clear();
                  mChartView.repaint();
              }
          }
      });*/
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //if(mStopButton.getText().toString().equals("STOP"))
                {
                    sm.unregisterListener(myAccelerometerListener);
                    mStopButton.setText("START");
                }

                // save
                if (requestPermission()) {

                    if(D)Log.i(TAG, "getSDPath" + FileStoreTools.getSDPath());
                    //
                    String str[] = new String[1000];
                    //Get the text file based on folder

                    File file = new File(FileStoreTools.getSDPath() + File.separator + "zozo_file");
                    if(D)Log.i(TAG, "file full = " + file + "file path=" + file.getAbsolutePath());

                    //x_index
                    String str_buf, save_str;
                    for (double i = 0, j = 0; i < x_index; i++) {
                        double y;

                        str_buf = "";
                        save_str = "";

                        if(i > Serise1StartIndex){
                            y = xyseries1.getY((int)i);
                            str_buf = "" + y;
                            str_buf = ZozoXYChartBuilder.formatStr(str_buf, 7) + ", ";
                            if(D)Log.i(TAG, "xyseries1 str=" + str_buf );
                            save_str = str_buf;
                        }
                        if(i > Serise2StartIndex){
                            y = xyseries2.getY((int)(i-Serise2StartIndex));
                            str_buf = "" + y;
                            str_buf = ZozoXYChartBuilder.formatStr(str_buf, 7) + ", ";
                            if(D)Log.i(TAG, "xyseries2 str=" + str_buf );
                            save_str = save_str + str_buf;
                        }
                        if(i > Serise3StartIndex){
                            y = xyseries3.getY((int)(i-Serise3StartIndex));
                            str_buf = "" + y;
                            str_buf = ZozoXYChartBuilder.formatStr(str_buf, 7) + ", ";
                            if(D)Log.i(TAG, "xyseries3 str=" + str_buf );
                            save_str = save_str + str_buf;
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
                }
            }
        });

        mYplusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double y_max, y_min;
                if (EnableSerise1 | EnableSerise2 | EnableSerise3) {
                    y_max = mRenderer.getYAxisMax();
                    y_min = mRenderer.getYAxisMin();
                    if (y_max > 0) {
                        y_max /= 1.1;
                    } else {
                        y_max *= 1.1;
                    }

                    if (y_min > 0) {
                        y_min *= 1.1;
                    } else {
                        y_min /= 1.1;
                    }
                    if (y_min < y_max) {
                        mRenderer.setYAxisMax(y_max);
                        mRenderer.setYAxisMin(y_min);
                    }
                    mChartView.repaint();
                }
            }
        });
        mYdecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double y_max, y_min;
                if (EnableSerise1 | EnableSerise2 | EnableSerise3) {
                    y_max = mRenderer.getYAxisMax();
                    y_min = mRenderer.getYAxisMin();

                    if (y_max > 0) {
                        y_max *= 1.1;
                    } else {
                        y_max /= 1.1;
                    }

                    if (y_min > 0) {
                        y_min /= 1.1;
                    } else {
                        y_min *= 1.1;
                    }
                    if (y_min < y_max) {
                        mRenderer.setYAxisMax(y_max);
                        mRenderer.setYAxisMin(y_min);
                    }

                    mChartView.repaint();
                }

            }
        });
        mXplusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double x_max, x_min;
                if (EnableSerise1 | EnableSerise2 | EnableSerise3) {
                    x_max = mRenderer.getXAxisMax();
                    x_min = mRenderer.getXAxisMin();

                    if (x_max > 0) {
                        x_max /= 1.1;
                    } else {
                        x_max *= 1.1;
                    }

                    if (x_min > 0) {
                        x_min *= 1.1;

                    } else {
                        x_min /= 1.1;
                        mRenderer.setYAxisMin(x_min);
                    }
                    if (x_min < x_max) {
                        mRenderer.setXAxisMax(x_max);
                        mRenderer.setXAxisMin(x_min);
                    }

                    mChartView.repaint();
                }

            }
        });
        mXdecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double x_max, x_min;
                if (EnableSerise1 | EnableSerise2 | EnableSerise3) {
                    x_max = mRenderer.getXAxisMax();
                    x_min = mRenderer.getXAxisMin();

                    if (x_max > 0) {
                        x_max *= 1.1;
                    } else {
                        x_max /= 1.1;
                    }


                    if (x_min > 0) {
                        x_min /= 1.1;
                    } else {
                        x_min *= 1.1;
                    }
                    if (x_min < x_max) {
                        mRenderer.setXAxisMax(x_max);
                        mRenderer.setXAxisMin(x_min);
                    }

                    mChartView.repaint();
                }

            }
        });

        if (mChartView != null) {
                mChartView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // handle the click event on the chart
                        SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                        if (seriesSelection == null) {
                            Toast.makeText(MotionXYChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
                        } else {
                            // display information of the clicked point
                            Toast.makeText(
                                    MotionXYChartBuilder.this,
                                    "Chart element in series index " + seriesSelection.getSeriesIndex()
                                            + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                                            + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                                            + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();

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
        if (mChartView == null) {
/*      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
      // enable the chart click events
      mRenderer.setClickEnabled(true);
      mRenderer.setSelectableBuffer(10);*/

            mChartView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // handle the click event on the chart
                    SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                    if (seriesSelection == null) {
                        Toast.makeText(MotionXYChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
                    } else {
                        // display information of the clicked point
                        Toast.makeText(
                                MotionXYChartBuilder.this,
                                "Chart element in series index " + seriesSelection.getSeriesIndex()
                                        + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                                        + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                                        + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();

                        if(D)Log.i(TAG, "Chart element in series index " + seriesSelection.getSeriesIndex()
                                + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                                + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                                + seriesSelection.getValue());
                    }
                }
            });
/*      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      boolean enabled = mDataset.getSeriesCount() > 0;
      setSeriesWidgetsEnabled(enabled);*/
        } else {
            if (EnableSerise1 | EnableSerise2 | EnableSerise3) {
                sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
            }
            mChartView.repaint();
        }
    }


    private void waveUpdataRoutine(float mx, float my, float mz) {

        if (EnableSerise1) {
            xyseries1.add(x_index, mx);
            if (mx > Y_max_buf) {
                Y_max_buf = mx;
            } else if (mx < Y_min_buf) {
                Y_min_buf = mx;
            }

        }
        if (EnableSerise2) {
            xyseries2.add(x_index, my);
            if (my > Y_max_buf) {
                Y_max_buf = my;
            } else if (my < Y_min_buf) {
                Y_min_buf = my;
            }
        }
        if (EnableSerise3) {
            xyseries3.add(x_index, mz);
            if (mz > Y_max_buf) {
                Y_max_buf = mz;
            } else if (mz < Y_min_buf) {
                Y_min_buf = mz;
            }
        }

/*        if(Y_min_buf > Y_MAX)
        {
            Y_MAX = Y_min_buf * 1.1;
            mRenderer.setYAxisMax(Y_MAX);
        }
        else if(Y_min_buf < Y_MIN)
        {
            if(Y_min_buf<0)
                Y_MIN = Y_min_buf * 1.1;
            mRenderer.setYAxisMin(Y_MIN);
        }*/
        //X_MAX = mRenderer.getXAxisMax();
        if (x_index * 1.2 > X_MAX) {
            X_MAX *= 1.2;//
            //X_MIN = X_MAX - 100;
            mRenderer.setXAxisMax(X_MAX);
        }
        x_index += 1;
        mChartView.postInvalidate();
    }


/*    private void startTimer()
    {
        if(mTimer == null)
        {
            //1.creator timer
            mTimer = new Timer();

            //2.creator task
            if(mTask == null)
            {
                mTask = new TimerTask() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        waveUpdataRoutine();
                    }
                };
            }
            //3.start timer
                mTimer.schedule(mTask, 2000, 500);
        }

    }*/


/*    private void stopTimer()
    {
        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
        if(mTask != null)
        {
            mTask.cancel();
            mTask = null;
        }
    }*/

    /*
     * SensorEventListener接口的实现，需要实现两个方法
     * 方法1 onSensorChanged 当数据变化的时候被触发调用
     * 方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
     * */
    final SensorEventListener myAccelerometerListener = new SensorEventListener() {

        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(D)Log.i(TAG, "onSensorChanged");
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if(D)Log.i(TAG, "TYPE_ACCELEROMETER");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];


                if(D)Log.i(TAG, "\n accel_x " + X_lateral);
                if(D)Log.i(TAG, "\n accel_y " + Y_longitudinal);
                if(D)Log.i(TAG, "\n accel_z " + Z_vertical);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if(D)Log.i(TAG, "TYPE_GRAVITY");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];

                waveUpdataRoutine(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                if(D)Log.i(TAG, "\n gaccel_x " + X_lateral);
                if(D)Log.i(TAG, "\n gaccel_y " + Y_longitudinal);
                if(D)Log.i(TAG, "\n gaccel_z " + Z_vertical);
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

                waveUpdataRoutine(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                Log.i(TAG, "TYPE_ORIENTATION");

                //图解中已经解释三个值的含义
                float X_heading = sensorEvent.values[0];
                float Y_pitch = sensorEvent.values[1];
                float Z_roll = sensorEvent.values[2];


                Log.i(TAG, "\n heading " + X_heading);
                Log.i(TAG, "\n pitch " + Y_pitch);
                Log.i(TAG, "\n roll " + Z_roll);
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