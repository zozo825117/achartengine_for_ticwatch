package com.fusion.types;

/**
 * Created by zozo on 2016/9/28.
 */

public class Defined {

    // sampling rate and kalman filter timing eg (25, 1), (200, 8), (400, 16), (500, 20), (600, 24), (800, 32)
    // the MULTI-(B) 9-AXIS and AGM01 boards are able to sample the gyro sensor at 800Hz with Kalman filter rates depending
    // on the processor speed and number of algorithms executing in parallel.
    public static final int SENSORFS          = 200;         // int32: frequency (Hz) of gyro sensor sampling process
    public static final int OVERSAMPLE_RATIO  =	8;			// int32: accel and mag sampling and algorithms run at SENSORFS / OVERSAMPLE_RATIO Hz

    // the quaternion type to be transmitted
    public enum quaternion {Q3, Q3M, Q3G, Q6MA, Q6AG, Q9};

    // vector components
    public static final int CHX = 0;
    public static final int CHY = 1;
    public static final int CHZ = 2;

    // useful multiplicative conversion constants
    public final float PI = 3.141592654F;				// Pi
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
}
