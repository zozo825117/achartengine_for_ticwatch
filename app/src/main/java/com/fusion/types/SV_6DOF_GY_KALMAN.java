package com.fusion.types;

// 6DOF Kalman filter accelerometer and gyroscope state vector structure
public class SV_6DOF_GY_KALMAN {

        // start: elements common to all motion state vectors
        public float fPhiPl;						// roll (deg)
        public float fThePl;						// pitch (deg)
        public float fPsiPl;						// yaw (deg)
        public float fRhoPl;						// compass (deg)
        public float fChiPl;						// tilt from vertical (deg)
        public float[][] fRPl = new float[3][3];					// a posteriori orientation matrix
        public Fquaternion fqPl = new Fquaternion();		// rotation vector
        public float[] fRVecPl = new float[3];					// rotation vector
        public float[] fOmega = new float[3];					// average angular velocity (deg/s)
        int systick;						// systick timer;
        // end: elements common to all motion state vectors
        public float[][] fQw6x6= new float[6][6];					// covariance matrix Qw
        public float[][] fK6x3 = new float[6][3];					// kalman filter gain matrix K
        public float[][] fQwCT6x3 = new float[6][3];				// Qw.C^T matrix
        public float fQv;							// measurement noise covariance matrix leading diagonal
        public float[] fZErr = new float[3];						// measurement error vector
        public float[] fqgErrPl = new float[3];					// gravity vector tilt orientation quaternion error (dimensionless)
        public float[] fbPl = new float[3];						// gyro offset (deg/s)
        public float[] fbErrPl = new float[3];					// gyro offset error (deg/s)
        public float[] fAccGl = new float[3];					// linear acceleration (g) in global frame
        public float fGyrodeltat;					// gyro sampling interval (s) = 1 / SENSORFS
        public float fAlphaOver2;					// PI/360 * fKalmandeltat
        public float fAlphaOver2Sq;				// (PI/360 * fKalmandeltat)^2
        public float fAlphaOver2SqQvYQwb;			// (PI/360 * fKalmandeltat)^2 * (QvY + Qwb)
        public float fAlphaOver2Qwb;				// PI/360 * fKalmandeltat * FQWB_9DOF_GBY_KALMAN;
        public boolean resetflag;						// flag to request re-initialization on next pass

}
