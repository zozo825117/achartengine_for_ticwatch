package com.fusion.types;

/**
 * Created by zozo on 2016/9/27.
 */
// accelerometer sensor classure definition
public class AccelSensor extends Types{
    public float fGsAvg[] = new float[3];						// averaged measurement (g)
    public float fgPerCount;						// g per count
    public float fGsBuffer[][] = new float[OVERSAMPLE_RATIO][3];	// buffered measurements (counts)
    public float fGs[]= new float[3];							// most recent unaveraged measurement (counts)
    public int iGsAvg[]= new int[3];						// averaged measurement (counts)
    public int iCountsPerg;						// counts per g
    public int iWhoAmI;							// sensor whoami
}
