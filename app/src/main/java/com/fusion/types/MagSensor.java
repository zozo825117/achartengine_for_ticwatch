package com.fusion.types;

/**
 * Created by zozo on 2016/9/27.
 */
// magnetometer sensor classure definition
public class MagSensor extends Types{
    public float fBsAvg[] = new float[3];						// averaged un-calibrated measurement (uT)
    public float fBcAvg[] = new float[3];						// averaged calibrated measurement (uT)
    public float fuTPerCount;						// uT per count
    public float fCountsPeruT;						// counts per uT
    public int iBsBuffer[][] = new int[OVERSAMPLE_RATIO][3];	// buffered uncalibrated measurement (counts)
    public int iBs[] = new int[3];							// most recent unaveraged uncalibrated measurement (counts)
    public int iBsAvg[] = new int[3];						// averaged uncalibrated measurement (counts)
    public int iBcAvg[] = new int[3];						// averaged calibrated measurement (counts)
    public int iCountsPeruT;						// counts per uT
    public int iWhoAmI;							// sensor whoami
}
