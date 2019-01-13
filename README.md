achartengine_for_ticwatch
========


android app
--------

achartengine_for_ticwatch  use mobvoi-api.jar communication to ticwatch , draw motion sensor oscillogram by achartengine.
用achartengine绘制6轴曲线和融合后的姿态角曲线，为6Dof算法调试提供一种便利的工具。并加入了6Dof算法及卡尔曼滤波器，可以方便对比算法和android自带算法的差异。
传感器可以用android自带的9轴传感器，也可以是[Ticwear](https://ticwear.com/ticwear/ticwear4.html)（出门问问）智能手表提供的传感器数据。
表端样例参考TicwearApiDemo-master可自行在我的仓库查阅。

## Features

- [x] 创建一个SensorManager来获取系统的传感器服务
- [x] achartengine绘制动态图标
- [x] 6Dof算法
- [x] mobvoi介入android wear
- [x] 数据日志保持


## TODO
- [ ] 加入BLE设备

## Project Organization
![](http://ww1.sinaimg.cn/large/98df967fly1fz5bc40zo0j20a60m5wg9.jpg)

主要文件介绍

    ├── manifests
	│   ├──AndroidManifest.xml       <- Android Manifest file
    ├── java
	│   ├── com      
	│  	│   ├── example.achartmotion       <- motion application.
	│   │  	│   ├ FileStoreTools.java		 <- save log.
	│   │  	│   ├ MotionXYChartBuilder.java
	│   │  	│   ├ TicXYChartBuilder.java
    │  	│   ├── fusion      
	│   │  	│   ├ Fusion.java      <- fusion routines.
	│   │  	│   ├ FusionTask.java	 <- fusion task.
	│   │  	│   ├ Magnetic.java      <- Magnetic Calibration.
	│   │  	│   ├ Matrix.java			<- Matrix math method.
	│   │  	│   ├ Orientation.java      <- Orientation math method.
    │  	│   ├── kalmanfilter      <- kalman filter test
    │   ├── org.achartengine      <- achartengine android SDK
    ├── res
	│   ├── layout      
	│  	│   ├── xy_zozo_chart.xml       <- motion application layout.
    ├── README.md          <- The top-level README for developers using this project.
    ├── Gradle Scripte
    │   ├── build.gradle       <- Top-level build file.
	
	
## build

- Android Studio 3.0.1 build
- com.android.tools.build:gradle:2.2.1
- compileSdkVersion 23
- buildToolsVersion "23.0.2"
- minSdkVersion 18
- targetSdkVersion 23
- compile files('libs/mobvoi-api.jar')
- compile 'com.android.support:appcompat-v7:23.1.1'
- compile 'com.android.support:support-v4:23.1.1'
	
	
## Useage
* 主菜单

|      | class				             | explain                        |
| --- | ---------------------------- | ------------------------------ |
| 1   | XYChartBuilder               | Embedded line chart demo       |
| 2   | PieChartBuilder              | Embedded pie chart demo        |
| 3   | ZozoXYChartBuilder           | zozo line chart demo           |
| 4   | MotionXYChartBuilder         | motion line chart demo         |
| 5   | TicXYChartBuilder            | ticwear motion line chart demo |
| 6   | AverageTemperatureChart      |                                |
| 7   | AverageCubicTemperatureChart |                                |
| 8   | SalesStackedBarChart         |                                |
| 9   | SalesBarChart                |                                |
| 10  | TrigonometricFunctionsChart  |                                |
| 11  | ScatterChart                 |                                |
| 12  | SalesComparisonChart         |                                |
| 13  | ProjectStatusChart           |                                |
| 14  | SalesGrowthChart             |                                |
| 15  | BudgetPieChart               |                                |
| 16  | BudgetDoughnutChart          |                                |
| 17  | ProjectStatusBubbleChart     |                                |
| 18  | TemperatureChart             |                                |
| 19  | WeightDialChart              |                                |
| 20  | SensorValuesChart            |                                |
| 21  | CombinedTemperatureChart     |                                |
| 22  | MultipleTemperatureChart     |                                |

* MotionXYChartBuilder
<img src="http://ww1.sinaimg.cn/large/98df967fly1fz5c26h0itj20u01hcwgm.jpg" width = "450" height = "800" alt="图片名称" align=left />

`EBUT`加入系列 `EBUT`移除系列
`1-3` 加入三轴加速度数据
`4-6` 加入三轴陀螺仪数据
`8` 加入fusion数据
`11` Kalman filter 测试数据
`12` 标准的android的旋转矩阵计算数据


``` java
    void EnableButtonClick()
    {
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
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                    EnableWave1to6(x);
                break;
            case 8:{
                    EnableWaveFusion();
                break;
            }
            case 9:

            case 10:
            case 11:
                    EnableWaveKalman(x);
                break;
            case 12:
                    EnableMySensorFusion();
                break;
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
	
```
![参考坐标系](http://ww1.sinaimg.cn/large/98df967fly1fz5cztty52j20dd0bq0tp.jpg)

##  Reference

- [achartengine官网](http://www.achartengine.org/)

- 本文代码fork自 [ddanny/achartengine](https://github.com/ddanny/achartengine)

- [Android Sensor Fusion Tutorial](https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial)

