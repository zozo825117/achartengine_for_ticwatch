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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v4.content.ContextCompat;

import  com.example.achartmotion.FileStoreTools;

public class ZozoXYChartBuilder extends Activity {
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
  private static final String TAG = "ZozoXYChartBuilder";

  double Y_Buf[] = { 12.3, 12.5, 13.8, 16.8, 20.4, 24.4, 26.4, 26.1, 23.6, 20.3, 17.2,13.9 };
  double X_Buf[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

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
    double X_MAX = 100;
    boolean EnableSerise1 = false,EnableSerise2 = false,EnableSerise3 = false;
    private XYSeries xyseries1,xyseries2,xyseries3;//数据
    private XYSeriesRenderer datarenderer1,datarenderer2,datarenderer3;
    private Timer mTimer;
    private TimerTask mTask;

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    if(D){
      Log.i(TAG, "onSaveInstanceState");}
    super.onSaveInstanceState(outState);
    // save the current data, for instance when changing screen orientation
    outState.putSerializable("dataset", mDataset);
    outState.putSerializable("renderer", mRenderer);
    outState.putSerializable("current_series", mCurrentSeries);
    outState.putSerializable("current_renderer", mCurrentRenderer);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
    if(D){
      Log.i(TAG, "onRestoreInstanceState");}
    super.onRestoreInstanceState(savedState);
    // restore the current data, for instance when changing the screen
    // orientation
    mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
    mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
    mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
    mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
  }

  @Override
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
    mClearButton = (Button) findViewById(R.id.save_button);

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

    mRenderer.setXAxisMin(0.5);
    mRenderer.setXAxisMax(100);
    mRenderer.setYAxisMin(-40000);
    mRenderer.setYAxisMax(40000);
    mRenderer.setAxesColor(Color.LTGRAY);
    mRenderer.setLabelsColor(Color.LTGRAY);

/*    LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
    mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
    // enable the chart click events
    mRenderer.setClickEnabled(true);
    mRenderer.setSelectableBuffer(10);
    layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
            LayoutParams.FILL_PARENT));*/

