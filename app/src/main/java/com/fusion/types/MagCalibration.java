package com.fusion.types;

/**
 * Created by zozo on 2016/9/27.
 */
// magnetic calibration structure
public class MagCalibration {
    public float fV[] = new float[3];					// current hard iron offset x, y, z, (uT)
    public float finvW[][] = new float[3][3];				// current inverse soft iron matrix
    public float fB;						// current geomagnetic field magnitude (uT)
    public float fFitErrorpc;				// current fit error %
    public float ftrV[] = new float[3];					// trial value of hard iron offset z, y, z (uT)
    public float ftrinvW[][] = new float[3][3];			// trial inverse soft iron matrix size
    public float ftrB;						// trial value of geomagnetic field magnitude in uT
    public float ftrFitErrorpc;			// trial value of fit error %
    public float fA[][] = new float[3][3];					// ellipsoid matrix A
    public float finvA[][] = new float[3][3];				// inverse of ellipsoid matrix A
    public float fmatA[][] = new float[10][10];			// scratch 10x10 matrix used by calibration algorithms
    public float fmatB[][] = new float[10][10];			// scratch 10x10 matrix used by calibration algorithms
    public float fvecA[] = new float[10];				// scratch 10x1 vector used by calibration algorithms
    public float fvecB[] = new float[4];					// scratch 4x1 vector used by calibration algorithms
    public boolean iCalInProgress;			// flag denoting that a calibration is in progress
    public boolean iMagCalHasRun;				// flag denoting that at least one calibration has been launched
    public int iValidMagCal;				// integer value 0, 4, 7, 10 denoting both valid calibration and solver used
}
