package com.fusion.types;

/**
 * Created by zozo on 2016/9/27.
 */
// gyro sensor classure definition
public class GyroSensor extends Types{
    public float fDegPerSecPerCount;				// deg/s per count
    public int iYsBuffer[][] = new int[OVERSAMPLE_RATIO][3];	// buffered sensor frame measurements (counts)
    public int iCountsPerDegPerSec;				// counts per deg/s
    public int iYs[] = new int[3];							// most recent sensor frame measurement (counts)
    public int iWhoAmI;							// sensor whoami
}