      if(requestPermission())
      {
          Log.i(TAG, "getSDPath" + FileStoreTools.getSDPath());
          //
          String str[] = new String[1000];
          //Get the text file based on folder

          File file = new File(FileStoreTools.getSDPath() + File.separator +"zozo_file");
          Log.i(TAG, "file full = " + file + "file path=" + file.getAbsolutePath());
/*          if(file.exists())
          {
              file.delete();
              Log.i(TAG, "file.delete");
          }*/
          for(int i = 0,j=0; i < SineWaveSim.length;i+=2)
          {
              int y;
              String str_buf;
              y = (int) ((int) SineWaveSim[i] | ((int) SineWaveSim[i + 1] << 8));
              if (y > 0x8000) y -= 0x10000;
              str[i] = ""+ y + ",";
              str_buf = formatStr(str[i],7);
              Log.i(TAG, str_buf);
              Log.i(TAG, "file = " + file);
              FileStoreTools.saveFile(str_buf,file.getPath(),"_zozo_xy_char.txt");
          }


      }


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
                    Log.i(TAG, "getSeries" + mDataset.getSeries());
                    if (!EnableSerise1) {
                        EnableSerise1 = true;
                        datarenderer1 = new XYSeriesRenderer();
                        datarenderer1.setDisplayChartValues(true);
                        datarenderer1.setColor(Color.GREEN);
                        datarenderer1.setPointStyle(PointStyle.POINT);
                        mRenderer.addSeriesRenderer(datarenderer1);

                        xyseries1 = new XYSeries("l_acc_x");
                        //xyseries1.add(0, 0);//先输入一个数据让它绘出renderer
                        mDataset.addSeries(0, xyseries1);

                    } else {
                        //if(mDataset.getSeriesAt(0) == null)
                        {
                            mDataset.addSeries(0, xyseries1);
                        }
                        datarenderer1.setColor(Color.GREEN);
                    }
                    break;
                case 2:
                    Log.i(TAG, "getSeries" + mDataset.getSeries());
                    if (!EnableSerise2) {
                        EnableSerise2 = true;
                        datarenderer2 = new XYSeriesRenderer();
                        datarenderer2.setDisplayChartValues(true);
                        datarenderer2.setColor(Color.YELLOW);
                        datarenderer2.setPointStyle(PointStyle.POINT);
                        mRenderer.addSeriesRenderer(datarenderer2);

                        xyseries2 = new XYSeries("l_acc_y");
                        //xyseries2.add(0, 0);//先输入一个数据让它绘出renderer
                        mDataset.addSeries(1, xyseries2);

                    } else {
                        //if(mDataset.getSeriesAt(1) == null)
                            {
                                mDataset.addSeries(1, xyseries2);
                            }


                        datarenderer2.setColor(Color.YELLOW);
                    }
                    break;
                case 3:
                    Log.i(TAG, "getSeries" + mDataset.getSeries());
                    if (!EnableSerise3) {
                        EnableSerise3 = true;
                        datarenderer3 = new XYSeriesRenderer();
                        datarenderer3.setDisplayChartValues(true);
                        datarenderer3.setColor(Color.BLUE);
                        datarenderer3.setPointStyle(PointStyle.POINT);
                        mRenderer.addSeriesRenderer(datarenderer3);

                        xyseries3 = new XYSeries("l_acc_y");
                        //xyseries3.add(0, 0);//先输入一个数据让它绘出renderer
                        mDataset.addSeries(2, xyseries3);

                    } else {
                        //if(mDataset.getSeriesAt(2) == null)
                        {
                            mDataset.addSeries(2, xyseries3);
                        }

                        datarenderer3.setColor(Color.BLUE);
                    }
                    break;
                default:
                    break;
            }

            mStopButton.setText("STOP");

            // time
            startTimer();

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
                  case 1:
                      if (EnableSerise1) {
                          if(mDataset.getSeriesAt(0) != null)
                            mDataset.removeSeries(xyseries1);
                          //datarenderer1.setColor(Color.BLACK);
                      }
                      break;
                  case 2:
                      if (EnableSerise2) {
                          if(mDataset.getSeriesAt(1) != null)
                              mDataset.removeSeries(xyseries2);
                          //datarenderer2.setColor(Color.BLACK);
                      }
                      break;
                  case 3:
                      if (EnableSerise3) {
                          if(mDataset.getSeriesAt(2) != null)
                              mDataset.removeSeries(xyseries3);
                          //datarenderer3.setColor(Color.BLACK);
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
                  stopTimer();
                  mStopButton.setText("START");
              }
              else if(mStopButton.getText().toString().equals("START"))
              {
                  startTimer();
                  mStopButton.setText("STOP");
              }
          }
      });

      mClearButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              //if(mStopButton.getText().toString().equals("STOP"))
              {
                  stopTimer();
                  mStopButton.setText("START");
              }
              if(mDataset != null)
              {
                  mDataset.clear();
                  mChartView.repaint();
              }
          }
      });
  }

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
            Toast.makeText(ZozoXYChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
          } else {
            // display information of the clicked point
            Toast.makeText(
                ZozoXYChartBuilder.this,
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

  /**
   * Enable or disable the add data to series widgets
   * 
   * @param enabled the enabled state
   */
  private void setSeriesWidgetsEnabled(boolean enabled) {
    if(D){
      Log.i(TAG, "setSeriesWidgetsEnabled");}

  }

    private void waveUpdataRoutine()
    {
        int y = 0;
        final int length = SineWaveSim.length;
        Log.i(TAG, "length=" + "" + length);        //mDataset.removeSeries(mCurrentSeries);

        if (EnableSerise1) {
            y = (int) ((int) SineWaveSim[ptr] | ((int) SineWaveSim[ptr + 1] << 8));
            if (y > 0x8000) y -= 0x10000;
            xyseries1.add(x_index, y);
        }
        if (EnableSerise2) {
            int ptr_buf = ptr + 40;
            if (ptr_buf >= length) ptr_buf -= length;
            y = (int) ((int) SineWaveSim[ptr_buf] | ((int) SineWaveSim[ptr_buf + 1] << 8));
            if (y > 0x8000) y -= 0x10000;
            xyseries2.add(x_index, y);
        }
        if (EnableSerise3) {
            int ptr_buf = ptr + 80;
            if (ptr_buf >= length) ptr_buf -= length;
            y = (int) ((int) SineWaveSim[ptr_buf] | ((int) SineWaveSim[ptr_buf + 1] << 8));
            if (y > 0x8000) y -= 0x10000;
            xyseries3.add(x_index, y);
        }
        ptr += 2;
        x_index += 2;
        if (ptr >= length) ptr = 0;

        //更新UI
        //mChartView.repaint();
        mChartView.postInvalidate();

        //延长X_MAX造成右移效果
        if (x_index * 1.2 > X_MAX) {
            X_MAX *= 1.2;//按2倍速度延长 可以设置成speed
            mRenderer.setXAxisMax(X_MAX);// 设置X最大值
        }

        Log.i(TAG, "ptr=" + "" + ptr + "," + "y=" + "" + y);
    }
    private void startTimer()
    {
        if(mTimer == null)
        {
            mTimer = new Timer();

            //2.初始化计时器任务。
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
            //3.启动定时器
                mTimer.schedule(mTask, 2000, 500);
        }

    }


    private void stopTimer()
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
    }

    public boolean requestPermission(){
        //判断当前Activity是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"please give me the permission", Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);

                Log.i(TAG, "ActivityCompat.requestPermissions");
            }

            return  false;
        }
        else
        {
            return  true;
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
                    Log.i(TAG, "PackageManager.PERMISSION_GRANTED  ok");
                    Log.i(TAG, "getSDPath" + FileStoreTools.getSDPath());
                    FileStoreTools.saveFile(SineWaveSim.toString(), FileStoreTools.getSDPath(), "zozo test.txt");
                } else {
                    //申请失败，可以继续向用户解释。
                    //CreateFilePermissionFlag = false;
                    Log.i(TAG, "PackageManager.PERMISSION_GRANTED  refused");
                }
                return;
            }
        }
    }

    static String formatStr(String str, int length)
    {
        if (str == null) {
            return null;
        }
        int strLen = str.length();
        if (strLen == length) {
            return str;
        } else if (strLen < length) {
            int temp = length - strLen;
            String tem = "";
            for (int i = 0; i < temp; i++) {
                tem = tem + " ";
            }
            return tem + str;
        }else{
            return str.substring(0,length);
        }
    }
}