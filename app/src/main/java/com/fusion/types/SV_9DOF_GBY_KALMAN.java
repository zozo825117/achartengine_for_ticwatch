package com.fusion.types;


/**
 * Created by zozo on 2016/9/27.
 */
// start: elements common to all motion state vectors
public class SV_9DOF_GBY_KALMAN extends Fquaternion{
    public float fPhiPl;						// roll (deg)
    public float fThePl;						// pitch (deg)
    public float fPsiPl;						// yaw (deg)
    public float fRhoPl;						// compass (deg)
    public float fChiPl;						// tilt from vertical (deg)
    public float fRPl[][] = new float[3][3];					// a posteriori orientation matrix
    public Fquaternion fqPl = new Fquaternion();			// a posteriori orientation quaternion
    public float fRVecPl[] = new float[3];					// rotation vector
    public float fOmega[] = new float[3];					// average angular velocity (deg/s)
    public int systick;						// systick timer;
                 // end: elements common to all motion state vectors
    public float fQw10x10[][] = new float[10][10];				// covariance matrix Qw
    public float fK10x7[][] = new float[10][7];				// kalman filter gain matrix K
    public float fQwCT10x7[][] = new float[10][7];				// Qw.C^T matrix
    public float fZErr[] = new float[7];						// measurement error vector
    public float fQv7x1[] = new float[7];					// measurement noise covariance matrix leading diagonal
    public float fDeltaPl;						// a posteriori inclination angle from Kalman filter (deg)
    public float fsinDeltaPl;					// sin(fDeltaPl)
    public float fcosDeltaPl;					// cos(fDeltaPl)
    public float fqgErrPl[] = new float[3];					// gravity vector tilt orientation quaternion error (dimensionless)
    public float fqmErrPl[] = new float[3];					// geomagnetic vector tilt orientation quaternion error (dimensionless)
    public float fbPl[] = new float[3];						// gyro offset (deg/s)
    public float fbErrPl[] = new float[3];					// gyro offset error (deg/s)
    public float fDeltaErrPl;					// a priori inclination angle error correction
    public float fAccGl[] = new float[3];					// linear acceleration (g) in global frame
    public float fVelGl[] = new float[3];					// velocity (m/s) in global frame
    public float fDisGl[] = new float[3];					// displacement (m) in global frame
    public float fGyrodeltat;					// gyro sampling interval (s) = 1 / SENSORFS
    public float fKalmandeltat;				// Kalman filter interval (s) = OVERSAMPLE_RATIO / SENSORFS
    public float fgKalmandeltat;				// g (m/s2) * Kalman filter interval (s) = 9.80665 * OVERSAMPLE_RATIO / SENSORFS
    public float fAlphaOver2;					// PI/360 * fKalmandeltat
    public float fAlphaOver2Sq;				// (PI/360 * fKalmandeltat)^2
    public float fAlphaOver2SqQvYQwb;			// (PI/360 * fKalmandeltat)^2 * (QvY + Qwb)
    public float fAlphaOver2Qwb;				// PI/360 * fKalmandeltat * FQWB_9DOF_GBY_KALMAN;
    public boolean iFirstAccelMagLock;			// denotes that 9DOF orientation has locked to 6DOF eCompass
    public boolean resetflag;						// flag to request re-initialization on next pass
}
