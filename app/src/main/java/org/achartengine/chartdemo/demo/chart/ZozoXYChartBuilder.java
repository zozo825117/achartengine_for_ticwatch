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

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
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
import java.util.logging.Handler;

public class ZozoXYChartBuilder extends Activity {
  /** The main dataset that includes all the series that go into a chart. */
  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
  /** The main renderer that includes all the renderers customizing a chart. */
  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  /** The most recently added series. */
  private XYSeries mCurrentSeries;
  /** The most recently created renderer, customizing the current series. */
  private XYSeriesRenderer mCurrentRenderer;
  /** Button for creating a new series of data. */
  private Button mNewSeries;
  /** Button for adding entered data to the current series. */
  private Button mAdd;
  /** Edit text field for entering the X value of the data to be added. */
  private EditText mX;
  /** Edit text field for entering the Y value of the data to be added. */
  private EditText mY;
  /** The chart view that displays the data. */
  private GraphicalView mChartView;

  final boolean D = true;
  private static final String TAG = "XYChartBuilder";

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

  int ptr = 0,x_index = 0;
    int X_MAX;

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
    setContentView(R.layout.xy_chart);

    // the top part of the UI components for adding new data points
    mX = (EditText) findViewById(R.id.xValue);
    mY = (EditText) findViewById(R.id.yValue);
    mAdd = (Button) findViewById(R.id.add);

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
      mRenderer.setYTitle("raw data");

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

    // the button that handles the new series of data creation
    mNewSeries = (Button) findViewById(R.id.new_series);
    mNewSeries.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if(D){
          Log.i(TAG, "mNewSeries onClick");}
        String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
        // create a new series of data
        XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries = series;


          X_MAX = 100;

/*        int length = X_Buf.length;
        for (int i = 0; i < length; i++)
        {
          mCurrentSeries.add(X_Buf[i], Y_Buf[i]);
        }*/
        //
        final int length = SineWaveSim.length;


        // time
        final Timer timer = new Timer();
        TimerTask task;

        //2.初始化计时器任务。
        task = new TimerTask() {
          @Override
          public void run() {
            // TODO Auto-generated method stub
            int y = 0;
              y = (int)((int)SineWaveSim[ptr] | ((int)SineWaveSim[ptr+1]<<8));
              if(y > 0x8000)y-=0x10000;


              mDataset.removeSeries(mCurrentSeries);
              mCurrentSeries.add(ptr, y);
              mDataset.addSeries(mCurrentSeries);

              ptr+=2;
              x_index+=2;
              if(ptr >= length)ptr=0;

              //更新UI
              //mChartView.repaint();
              mChartView.postInvalidate();

              //延长X_MAX造成右移效果
              if(x_index * 2 > X_MAX)
              {
                  X_MAX*=2;//按2倍速度延长 可以设置成speed
                  mRenderer.setXAxisMax(X_MAX);// 设置X最大值
              }

            Log.i(TAG,"ptr="+""+ ptr +","+"y="+""+ y);
          }
        };
        //3.启动定时器
        timer.schedule(task, 2000, 200);

        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // set some renderer properties
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(true);
        renderer.setDisplayChartValuesDistance(10);
        mCurrentRenderer = renderer;
        setSeriesWidgetsEnabled(true);
        mChartView.repaint();
      }
    });

    mAdd.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if(D){
          Log.i(TAG, "mAdd onClick");}
        double x = 0;
        double y = 0;
        try {
          x = Double.parseDouble(mX.getText().toString());
        } catch (NumberFormatException e) {
          mX.requestFocus();
          return;
        }
        try {
          y = Double.parseDouble(mY.getText().toString());
        } catch (NumberFormatException e) {
          mY.requestFocus();
          return;
        }
        // add a new data point to the current series
        mCurrentSeries.add(x, y);
        mX.setText("");
        mY.setText("");
        mX.requestFocus();
        // repaint the chart such as the newly added point to be visible
        mChartView.repaint();
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
    mX.setEnabled(enabled);
    mY.setEnabled(enabled);
    mAdd.setEnabled(enabled);
  }
}