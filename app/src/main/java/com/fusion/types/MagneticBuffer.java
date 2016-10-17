package com.fusion.types;

/**
 * Created by zozo on 2016/10/1.
 */

public class MagneticBuffer extends Types{

    public int[][][] iBs = new int[3][MAGBUFFSIZEX][MAGBUFFSIZEY];		// uncalibrated magnetometer readings
    public int[][] index = new int[MAGBUFFSIZEX][MAGBUFFSIZEY];		// array of time indices
    public int[] tanarray = new int[MAGBUFFSIZEX - 1];				// array of tangents of (100 * angle)
    public int iMagBufferCount;							// number of magnetometer readings

}
