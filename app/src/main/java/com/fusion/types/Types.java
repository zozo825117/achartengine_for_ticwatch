package com.fusion.types;

/**
 * Created by zozo on 2016/9/28.
 */

public class Types {

    // sampling rate and kalman filter timing eg (25, 1), (200, 8), (400, 16), (500, 20), (600, 24), (800, 32)
    // the MULTI-(B) 9-AXIS and AGM01 boards are able to sample the gyro sensor at 800Hz with Kalman filter rates depending
    // on the processor speed and number of algorithms executing in parallel.
    public static final int SENSORFS          = 119;         // Gyro delta time = 8388608.0 ns // int32: frequency (Hz) of gyro sensor sampling process
    public static final int OVERSAMPLE_RATIO  =	2;			// int32: accel and mag sampling and algorithms run at SENSORFS / OVERSAMPLE_RATIO Hz

    // coordinate system for the build
    public static final int NED = 0;                       // identifier for NED (Aerospace) axes and angles
    public static final int ANDROID = 1;                   // identifier for Android axes and angles
    public static final int WIN8 = 2;						// identifier for Windows 8 axes and angles
    public static final int THISCOORDSYSTEM = ANDROID;			// the coordinate system to be used
    // the quaternion type to be transmitted
    public enum quaternion {Q3, Q3M, Q3G, Q6MA, Q6AG, Q9};

    // vector components
    public static final int CHX = 0;
    public static final int CHY = 1;
    public static final int CHZ = 2;

    // useful multiplicative conversion constants
    public static final float PI = 3.141592654F;				// Pi
    public static final float FPIOVER180 = 0.01745329251994F;	// degrees to radians conversion = pi / 180
    public static final float F180OVERPI = 57.2957795130823F;	// radians to degrees conversion = 180 / pi
    public static final float F180OVERPISQ = 3282.8063500117F;	// square of F180OVERPI
    public static final float ONETHIRD = 0.33333333F;			// one third
    public static final float ONESIXTH = 0.166666667F;			// one sixth
    public static final float ONEOVER48 = 0.02083333333F;		// 1 / 48
    public static final float ONEOVER120 = 0.0083333333F;		// 1 / 120
    public static final float ONEOVER3840 = 0.0002604166667F;	// 1 / 3840
    public static final float ONEOVERSQRT2 = 0.707106781F;		// 1/sqrt(2)
    public static final float GTOMSEC2 = 9.80665F;				// standard gravity in m/s2

    // magnetic calibration constants
    public static final int MAGBUFFSIZEX = 14;				// x dimension in magnetometer buffer (12x24 equals 288 elements)
    public static final int MAGBUFFSIZEY = (2 * MAGBUFFSIZEX);	// y dimension in magnetometer buffer (12x24 equals 288 elements)
    public final int MINMEASUREMENTS4CAL = 100;		// minimum number of measurements for 4 element calibration
    public static final int MINMEASUREMENTS7CAL = 150;			// minimum number of measurements for 7 element calibration
    public static final int MINMEASUREMENTS10CAL = 250;		// minimum number of measurements for 10 element calibration
    public static final int MAXMEASUREMENTS = 340;				// maximum number of measurements used for calibration
    public static final int INTERVAL4CAL = 75;					// 3s at 25Hz: 4 element interval (samples)
    public static final int INTERVAL7CAL = 250;				// 10s at 25Hz: 7 element interval (samples)
    public static final int INTERVAL10CAL = 750;				// 30s at 25Hz: 10 element interval (samples)
    public static final float MINBFITUT = 10.0F;					// minimum acceptable geomagnetic field B (uT) for valid calibration
    public static final float MAXBFITUT = 90.0F;					// maximum acceptable geomagnetic field B (uT) for valid calibration
    public static final float FITERRORAGINGSECS = 86400.0F;		// 24 hours: time (s) for fit error to increase (age) by e=2.718
    public static final int  MESHDELTACOUNTS = 50;				// magnetic buffer mesh spacing in counts (here 5uT)
    public static final float DEFAULTB = 50.0F;					// default geomagnetic field (uT)

    public static final float GRAVITY = 9.8066f;
    public static final int ANDROID_ACC_COUNTSPERG = 98066 ;
    public static final int ANDROID_GYRO_COUNTSPERDEGPERSEC = 100;
    public static final int ANDROID_MAG_COUNTSPERUT = 10;

}
