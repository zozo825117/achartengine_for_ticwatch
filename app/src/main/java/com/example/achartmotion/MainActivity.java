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
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

 /** The main dataset that includes all the series that go into a chart. */
  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
  /** The main renderer that includes all the renderers customizing a chart. */
  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  /** The most recently added series. */
  private XYSeries mCurrentSeries;
  /** The most recently created renderer, customizing the current series. */
  private XYSeriesRenderer mCurrentRenderer;
/*  *//** Button for creating a new series of data. *//*
  private Button mNewSeries;*/
  /** Button for adding entered data to the current series. */
  private Button mEnableButton;
    /** Button for adding entered data to the current series. */
    private Button mDisableButton;
    /** Button for adding entered data to the current series. */
    private Button mStopButton;
    /** Button for adding entered data to the current series. */
    private Button mClearButton;
    /* Edit text field for entering the Y value of the data to be added. */
    private EditText mSeries;

  /** The chart view that displays the data. */
  private GraphicalView mChartView;

  final boolean D = true;
  private static final String TAG = "MainActivity";

  int SineWaveSim[] = {
          0x31,0x27,0x67,0x48,0x9c,0x5e,0x66,0x66,0x9b,0x5e,0x69,0x48,0x2f,0x27,0x01,0x00,
          0xce,0xd8,0x9b,0xb7,0x61,0xa1,0x9d,0x99,0x63,0xa1,0x98,0xb7,0xd1,0xd8,0xfe,0xff,
          0x32,0x27,0x68,0x48,0x9b,0x5e,0x66,0x66,0x9c,0x5e,0x67,0x48,0x31,0x27,0x00,0x00,
          0xd0,0xd8,0x9a,0xb7,0x60,0xa1,0x9f,0x99,0x60,0xa1,0x9c,0xb7,0xcd,0xd8,0x01,0x00,
          0x2f,0x27,0x6b,0x48,0x98,0x5e,0x67,0x66,0x9c,0x5e,0x66,0x48,0x33,0x27,0xfe,0xff,
          0xd1,0xd8,0x96,0xb7,0x68,0xa1,0x95,0x99,0x6b,0xa1,0x93,0xb7,0xd1,0xd8,0x02,0x00,
          0x2d,0x27,0x6a,0x48,0x9c,0x5e,0x62,0x66,0xa1,0x5e,0x65,0x48,0x2f,0x27,0x03,0x00,
          0xcd,0xd8,0x9a,0xb7,0x64,0xa1,0x9a,0x99,0x65,0xa1,0x98,0xb7,0xd0,0xd8,0xfe,0xff,
          0x33,0x27,0x66,0x48,0x9c,0x5e,0x69,0x66,0x94,0x5e,0x71,0x48,0x28,0x27,0x05,0x00,
          0xcf,0xd8,0x98,0xb7,0x63,0xa1,0x9d,0x99,0x62,0xa1,0x99,0xb7,0xd2,0xd8,0xfd,0xff,
          0x31,0x27,0x69,0x48,0x9a,0x5e,0x68,0x66,0x99,0x5e,0x6b,0x48,0x2c,0x27,0x04,0x00,
          0xcd,0xd8,0x99,0xb7,0x65,0xa1,0x99,0x99,0x66,0xa1,0x97,0xb7,0xd1,0xd8,0xff,0xff,
          0x30,0x27,0x6a,0x48,0x98,0x5e,0x6a,0x66,0x98,0x5e,0x6a,0x48,0x2f,0x27,0x01,0x00,
          0xd0,0xd8,0x96,0xb7,0x68,0xa1,0x96,0x99,0x68,0xa1,0x97,0xb7,0xcf,0xd8,0x02,0x00,
          0x2e,0x27,0x69,0x48,0x9c,0x5e,0x63,0x66,0xa0,0x5e,0x65,0x48,0x31,0x27,0x00,0x00,
          0xcf,0xd8,0x99,0xb7,0x64,0xa1,0x99,0x99,0x68,0xa1,0x94,0xb7,0xd3,0xd8,0xff,0xff,
          0x2e,0x27,0x6b,0x48,0x9a,0x5e,0x66,0x66,0x9c,0x5e,0x67,0x48,0x31,0x27,0x00,0x00,
          0xd0,0xd8,0x97,0xb7,0x66,0xa1,0x99,0x99,0x65,0xa1,0x99,0xb7,0xcf,0xd8,0xff,0xff,
          0x34,0x27,0x62,0x48,0xa2,0x5e,0x61,0x66,0x9e,0x5e,0x66,0x48,0x33,0x27,0xfb,0xff,
          0xd8,0xd8,0x8e,0xb7,0x6d,0xa1,0x94,0x99,0x68,0xa1,0x96,0xb7,0xd2,0xd8,0xff,0xff,
          0x30,0x27,0x69,0x48,0x98,0x5e,0x6b,0x66,0x97,0x5e,0x6c,0x48,0x2c,0x27,0x03,0x00,
          0xce,0xd8,0x9a,0xb7,0x63,0xa1,0x9b,0x99,0x64,0xa1,0x98,0xb7,0xd1,0xd8,0xff,0xff
  };

    int ptr = 0;
    double x_index = 0;
    double X_MIN = 0;
    double X_MAX = 100;
    double Y_MIN = -10;
    double Y_MAX = 10;
    boolean EnableSerise1 = false,EnableSerise2 = false,EnableSerise3 = false,InitSerise1 = false,InitSerise2 = false,InitSerise3 = false;;
    private XYSeries xyseries1,xyseries2,xyseries3;//????
    private XYSeriesRenderer datarenderer1,datarenderer2,datarenderer3;
    private Timer mTimer;
    private TimerTask mTask;

    //motion
    private SensorManager sm;
    int AsensorType;
    int MsensorType;
    int GsensorType;
    int LsensorType;
    /**
     * Enable or disable the add data to series widgets
     *
     * @param enabled the enabled state
     */
    private void setSeriesWidgetsEnabled(boolean enabled) {
        if(D){
            Log.i(TAG, "setSeriesWidgetsEnabled");}

    }

	protected void onCreate(Bundle savedInstanceState) {
    if(D){
      Log.i(TAG, "onCreate");}
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
    mClearButton = (Button) findViewById(R.id.clear_button);

    // set some properties on the main renderer
    mRenderer.setApplyBackgroundColor(true);
    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));

    mRenderer.setXLabels(12);
    mRenderer.setYLabels(10);
    mRenderer.setShowGrid(true);
    mRenderer.setMargins(new int[]{20, 30, 15, 0});
    mRenderer.setZoomButtonsVisible(true);

    mRenderer.setPointSize(5);

    mRenderer.setChartTitle("zozo motion test");
    mRenderer.setXTitle("time");
    mRenderer.setYTitle("acc data");

    mRenderer.setXLabelsAlign(Paint.Align.RIGHT);
    mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
    mRenderer.setAxisTitleTextSize(16);
    mRenderer.setChartTitleTextSize(20);
    mRenderer.setLabelsTextSize(10);
    mRenderer.setLegendTextSize(15);

    mRenderer.setXAxisMin(X_MIN);
    mRenderer.setXAxisMax(X_MAX);
    mRenderer.setYAxisMin(Y_MIN);
    mRenderer.setYAxisMax(Y_MAX);
    mRenderer.setAxesColor(Color.LTGRAY);
    mRenderer.setLabelsColor(Color.LTGRAY);

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
                        datarenderer1.setDisplayChartValues(true);
                        datarenderer1.setColor(Color.GREEN);
                        datarenderer1.setPointStyle(PointStyle.POINT);
                        mRenderer.addSeriesRenderer(datarenderer1);

                        xyseries1 = new XYSeries("lacc_x");
                        mDataset.addSeries(0, xyseries1);
                    } else {
                        if(!EnableSerise1)
                        {
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
                        datarenderer2.setDisplayChartValues(true);
                        datarenderer2.setColor(Color.YELLOW);
                        datarenderer2.setPointStyle(PointStyle.POINT);
                        mRenderer.addSeriesRenderer(datarenderer2);

                        xyseries2 = new XYSeries("lacc_y");
                        mDataset.addSeries(1, xyseries2);

                    } else {
                        if(!EnableSerise2)
                            {
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
                        datarenderer3.setDisplayChartValues(true);
                        datarenderer3.setColor(Color.BLUE);
                        datarenderer3.setPointStyle(PointStyle.POINT);
                        mRenderer.addSeriesRenderer(datarenderer3);

                        xyseries3 = new XYSeries("lacc_z");
                        mDataset.addSeries(2, xyseries3);

                    } else {
                        if(!EnableSerise3)
                        {
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
            sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
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
              if(mStopButton.getText().toString().equals("STOP"))
              {
                  //stopTimer();
                  sm.unregisterListener(myAccelerometerListener);
                  mStopButton.setText("START");
              }
              else if(mStopButton.getText().toString().equals("START"))
              {
                  if(EnableSerise1 | EnableSerise2 | EnableSerise3)
                  {
                      sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
                  }
                  mStopButton.setText("STOP");
              }
          }
      });

      mClearButton.setOnClickListener(new View.OnClickListener() {
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
      });

	}//oncreate end

  @Override
  protected void onResume() {
    if(D){
      Log.i(TAG, "onResume");}
    super.onResume();
    if (mChartView == null) {
      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
      // enable the chart click events
      mRenderer.setClickEnabled(true);
      mRenderer.setSelectableBuffer(10);
      mChartView.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          // handle the click event on the chart
          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
          if (seriesSelection == null) {
            Toast.makeText(MainActivity.this, "No chart element", Toast.LENGTH_SHORT).show();
          } else {
            // display information of the clicked point
            Toast.makeText(
                    MainActivity.this,
                "Chart element in series index " + seriesSelection.getSeriesIndex()
                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
          }
        }
      });
      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      boolean enabled = mDataset.getSeriesCount() > 0;
      setSeriesWidgetsEnabled(enabled);
    } else {
      mChartView.repaint();
    }
  }
  
  
    private void waveUpdataRoutine(float mx,float my,float mz)
    {
        float y_min = 32768, y_max = -32767;
        if (EnableSerise1) {
            xyseries1.add(x_index, mx);
            if(mx > y_max)
            {
                y_max = mx;
            }
            else if(mx < y_min)
            {
                y_min = mx;
            }

        }
        if (EnableSerise2) {
            xyseries2.add(x_index, my);
            if(my > y_max)
            {
                y_max = my;
            }
            else if(my < y_min)
            {
                y_min = my;
            }
        }
        if (EnableSerise3) {
            xyseries3.add(x_index, mz);
            if(mz > y_max)
            {
                y_max = mz;
            }
            else if(mz < y_min)
            {
                y_min = mz;
            }
        }
        x_index += 1;
        mChartView.postInvalidate();

        if(y_max > Y_MAX)
        {
            Y_MAX = y_max * 1.1;
            mRenderer.setXAxisMax(Y_MAX);
        }
        else if(y_min < Y_MIN)
        {
            if(y_min<0)
                Y_MIN = y_min * 1.1;
            mRenderer.setXAxisMax(Y_MIN);
        }

        if (x_index * 1.2 > X_MAX) {
            X_MAX *= 1.2;//
            X_MIN = X_MAX - 100;
            mRenderer.setXAxisMax(X_MAX);
            mRenderer.setXAxisMax(X_MIN);//
        }

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
            Log.i(TAG, "onSensorChanged");
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.i(TAG, "TYPE_ACCELEROMETER");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];


                Log.i(TAG, "\n accel_x " + X_lateral);
                Log.i(TAG, "\n accel_y " + Y_longitudinal);
                Log.i(TAG, "\n accel_z " + Z_vertical);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
                Log.i(TAG, "TYPE_GRAVITY");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];

                waveUpdataRoutine(sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);
                Log.i(TAG, "\n gaccel_x " + X_lateral);
                Log.i(TAG, "\n gaccel_y " + Y_longitudinal);
                Log.i(TAG, "\n gaccel_z " + Z_vertical);
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

                waveUpdataRoutine(sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);
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
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    @Override
    public void onPause(){
        Log.i(TAG, "onPause");
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