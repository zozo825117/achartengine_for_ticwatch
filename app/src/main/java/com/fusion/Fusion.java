package com.fusion;

import android.nfc.Tag;
import android.util.Log;

import com.fusion.types.SV_9DOF_GBY_KALMAN;
import com.fusion.types.AccelSensor;
import com.fusion.types.MagSensor;
import com.fusion.types.MagCalibration;
import com.fusion.types.Types;
import com.fusion.types.GyroSensor;
import com.fusion.types.Fquaternion;
import com.fusion.types.SV_6DOF_GY_KALMAN;

import static com.fusion.types.Types.quaternion.Q6AG;


// Copyright (c) 2014, 2015, Freescale Semiconductor, Inc.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of Freescale Semiconductor, Inc. nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL FREESCALE SEMICONDUCTOR, INC. BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// This is the file that contains the fusion routines.  It is STRONGLY RECOMMENDED
// that the casual developer NOT TOUCH THIS FILE.  The mathematics behind this file
// is extremely complex, and it will be very easy (almost inevitable) that you screw
// it up.
//

public class Fusion extends Types{

    private static boolean D = true;
    private static String TAG = "Fusion";
    // *********************************************************************************
    // COMPUTE_6DOF_GY_KALMAN constants
    // *********************************************************************************

    // gyro sensor noise covariance units deg^2
    private static final float FQVY_6DOF_GY_KALMAN = 1E4F;
    // gyro offset random walk units (deg/s)^2
    private static final float FQWB_6DOF_GY_KALMAN = 5E-1F;
    // *********************************************************************************
    // COMPUTE_9DOF_GBY_KALMAN constants
    // *********************************************************************************

    // gyro sensor noise covariance units deg^2
    private static final float FQVY_9DOF_GBY_KALMAN =1E4F;
    // gyro offset random walk units (deg/s)^2
    private static final float FQWB_9DOF_GBY_KALMAN = 5E-1F;
    // geomagnetic inclination angle random walk units deg/s
    private static final float FQWDLT_9DOF_GBY_KALMAN = 2E1F;
    // minimum magnetometer and magnetic disturbance
    private static final float FQVBQD_MIN_9DOF_GBY_KALMAN = 7E0F;
    // minimum and maximum gyro offset in deg/s
    private static final float FGYRO_OFFSET_MIN_9DOF_GBY_KALMAN =	-5.0F;
    private static final float FGYRO_OFFSET_MAX_9DOF_GBY_KALMAN =	5.0F;

    // function initalizes the 6DOF accel + gyro Kalman filter algorithm
    static void fInit_6DOF_GY_KALMAN(SV_6DOF_GY_KALMAN pthisSV,  AccelSensor pthisAccel)
    {
        int i;				// counter

        // compute and store useful product terms to save floating point calculations later
        pthisSV.fGyrodeltat = 1.0F / (float) SENSORFS;
        pthisSV.fAlphaOver2 = 0.5F * FPIOVER180 * (float) OVERSAMPLE_RATIO / (float) SENSORFS;
        pthisSV.fAlphaOver2Sq = pthisSV.fAlphaOver2 * pthisSV.fAlphaOver2;
        pthisSV.fAlphaOver2Qwb = pthisSV.fAlphaOver2 * FQWB_6DOF_GY_KALMAN;
        pthisSV.fAlphaOver2SqQvYQwb = pthisSV.fAlphaOver2Sq * (FQVY_6DOF_GY_KALMAN + FQWB_6DOF_GY_KALMAN);

        // zero the a posteriori gyro offset and error vectors
        for (i = CHX; i <= CHZ; i++)
        {
            pthisSV.fbPl[i] = 0.0F;
            pthisSV.fqgErrPl[i] = 0.0F;
            pthisSV.fbErrPl[i] = 0.0F;
        }

        // initialize the a posteriori orientation state vector to the tilt orientation
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                f3DOFTiltNED(pthisSV.fRPl, pthisAccel.fGsAvg);
                break;
            case ANDROID:
                f3DOFTiltAndroid(pthisSV.fRPl, pthisAccel.fGsAvg);
                break;
            case WIN8:
            default:
                f3DOFTiltWin8(pthisSV.fRPl, pthisAccel.fGsAvg);
                break;
        }*/
        Orientation.f3DOFTiltAndroid(pthisSV.fRPl, pthisAccel.fGsAvg);
        Orientation.fQuaternionFromRotationMatrix(pthisSV.fRPl, pthisSV.fqPl);

        // update the default quaternion type supported to the most sophisticated
/*        if (globals.DefaultQuaternionPacketType < Q6AG)
            globals.QuaternionPacketType = globals.DefaultQuaternionPacketType = Q6AG;*/

        // clear the reset flag
        pthisSV.resetflag = false;

        //return;
    } // end fInit_6DOF_GY_KALMAN

    static void fInit_9DOF_GBY_KALMAN(SV_9DOF_GBY_KALMAN pthisSV,AccelSensor pthisAccel, MagSensor pthisMag,
                               MagCalibration pthisMagCal)
    {
        float fDelta6DOF=0;		// eCompass estimate of inclination angle returned by least squares eCompass
        float pfQvGQa=0;			// accelerometer noise covariance to 1g sphere
        float fQvBQd=0;			// magnetometer noise covariances to geomagnetic sphere
        int i;					// counter

        if(D) Log.d(TAG,"fInit_9DOF_GBY_KALMAN");
        // compute and store useful product terms to save floating point calculations later
        pthisSV.fGyrodeltat = 1.0F / (float) SENSORFS;
        pthisSV.fKalmandeltat = (float) OVERSAMPLE_RATIO / (float) SENSORFS;
        pthisSV.fgKalmandeltat = GTOMSEC2 * pthisSV.fKalmandeltat;
        pthisSV.fAlphaOver2 = 0.5F * FPIOVER180 * (float) OVERSAMPLE_RATIO / (float) SENSORFS;
        pthisSV.fAlphaOver2Sq = pthisSV.fAlphaOver2 * pthisSV.fAlphaOver2;
        pthisSV.fAlphaOver2Qwb = pthisSV.fAlphaOver2 * FQWB_9DOF_GBY_KALMAN;
        pthisSV.fAlphaOver2SqQvYQwb = pthisSV.fAlphaOver2Sq * (FQVY_9DOF_GBY_KALMAN + FQWB_9DOF_GBY_KALMAN);
// zero the a posteriori gyro offset and error vectors
        for (i = CHX; i <= CHZ; i++)
        {
            pthisSV.fbPl[i] = 0.0F;
            pthisSV.fqgErrPl[i] = 0.0F;
            pthisSV.fqmErrPl[i] = 0.0F;
            pthisSV.fbErrPl[i] = 0.0F;
        }
        pthisSV.fDeltaPl = 0.0F;
        pthisSV.fDeltaErrPl = 0.0F;

        // zero the inertial navigation velocity and displacement in the global frame
        for (i = CHX; i <= CHZ; i++)
        {
            pthisSV.fVelGl[i] = 0.0F;
            pthisSV.fDisGl[i] = 0.0F;
        }

        // initialize the posteriori orientation state vector to the instantaneous eCompass orientation
        pthisSV.iFirstAccelMagLock = false;
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                fLeastSquareseCompassNED(&(pthisSV.fqPl), pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl,
                        pthisSV.fcosDeltaPl, &fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, &fQvBQd, &pfQvGQa);
                break;
            case ANDROID:
                fLeastSquareseCompassAndroid(&(pthisSV.fqPl), pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl,
                        pthisSV.fcosDeltaPl, &fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, &fQvBQd, &pfQvGQa);
                break;
            case WIN8:
            default:
                fLeastSquareseCompassWin8(&(pthisSV.fqPl), pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl,
                        pthisSV.fcosDeltaPl, &fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, &fQvBQd, &pfQvGQa);
                break;
        }*/
        Orientation.fLeastSquareseCompassAndroid(pthisSV.fqPl, pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl,
                pthisSV.fcosDeltaPl, fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, fQvBQd, pfQvGQa);
        Orientation.fRotationMatrixFromQuaternion(pthisSV.fRPl, pthisSV.fqPl);

        // initialize the geomagnetic inclination angle to the estimate from the eCompass call.
        // this will be inaccurate because of the lack of a magnetic calibration but prevents a transient in
        // the Kalman filter that will be tracking it even before the first magnetic calibration.
        pthisSV.fDeltaPl = fDelta6DOF;
        pthisSV.fsinDeltaPl = (float)Math.sin(pthisSV.fDeltaPl * FPIOVER180);
        pthisSV.fcosDeltaPl = (float)Math.sqrt(1.0F - pthisSV.fsinDeltaPl * pthisSV.fsinDeltaPl);

        // update the default quaternion type supported to the most sophisticated
/*        if (globals.DefaultQuaternionPacketType < Q9)
            globals.QuaternionPacketType = globals.DefaultQuaternionPacketType = Q9;*/

        // clear the reset flag
        pthisSV.resetflag = false;

        //return;

    }

    // 6DOF accelerometer+gyroscope orientation function implemented using indirect complementary Kalman filter
    static void fRun_6DOF_GY_KALMAN( SV_6DOF_GY_KALMAN pthisSV,  AccelSensor pthisAccel,  GyroSensor pthisGyro)
    {
        // local scalars and arrays
        float ftmpMi3x1[] = new float[3];					// temporary vector used for a priori calculations
        float ftmp3DOF3x1[] = new float[3];				// temporary vector used for 3DOF calculations
        float ftmpA3x3[][] = new float[3][3];				// scratch 3x3 matrix
        float fC3x6ik;						// element i, k of measurement matrix C
        float fC3x6jk;						// element j, k of measurement matrix C
        float fmodSqGs;						// modulus squared of accelerometer measurement
        Fquaternion fqMi;			// a priori orientation quaternion
        Fquaternion ftmpq = new Fquaternion();			// scratch quaternion
        float ftmp;							// scratch float
        boolean ierror = false;						// matrix inversion error flag
        int i, j, k;						// loop counters

        // working arrays for 3x3 matrix inversion
        float pfRows[][] = new float[3][];
        int iColInd[] = new int[3];
        int iRowInd[] = new int[3];
        int iPivot[] = new int[3];

        // if requested, do a reset initialization with no further processing
        if (pthisSV.resetflag)
        {
            fInit_6DOF_GY_KALMAN(pthisSV, pthisAccel);
            return;
        }

        // compute the a priori orientation quaternion fqMi by integrating the buffered gyro measurements
        fqMi = pthisSV.fqPl;
        // loop over all the buffered gyroscope measurements
        for (j = 0; j < OVERSAMPLE_RATIO; j++)
        {
            // calculate the instantaneous angular velocity (after subtracting the gyro offset) and rotation vector ftmpMi3x1
            for (i = CHX; i <= CHZ; i++)
            {
                //pthisSV.fOmega[i] = (float)pthisGyro.iYsBuffer[j][i] * pthisGyro.fDegPerSecPerCount - pthisSV.fbPl[i];
                pthisSV.fOmega[i] = (float)pthisGyro.fYsBuffer[j][i] - pthisSV.fbPl[i];
                //ftmpMi3x1[i] = pthisSV.fOmega[i] * pthisSV.fGyrodeltat;
                ftmpMi3x1[i] = pthisSV.fOmega[i];
            }
            // convert the rotation vector ftmpMi3x1 to a rotation quaternion ftmpq
            Orientation.fQuaternionFromRotationVectorDeg(ftmpq, ftmpMi3x1, 1.0F);
            // integrate the gyro sensor incremental quaternions into the a priori orientation quaternion fqMi
            Orientation.qAeqAxB(fqMi, ftmpq);
        }

        // set ftmp3DOF3x1 to the 3DOF gravity vector in the sensor frame
        fmodSqGs = pthisAccel.fGsAvg[CHX] * pthisAccel.fGsAvg[CHX] + pthisAccel.fGsAvg[CHY] * pthisAccel.fGsAvg[CHY] +
                pthisAccel.fGsAvg[CHZ] * pthisAccel.fGsAvg[CHZ];
        if (fmodSqGs != 0.0F)
        {
            // normal non-freefall case
            ftmp = 1.0F / (float) Math.sqrt(Math.abs(fmodSqGs));
            ftmp3DOF3x1[CHX] = pthisAccel.fGsAvg[CHX] * ftmp;
            ftmp3DOF3x1[CHY] = pthisAccel.fGsAvg[CHY] * ftmp;
            ftmp3DOF3x1[CHZ] = pthisAccel.fGsAvg[CHZ] * ftmp;
        }
        else
        {
            // use zero tilt in case of freefall
            ftmp3DOF3x1[CHX] = 0.0F;
            ftmp3DOF3x1[CHY] = 0.0F;
            ftmp3DOF3x1[CHZ] = 1.0F;
        }

        // correct accelerometer gravity vector for different coordinate systems
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                // +1g in accelerometer z axis (z down) when PCB is flat so no correction needed
                break;
            case ANDROID:
                // +1g in accelerometer z axis (z up) when PCB is flat so negate the vector to obtain gravity
                ftmp3DOF3x1[CHX] = -ftmp3DOF3x1[CHX];
                ftmp3DOF3x1[CHY] = -ftmp3DOF3x1[CHY];
                ftmp3DOF3x1[CHZ] = -ftmp3DOF3x1[CHZ];
                break;
            case WIN8:
            default:
                // -1g in accelerometer z axis (z up) when PCB is flat so no correction needed
                break;
        }*/
        // +1g in accelerometer z axis (z up) when PCB is flat so negate the vector to obtain gravity
        ftmp3DOF3x1[CHX] = -ftmp3DOF3x1[CHX];
        ftmp3DOF3x1[CHY] = -ftmp3DOF3x1[CHY];
        ftmp3DOF3x1[CHZ] = -ftmp3DOF3x1[CHZ];

        // set ftmpMi3x1 to the a priori gravity vector in the sensor frame from the a priori quaternion
        ftmpMi3x1[CHX] = 2.0F * (fqMi.q1 * fqMi.q3 - fqMi.q0 * fqMi.q2);
        ftmpMi3x1[CHY] = 2.0F * (fqMi.q2 * fqMi.q3 + fqMi.q0 * fqMi.q1);
        ftmpMi3x1[CHZ] = 2.0F * (fqMi.q0 * fqMi.q0 + fqMi.q3 * fqMi.q3) - 1.0F;

        // correct a priori gravity vector for different coordinate systems
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                // z axis is down (NED) so no correction needed
                break;
            case ANDROID:
            case WIN8:
            default:
                // z axis is up (ENU) so no negate the vector to obtain gravity
                ftmpMi3x1[CHX] = -ftmpMi3x1[CHX];
                ftmpMi3x1[CHY] = -ftmpMi3x1[CHY];
                ftmpMi3x1[CHZ] = -ftmpMi3x1[CHZ];
                break;
        }*/
        // calculate the rotation quaternion between 3DOF and a priori gravity vectors (ignored minus signs cancel here)
        // and copy the quaternion vector components to the measurement error vector fZErr
        Orientation.fveqconjgquq(ftmpq, ftmp3DOF3x1, ftmpMi3x1);
        pthisSV.fZErr[CHX] = ftmpq.q1;
        pthisSV.fZErr[CHY] = ftmpq.q2;
        pthisSV.fZErr[CHZ] = ftmpq.q3;

        // update Qw using the a posteriori error vectors from the previous iteration.
        // as Qv increases or Qw decreases, K . 0 and the Kalman filter is weighted towards the a priori prediction
        // as Qv decreases or Qw increases, KC . I and the Kalman filter is weighted towards the measurement.
        // initialize Qw to all zeroes
        for (i = 0; i < 6; i++)
            for (j = 0; j < 6; j++)
                pthisSV.fQw6x6[i][j] = 0.0F;
        // partial diagonal gyro offset terms
        pthisSV.fQw6x6[3][3] = pthisSV.fbErrPl[CHX] * pthisSV.fbErrPl[CHX];
        pthisSV.fQw6x6[4][4] = pthisSV.fbErrPl[CHY] * pthisSV.fbErrPl[CHY];
        pthisSV.fQw6x6[5][5] = pthisSV.fbErrPl[CHZ] * pthisSV.fbErrPl[CHZ];
        // diagonal gravity vector components
        pthisSV.fQw6x6[0][0] = pthisSV.fqgErrPl[CHX] * pthisSV.fqgErrPl[CHX] + pthisSV.fAlphaOver2SqQvYQwb +
                pthisSV.fAlphaOver2Sq * pthisSV.fQw6x6[3][3];
        pthisSV.fQw6x6[1][1] = pthisSV.fqgErrPl[CHY] * pthisSV.fqgErrPl[CHY] + pthisSV.fAlphaOver2SqQvYQwb +
                pthisSV.fAlphaOver2Sq * pthisSV.fQw6x6[4][4];
        pthisSV.fQw6x6[2][2] = pthisSV.fqgErrPl[CHZ] * pthisSV.fqgErrPl[CHZ] + pthisSV.fAlphaOver2SqQvYQwb +
                pthisSV.fAlphaOver2Sq * pthisSV.fQw6x6[5][5];
        // remaining diagonal gyro offset components
        pthisSV.fQw6x6[3][3] += FQWB_6DOF_GY_KALMAN;
        pthisSV.fQw6x6[4][4] += FQWB_6DOF_GY_KALMAN;
        pthisSV.fQw6x6[5][5] += FQWB_6DOF_GY_KALMAN;
        // off diagonal gravity and gyro offset components
        pthisSV.fQw6x6[0][3] = pthisSV.fQw6x6[3][0] = pthisSV.fqgErrPl[CHX] * pthisSV.fbErrPl[CHX] - pthisSV.fAlphaOver2Qwb;
        pthisSV.fQw6x6[1][4] = pthisSV.fQw6x6[4][1] = pthisSV.fqgErrPl[CHY] * pthisSV.fbErrPl[CHY] - pthisSV.fAlphaOver2Qwb;
        pthisSV.fQw6x6[2][5] = pthisSV.fQw6x6[5][2] = pthisSV.fqgErrPl[CHZ] * pthisSV.fbErrPl[CHZ] - pthisSV.fAlphaOver2Qwb;

        // calculate the vector fQv containing the diagonal elements of the measurement covariance matrix Qv
        pthisSV.fQv = 0.25F * Math.abs(fmodSqGs - 1.0F) + pthisSV.fAlphaOver2SqQvYQwb;

        // calculate the 6x3 Kalman gain matrix K = Qw * C^T * inv(C * Qw * C^T + Qv)
        // set fQwCT6x3 = Qw.C^T where Qw has size 6x6 and C^T has size 6x3
        for (i = 0; i < 6; i++) // loop over rows
        {
            for (j = 0; j < 3; j++) // loop over columns
            {
                pthisSV.fQwCT6x3[i][j] = 0.0F;
                // accumulate matrix sum
                for (k = 0; k < 6; k++)
                {
                    // determine fC3x6[j][k] since the matrix is highly sparse
                    fC3x6jk = 0.0F;
                    if (k == j) fC3x6jk = 1.0F;
                    if (k == (j + 3)) fC3x6jk = -pthisSV.fAlphaOver2;
                    // accumulate fQwCT6x3[i][j] += Qw6x6[i][k] * C[j][k]
                    if ((pthisSV.fQw6x6[i][k] != 0.0F) && (fC3x6jk != 0.0F))
                    {
                        if (fC3x6jk == 1.0F)
                            pthisSV.fQwCT6x3[i][j] += pthisSV.fQw6x6[i][k];
                        else
                            pthisSV.fQwCT6x3[i][j] += pthisSV.fQw6x6[i][k] * fC3x6jk;
                    }
                }
            }
        }

        // set symmetric ftmpA3x3 = C.(Qw.C^T) + Qv = C.fQwCT6x3 + Qv
        for (i = 0; i < 3; i++) // loop over rows
        {
            for (j = i; j < 3; j++) // loop over on and above diagonal columns
            {
                // zero off diagonal and set diagonal to Qv
                if (i == j)
                    ftmpA3x3[i][j] = pthisSV.fQv;
                else
                    ftmpA3x3[i][j] = 0.0F;
                // accumulate matrix sum
                for (k = 0; k < 6; k++)
                {
                    // determine fC3x6[i][k]
                    fC3x6ik = 0.0F;
                    if (k == i) fC3x6ik = 1.0F;
                    if (k == (i + 3)) fC3x6ik = -pthisSV.fAlphaOver2;

                    // accumulate ftmpA3x3[i][j] += C[i][k] & fQwCT6x3[k][j]
                    if ((fC3x6ik != 0.0F) && (pthisSV.fQwCT6x3[k][j] != 0.0F))
                    {
                        if (fC3x6ik == 1.0F)
                            ftmpA3x3[i][j] += pthisSV.fQwCT6x3[k][j];
                        else
                            ftmpA3x3[i][j] += fC3x6ik * pthisSV.fQwCT6x3[k][j];
                    }
                }
            }
        }
        // set ftmpA3x3 below diagonal elements to above diagonal elements
        ftmpA3x3[1][0] = ftmpA3x3[0][1];
        ftmpA3x3[2][0] = ftmpA3x3[0][2];
        ftmpA3x3[2][1] = ftmpA3x3[1][2];

        // invert ftmpA3x3 in situ to give ftmpA3x3 = inv(C * Qw * C^T + Qv) = inv(ftmpA3x3)
        //for (i = 0; i < 3; i++)
            pfRows = ftmpA3x3;
        Matrix.fmatrixAeqInvA(pfRows, iColInd, iRowInd, iPivot, 3, ierror);

        // on successful inversion set Kalman gain matrix fK6x3 = Qw * C^T * inv(C * Qw * C^T + Qv) = fQwCT6x3 * ftmpA3x3
        if (!ierror)
        {
            // normal case
            for (i = 0; i < 6; i++) // loop over rows
            {
                for (j = 0; j < 3; j++) // loop over columns
                {
                    pthisSV.fK6x3[i][j] = 0.0F;
                    for (k = 0; k < 3; k++)
                    {
                        if ((pthisSV.fQwCT6x3[i][k] != 0.0F) && (ftmpA3x3[k][j] != 0.0F))
                        {
                            pthisSV.fK6x3[i][j] += pthisSV.fQwCT6x3[i][k] * ftmpA3x3[k][j];
                        }
                    }
                }
            }
        }
        else
        {
            // ftmpA3x3 was singular so set Kalman gain matrix fK6x3 to zero
            for (i = 0; i < 6; i++) // loop over rows
            {
                for (j = 0; j < 3; j++) // loop over columns
                {
                    pthisSV.fK6x3[i][j] = 0.0F;
                }
            }
        }

        // calculate the a posteriori gravity and geomagnetic tilt quaternion errors and gyro offset error vector
        // from the Kalman matrix fK6x3 and from the measurement error vector fZErr.
        for (i = CHX; i <= CHZ; i++)
        {
            // gravity tilt vector error component
            if (pthisSV.fK6x3[i][CHX] != 0.0F)
                pthisSV.fqgErrPl[i] = pthisSV.fK6x3[i][CHX] * pthisSV.fZErr[CHX];
            else
                pthisSV.fqgErrPl[i] = 0.0F;
            if (pthisSV.fK6x3[i][CHY] != 0.0F) pthisSV.fqgErrPl[i] += pthisSV.fK6x3[i][CHY] * pthisSV.fZErr[CHY];
            if (pthisSV.fK6x3[i][CHZ] != 0.0F) pthisSV.fqgErrPl[i] += pthisSV.fK6x3[i][CHZ] * pthisSV.fZErr[CHZ];

            // gyro offset vector error component
            if (pthisSV.fK6x3[i + 3][CHX] != 0.0F)
                pthisSV.fbErrPl[i] = pthisSV.fK6x3[i + 3][CHX] * pthisSV.fZErr[CHX];
            else
                pthisSV.fbErrPl[i] = 0.0F;
            if (pthisSV.fK6x3[i + 3][CHY] != 0.0F) pthisSV.fbErrPl[i] += pthisSV.fK6x3[i + 3][CHY] * pthisSV.fZErr[CHY];
            if (pthisSV.fK6x3[i + 3][CHZ] != 0.0F) pthisSV.fbErrPl[i] += pthisSV.fK6x3[i + 3][CHZ] * pthisSV.fZErr[CHZ];
        }

        // set ftmpq to the gravity tilt correction (conjugate) quaternion
        ftmpq.q1 = -pthisSV.fqgErrPl[CHX];
        ftmpq.q2 = -pthisSV.fqgErrPl[CHY];
        ftmpq.q3 = -pthisSV.fqgErrPl[CHZ];
        ftmp = ftmpq.q1 * ftmpq.q1 + ftmpq.q2 * ftmpq.q2 + ftmpq.q3 * ftmpq.q3;
        // determine the scalar component q0
        if (ftmp <= 1.0F)
        {
            // normal case
            ftmpq.q0 = (float) Math.sqrt(Math.abs(1.0F - ftmp));
        }
        else
        {
            // if vector component exceeds unity then rounding errors are present at 180 deg rotation
            // so set scalar component to zero for 180 deg rotation about the rotation vector
            ftmpq.q0 = 0.0F;
        }
        // apply the gravity tilt correction quaternion so fqPl = fqMi.(fqgErrPl)* = fqMi.ftmpq and normalize
        Orientation.qAeqBxC(pthisSV.fqPl, fqMi, ftmpq);

        // normalize the a posteriori quaternion and compute the a posteriori rotation matrix and rotation vector
        Orientation.fqAeqNormqA(pthisSV.fqPl);
        Orientation.fRotationMatrixFromQuaternion(pthisSV.fRPl, pthisSV.fqPl);
        Orientation.fRotationVectorDegFromQuaternion(pthisSV.fqPl, pthisSV.fRVecPl);

        // update the a posteriori gyro offset vector: b+[k] = b-[k] - be+[k] = b+[k] - be+[k] (deg/s)
        for (i = CHX; i <= CHZ; i++)
            pthisSV.fbPl[i] -= pthisSV.fbErrPl[i];

        // compute the linear acceleration in the global frame
        // de-rotate the accelerometer from the sensor to global frame using the transpose (inverse) of the orientation matrix
        pthisSV.fAccGl[CHX] = pthisSV.fRPl[CHX][CHX] * pthisAccel.fGsAvg[CHX] + pthisSV.fRPl[CHY][CHX] * pthisAccel.fGsAvg[CHY] +
                pthisSV.fRPl[CHZ][CHX] * pthisAccel.fGsAvg[CHZ];
        pthisSV.fAccGl[CHY] = pthisSV.fRPl[CHX][CHY] * pthisAccel.fGsAvg[CHX] + pthisSV.fRPl[CHY][CHY] * pthisAccel.fGsAvg[CHY] +
                pthisSV.fRPl[CHZ][CHY] * pthisAccel.fGsAvg[CHZ];
        pthisSV.fAccGl[CHZ] = pthisSV.fRPl[CHX][CHZ] * pthisAccel.fGsAvg[CHX] + pthisSV.fRPl[CHY][CHZ] * pthisAccel.fGsAvg[CHY] +
                pthisSV.fRPl[CHZ][CHZ] * pthisAccel.fGsAvg[CHZ];
        // sutract the fixed gravity vector in the global frame leaving linear acceleration
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                // gravity positive NED
                pthisSV.fAccGl[CHX] = -pthisSV.fAccGl[CHX];
                pthisSV.fAccGl[CHY] = -pthisSV.fAccGl[CHY];
                pthisSV.fAccGl[CHZ] = -(pthisSV.fAccGl[CHZ] - 1.0F);
                break;
            case ANDROID:
                // acceleration positive ENU
                pthisSV.fAccGl[CHZ] = pthisSV.fAccGl[CHZ] - 1.0F;
                break;
            case WIN8:
            default:
                // gravity positive ENU
                pthisSV.fAccGl[CHX] = -pthisSV.fAccGl[CHX];
                pthisSV.fAccGl[CHY] = -pthisSV.fAccGl[CHY];
                pthisSV.fAccGl[CHZ] = -(pthisSV.fAccGl[CHZ] + 1.0F);
                break;
        }*/
        // acceleration positive ENU
        pthisSV.fAccGl[CHZ] = pthisSV.fAccGl[CHZ] - 1.0F;
        // compute the a posteriori Euler angles from the a posteriori orientation matrix fRPl
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                fNEDAnglesDegFromRotationMatrix(pthisSV.fRPl, &(pthisSV.fPhiPl), &(pthisSV.fThePl), &(pthisSV.fPsiPl), &(pthisSV.fRhoPl), &(pthisSV.fChiPl));
                break;
            case ANDROID:
                fAndroidAnglesDegFromRotationMatrix(pthisSV.fRPl, &(pthisSV.fPhiPl), &(pthisSV.fThePl), &(pthisSV.fPsiPl), &(pthisSV.fRhoPl), &(pthisSV.fChiPl));
                break;
            case WIN8:
            default:
                fWin8AnglesDegFromRotationMatrix(pthisSV.fRPl, &(pthisSV.fPhiPl), &(pthisSV.fThePl), &(pthisSV.fPsiPl), &(pthisSV.fRhoPl), &(pthisSV.fChiPl));
                break;
        }*/
        Orientation.fAndroidAnglesDegFromRotationMatrix(pthisSV.fRPl,pthisSV.fPhiPl, pthisSV.fThePl, pthisSV.fPsiPl,
                pthisSV.fRhoPl, pthisSV.fChiPl);

        //return;
    } // end fRun_6DOF_GY_KALMAN
    // 9DOF accelerometer+magnetometer+gyroscope orientation function implemented using indirect complementary Kalman filter
    static void fRun_9DOF_GBY_KALMAN(SV_9DOF_GBY_KALMAN pthisSV, AccelSensor pthisAccel, MagSensor pthisMag,
                              GyroSensor pthisGyro, MagCalibration pthisMagCal)
    {
        // local scalars and arrays
        float ftmpA7x7[][] = new float[7][7];				// scratch 7x7 matrix
        float fRMi[][] = new float[3][3];					// a priori orientation matrix
        float fR6DOF[][] = new float[3][3];					// eCompass (6DOF accelerometer+magnetometer) orientation matrix
        float ftmpMi3x1[] = new float[3];					// temporary vector used for a priori calculations
        float ftmp6DOF3x1[] = new float[3];				// temporary vector used for 6DOF calculations
        float ftmpA3x1[] = new float[3];					// scratch vector
        float fQvGQa = 0;						// accelerometer noise covariance to 1g sphere
        float fQvBQd = 0;						// magnetometer noise covariance to geomagnetic sphere
        float fC7x10ik;						// element i, k of measurement matrix C
        float fC7x10jk;						// element j, k of measurement matrix C
        Fquaternion fqMi;			// a priori orientation quaternion
        Fquaternion fq6DOF = new Fquaternion();			// eCompass (6DOF accelerometer+magnetometer) orientation quaternion
        Fquaternion ftmpq = new Fquaternion();			// scratch quaternion
        float fDelta6DOF = 0;					// inclination angle from accelerometer and magnetometer
        float ftmp;							// scratch float
        boolean ierror = false;						// matrix inversion error flag
        int i, j, k;						// loop counters

        // working arrays for 7x7 matrix inversion
        //float pfRows[] = new float[7];
        int iColInd[] = new int[7];
        int iRowInd[] = new int[7];
        int iPivot[] = new int[7];

        // if requested, do a reset initialization with no further processing
        if (pthisSV.resetflag)
        {
            fInit_9DOF_GBY_KALMAN(pthisSV, pthisAccel, pthisMag, pthisMagCal);
            return;
        }
        if(D) Log.d(TAG,"fRun_9DOF_GBY_KALMAN");
        // compute the a priori orientation quaternion fqMi by integrating the buffered gyro measurements
        fqMi = pthisSV.fqPl;
        // loop over all the buffered gyroscope measurements
        for (j = 0; j < OVERSAMPLE_RATIO; j++)
        {
            // calculate the instantaneous angular velocity (after subtracting the gyro offset) and rotation vector ftmpMi3x1
            for (i = CHX; i <= CHZ; i++)
            {
                //pthisSV.fOmega[i] = (float)pthisGyro.iYsBuffer[j][i] * pthisGyro.fDegPerSecPerCount - pthisSV.fbPl[i];
                pthisSV.fOmega[i] = (float)pthisGyro.fYsBuffer[j][i] - pthisSV.fbPl[i];
                //ftmpMi3x1[i] = pthisSV.fOmega[i] * pthisSV.fGyrodeltat;
                ftmpMi3x1[i] = pthisSV.fOmega[i]; //* pthisSV.fGyrodeltat
            }
            // convert the rotation vector ftmpMi3x1 to a rotation quaternion ftmpq
            Orientation.fQuaternionFromRotationVectorDeg(ftmpq, ftmpMi3x1, 1.0F);
            // integrate the gyro sensor incremental quaternions into the a priori orientation quaternion fqMi
            Orientation.qAeqAxB(fqMi, ftmpq);
        }
        // compute the a priori orientation matrix from the a priori quaternion.
        Orientation.fRotationMatrixFromQuaternion(fRMi, fqMi);

        // compute the 6DOF least squares orientation quaternion fq6DOF, matrix fR6DOF and inclination angle fDelta6DOF and
        // the squared deviations of the accelerometer and magnetometer measurements from the 1g gravity and geomagnetic spheres.
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                fLeastSquareseCompassNED(&fq6DOF, pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl, pthisSV.fcosDeltaPl,
                &fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, &fQvBQd, &fQvGQa);
                break;
            case ANDROID:
                fLeastSquareseCompassAndroid(&fq6DOF, pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl, pthisSV.fcosDeltaPl,
                &fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, &fQvBQd, &fQvGQa);
                break;
            case WIN8:
            default:
                fLeastSquareseCompassWin8(&fq6DOF, pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl, pthisSV.fcosDeltaPl,
                &fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, &fQvBQd, &fQvGQa);
                break;
        }*/
        Orientation.fLeastSquareseCompassAndroid(fq6DOF, pthisMagCal.fB, pthisSV.fDeltaPl, pthisSV.fsinDeltaPl, pthisSV.fcosDeltaPl,
                                            fDelta6DOF, pthisMag.fBcAvg, pthisAccel.fGsAvg, fQvBQd, fQvGQa);
        // compute the 6DOF orientation matrix from the 6DOF quaternion.
        Orientation.fRotationMatrixFromQuaternion(fR6DOF, fq6DOF);

        // do a once-only orientation lock immediately after the first valid magnetic calibration by setting the
        // a priori and a posteriori orientation to the 6DOF eCompass orientation.
        // also initialize the geomagnetic inclination angle Delta now that a magnetic calibration has been achieved and a
        // reasonable estimate is available for the first time.
        if ((pthisMagCal.iValidMagCal != 0) && !pthisSV.iFirstAccelMagLock)
        {
            fqMi = pthisSV.fqPl = fq6DOF;
            Matrix.f3x3matrixAeqB(fRMi, fR6DOF);
            pthisSV.fDeltaPl = fDelta6DOF;
            pthisSV.fsinDeltaPl = (float) Math.sin(pthisSV.fDeltaPl * FPIOVER180);
            pthisSV.fcosDeltaPl = (float) Math.sqrt(1.0F - pthisSV.fsinDeltaPl * pthisSV.fsinDeltaPl);
            pthisSV.iFirstAccelMagLock = true;
        }

        // set ftmp6DOF3x1 to the 6DOF gravity vector in the sensor frame (ignoring the minus sign for Android and Windows 8)
        ftmp6DOF3x1[CHX] = fR6DOF[CHX][CHZ];
        ftmp6DOF3x1[CHY] = fR6DOF[CHY][CHZ];
        ftmp6DOF3x1[CHZ] = fR6DOF[CHZ][CHZ];
        // set ftmpMi3x1 to the a priori gravity vector in the sensor frame (ignoring the minus sign for Android and Windows 8)
        ftmpMi3x1[CHX] = fRMi[CHX][CHZ];
        ftmpMi3x1[CHY] = fRMi[CHY][CHZ];
        ftmpMi3x1[CHZ] = fRMi[CHZ][CHZ];
        // set ftmpq to the quaternion that rotates the 6DOF gravity tilt vector to the a priori gravity tilt vector
        // and copy the vector components to the measurement error vector fZErr. the ignored minus signs cancel here.
        Orientation.fveqconjgquq(ftmpq, ftmp6DOF3x1, ftmpMi3x1);
        pthisSV.fZErr[0] = ftmpq.q1;
        pthisSV.fZErr[1] = ftmpq.q2;
        pthisSV.fZErr[2] = ftmpq.q3;

        // determine the normalized 6DOF and a priori geomagnetic vectors in the sensor frame.
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                // set ftmp6DOF3x1 to the normalized 6DOF geomagnetic vector in the sensor frame
                ftmp6DOF3x1[CHX] = fR6DOF[CHX][CHX] * pthisSV.fcosDeltaPl + fR6DOF[CHX][CHZ] * pthisSV.fsinDeltaPl;
                ftmp6DOF3x1[CHY] = fR6DOF[CHY][CHX] * pthisSV.fcosDeltaPl + fR6DOF[CHY][CHZ] * pthisSV.fsinDeltaPl;
                ftmp6DOF3x1[CHZ] = fR6DOF[CHZ][CHX] * pthisSV.fcosDeltaPl + fR6DOF[CHZ][CHZ] * pthisSV.fsinDeltaPl;
                // set ftmpMi3x1 to the normalized a priori geomagnetic vector in the sensor frame
                ftmpMi3x1[CHX] = fRMi[CHX][CHX] * pthisSV.fcosDeltaPl + fRMi[CHX][CHZ] * pthisSV.fsinDeltaPl;
                ftmpMi3x1[CHY] = fRMi[CHY][CHX] * pthisSV.fcosDeltaPl + fRMi[CHY][CHZ] * pthisSV.fsinDeltaPl;
                ftmpMi3x1[CHZ] = fRMi[CHZ][CHX] * pthisSV.fcosDeltaPl + fRMi[CHZ][CHZ] * pthisSV.fsinDeltaPl;
                break;
            case ANDROID:
            case WIN8:
            default:
                // set ftmp6DOF3x1 to the 6DOF geomagnetic vector in the sensor frame
                ftmp6DOF3x1[CHX] = fR6DOF[CHX][CHY] * pthisSV.fcosDeltaPl - fR6DOF[CHX][CHZ] * pthisSV.fsinDeltaPl;
                ftmp6DOF3x1[CHY] = fR6DOF[CHY][CHY] * pthisSV.fcosDeltaPl - fR6DOF[CHY][CHZ] * pthisSV.fsinDeltaPl;
                ftmp6DOF3x1[CHZ] = fR6DOF[CHZ][CHY] * pthisSV.fcosDeltaPl - fR6DOF[CHZ][CHZ] * pthisSV.fsinDeltaPl;
                // set ftmpMi3x1 to the a priori geomagnetic vector in the sensor frame
                ftmpMi3x1[CHX] = fRMi[CHX][CHY] * pthisSV.fcosDeltaPl - fRMi[CHX][CHZ] * pthisSV.fsinDeltaPl;
                ftmpMi3x1[CHY] = fRMi[CHY][CHY] * pthisSV.fcosDeltaPl - fRMi[CHY][CHZ] * pthisSV.fsinDeltaPl;
                ftmpMi3x1[CHZ] = fRMi[CHZ][CHY] * pthisSV.fcosDeltaPl - fRMi[CHZ][CHZ] * pthisSV.fsinDeltaPl;
                break;
        }*/
        // set ftmp6DOF3x1 to the 6DOF geomagnetic vector in the sensor frame
        ftmp6DOF3x1[CHX] = fR6DOF[CHX][CHY] * pthisSV.fcosDeltaPl - fR6DOF[CHX][CHZ] * pthisSV.fsinDeltaPl;
        ftmp6DOF3x1[CHY] = fR6DOF[CHY][CHY] * pthisSV.fcosDeltaPl - fR6DOF[CHY][CHZ] * pthisSV.fsinDeltaPl;
        ftmp6DOF3x1[CHZ] = fR6DOF[CHZ][CHY] * pthisSV.fcosDeltaPl - fR6DOF[CHZ][CHZ] * pthisSV.fsinDeltaPl;
        // set ftmpMi3x1 to the a priori geomagnetic vector in the sensor frame
        ftmpMi3x1[CHX] = fRMi[CHX][CHY] * pthisSV.fcosDeltaPl - fRMi[CHX][CHZ] * pthisSV.fsinDeltaPl;
        ftmpMi3x1[CHY] = fRMi[CHY][CHY] * pthisSV.fcosDeltaPl - fRMi[CHY][CHZ] * pthisSV.fsinDeltaPl;
        ftmpMi3x1[CHZ] = fRMi[CHZ][CHY] * pthisSV.fcosDeltaPl - fRMi[CHZ][CHZ] * pthisSV.fsinDeltaPl;

        // set ftmpq to the quaternion that rotates the 6DOF geomagnetic tilt vector to the a priori geomagnetic tilt vector
        // limit to 60 deg rotation and copy the vector components to the measurement error vector fZErr
        Orientation.fveqconjgquq(ftmpq, ftmp6DOF3x1, ftmpMi3x1);
        pthisSV.fZErr[3] = ftmpq.q1;
        pthisSV.fZErr[4] = ftmpq.q2;
        pthisSV.fZErr[5] = ftmpq.q3;

        // calculate the angle error between a priori (equal to a posteriori) and 6DOF estimates of the geomagnetic inclination
        // and copy into the last element of the measurement error vector fZErr
        pthisSV.fZErr[6] = pthisSV.fDeltaPl - fDelta6DOF;

        // update Qw using the a posteriori error vectors from the previous iteration.
        // as Qv increases or Qw decreases, K . 0 and the Kalman filter is weighted towards the a priori prediction
        // as Qv decreases or Qw increases, KC . I and the Kalman filter is weighted towards the measurement.
        // initialize on and above diagonal elements of Qw to zero
        for (i = 0; i < 10; i++)
            for (j = i; j < 10; j++)
                pthisSV.fQw10x10[i][j] = 0.0F;
        // partial diagonal gyro offset terms
        pthisSV.fQw10x10[6][6] = pthisSV.fbErrPl[CHX] * pthisSV.fbErrPl[CHX];
        pthisSV.fQw10x10[7][7] = pthisSV.fbErrPl[CHY] * pthisSV.fbErrPl[CHY];
        pthisSV.fQw10x10[8][8] = pthisSV.fbErrPl[CHZ] * pthisSV.fbErrPl[CHZ];
        // set ftmpA3x1 to (alpha/2)^2 * fbErrPl.fbErrPl
        ftmpA3x1[0] = pthisSV.fAlphaOver2Sq * pthisSV.fQw10x10[6][6];
        ftmpA3x1[1] = pthisSV.fAlphaOver2Sq * pthisSV.fQw10x10[7][7];
        ftmpA3x1[2] = pthisSV.fAlphaOver2Sq * pthisSV.fQw10x10[8][8];
        // diagonal gravity vector components
        pthisSV.fQw10x10[0][0] = pthisSV.fqgErrPl[CHX] * pthisSV.fqgErrPl[CHX] + pthisSV.fAlphaOver2SqQvYQwb + ftmpA3x1[0];
        pthisSV.fQw10x10[1][1] = pthisSV.fqgErrPl[CHY] * pthisSV.fqgErrPl[CHY] + pthisSV.fAlphaOver2SqQvYQwb + ftmpA3x1[1];
        pthisSV.fQw10x10[2][2] = pthisSV.fqgErrPl[CHZ] * pthisSV.fqgErrPl[CHZ] + pthisSV.fAlphaOver2SqQvYQwb + ftmpA3x1[2];
        // diagonal geomagnetic vector components
        pthisSV.fQw10x10[3][3] = pthisSV.fqmErrPl[CHX] * pthisSV.fqmErrPl[CHX] + pthisSV.fAlphaOver2SqQvYQwb + ftmpA3x1[0];
        pthisSV.fQw10x10[4][4] = pthisSV.fqmErrPl[CHY] * pthisSV.fqmErrPl[CHY] + pthisSV.fAlphaOver2SqQvYQwb + ftmpA3x1[1];
        pthisSV.fQw10x10[5][5] = pthisSV.fqmErrPl[CHZ] * pthisSV.fqmErrPl[CHZ] + pthisSV.fAlphaOver2SqQvYQwb + ftmpA3x1[2];
        // diagonal gyro offset components
        pthisSV.fQw10x10[6][6] += FQWB_9DOF_GBY_KALMAN;
        pthisSV.fQw10x10[7][7] += FQWB_9DOF_GBY_KALMAN;
        pthisSV.fQw10x10[8][8] += FQWB_9DOF_GBY_KALMAN;
        // diagonal geomagnetic inclination angle
        pthisSV.fQw10x10[9][9] = FQWDLT_9DOF_GBY_KALMAN + pthisSV.fDeltaErrPl * pthisSV.fDeltaErrPl;
        // off diagonal gravity and gyro offset components
        pthisSV.fQw10x10[0][6] = pthisSV.fqgErrPl[CHX] * pthisSV.fbErrPl[CHX] - pthisSV.fAlphaOver2Qwb;
        pthisSV.fQw10x10[1][7] = pthisSV.fqgErrPl[CHY] * pthisSV.fbErrPl[CHY] - pthisSV.fAlphaOver2Qwb;
        pthisSV.fQw10x10[2][8] = pthisSV.fqgErrPl[CHZ] * pthisSV.fbErrPl[CHZ] - pthisSV.fAlphaOver2Qwb;
        // off diagonal geomagnetic and gyro offset components
        pthisSV.fQw10x10[3][6] = pthisSV.fqmErrPl[CHX] * pthisSV.fbErrPl[CHX] - pthisSV.fAlphaOver2Qwb;
        pthisSV.fQw10x10[4][7] = pthisSV.fqmErrPl[CHY] * pthisSV.fbErrPl[CHY] - pthisSV.fAlphaOver2Qwb;
        pthisSV.fQw10x10[5][8] = pthisSV.fqmErrPl[CHZ] * pthisSV.fbErrPl[CHZ] - pthisSV.fAlphaOver2Qwb;
        // set below diagonal elements of Qw to above diagonal elements
        for (i = 1; i < 10; i++)
            for (j = 0; j < i; j++)
                pthisSV.fQw10x10[i][j] = pthisSV.fQw10x10[j][i];

        // calculate the vector fQv7x1 containing the diagonal elements of the measurement covariance matrix Qv
        if (fQvBQd < FQVBQD_MIN_9DOF_GBY_KALMAN) fQvBQd = FQVBQD_MIN_9DOF_GBY_KALMAN;
        ftmp = fQvBQd / (pthisMagCal.fB * pthisMagCal.fB);
        pthisSV.fQv7x1[0] = pthisSV.fQv7x1[1] = pthisSV.fQv7x1[2] = 0.25F * fQvGQa + pthisSV.fAlphaOver2SqQvYQwb;
        pthisSV.fQv7x1[3] = pthisSV.fQv7x1[4] = pthisSV.fQv7x1[5] = 0.25F * ftmp + pthisSV.fAlphaOver2SqQvYQwb;
        pthisSV.fQv7x1[6] = (fQvGQa + ftmp) * F180OVERPISQ;

        // calculate the Kalman gain matrix K = Qw * C^T * inv(C * Qw * C^T + Qv)
        // set fQwCT10x7 = Qw.C^T where Qw has size 10x10 and C^T has size 10x7
        for (i = 0; i < 10; i++) // loop over rows
        {
            for (j = 0; j < 7; j++) // loop over columns
            {
                pthisSV.fQwCT10x7[i][j] = 0.0F;
                // accumulate matrix sum
                for (k = 0; k < 10; k++)
                {
                    // determine fC7x10[j][k] since the matrix is highly sparse
                    fC7x10jk = 0.0F;
                    // handle rows 0 to 2
                    if (j < 3)
                    {
                        if (k == j) fC7x10jk = 1.0F;
                        if (k == (j + 6)) fC7x10jk = -pthisSV.fAlphaOver2;
                    }
                    // handle rows 3 to 5
                    else if (j < 6)
                    {
                        if (k == j) fC7x10jk = 1.0F;
                        if (k == (j + 3)) fC7x10jk = -pthisSV.fAlphaOver2;
                    }
                    // handle last row 6
                    else if (k == 9) fC7x10jk = 1.0F;

                    // accumulate fQwCT10x7[i][j] += Qw10x10[i][k] * C[j][k]
                    if ((pthisSV.fQw10x10[i][k] != 0.0F) && (fC7x10jk != 0.0F))
                    {
                        if (fC7x10jk == 1.0F)
                            pthisSV.fQwCT10x7[i][j] += pthisSV.fQw10x10[i][k];
                        else
                            pthisSV.fQwCT10x7[i][j] += pthisSV.fQw10x10[i][k] * fC7x10jk;
                    }
                }
            }
        }

        // set symmetric ftmpA7x7 = C.(Qw.C^T) + Qv = C.fQwCT10x7 + Qv
        for (i = 0; i < 7; i++) // loop over rows
        {
            for (j = i; j < 7; j++) // loop over on and above diagonal columns
            {
                // zero off diagonal and set diagonal to Qv
                if (i == j)
                    ftmpA7x7[i][j] = pthisSV.fQv7x1[i];
                else
                    ftmpA7x7[i][j] = 0.0F;
                // accumulate matrix sum
                for (k = 0; k < 10; k++)
                {
                    // determine fC7x10[i][k]
                    fC7x10ik = 0.0F;
                    // handle rows 0 to 2
                    if (i < 3)
                    {
                        if (k == i) fC7x10ik = 1.0F;
                        if (k == (i + 6)) fC7x10ik = -pthisSV.fAlphaOver2;
                    }
                    // handle rows 3 to 5
                    else if (i < 6)
                    {
                        if (k == i) fC7x10ik = 1.0F;
                        if (k == (i + 3)) fC7x10ik = -pthisSV.fAlphaOver2;
                    }
                    // handle last row 6
                    else if (k == 9) fC7x10ik = 1.0F;

                    // accumulate ftmpA7x7[i][j] += C[i][k] & fQwCT10x7[k][j]
                    if ((fC7x10ik != 0.0F) && (pthisSV.fQwCT10x7[k][j] != 0.0F))
                    {
                        if (fC7x10ik == 1.0F)
                            ftmpA7x7[i][j] += pthisSV.fQwCT10x7[k][j];
                        else
                            ftmpA7x7[i][j] += fC7x10ik * pthisSV.fQwCT10x7[k][j];
                    }
                }
            }
        }
        // set ftmpA7x7 below diagonal elements to above diagonal elements
        for (i = 1; i < 7; i++) // loop over rows
        {
            for (j = 0; j < i; j++) // loop over below diagonal columns
            {
                ftmpA7x7[i][j] = ftmpA7x7[j][i];
            }
        }

        // invert ftmpA7x7 in situ to give ftmpA7x7 = inv(C * Qw * C^T + Qv) = inv(ftmpA7x7)
/*        for (i = 0; i < 7; i++)
            pfRows[i] = ftmpA7x7[i];*/
        Matrix.fmatrixAeqInvA(ftmpA7x7, iColInd, iRowInd, iPivot, 7, ierror);

        // on successful inversion set Kalman gain matrix K10x7 = Qw * C^T * inv(C * Qw * C^T + Qv) = fQwCT10x7 * ftmpA7x7
        if (!ierror)
        {
            // normal case
            for (i = 0; i < 10; i++) // loop over rows
            {
                for (j = 0; j < 7; j++) // loop over columns
                {
                    pthisSV.fK10x7[i][j] = 0.0F;
                    for (k = 0; k < 7; k++)
                    {
                        if ((pthisSV.fQwCT10x7[i][k] != 0.0F) && (ftmpA7x7[k][j] != 0.0F))
                        {
                            pthisSV.fK10x7[i][j] += pthisSV.fQwCT10x7[i][k] * ftmpA7x7[k][j];
                        }
                    }
                }
            }
        }
        else
        {
            // ftmpA7x7 was singular so set Kalman gain matrix to zero
            for (i = 0; i < 10; i++) // loop over rows
            {
                for (j = 0; j < 7; j++) // loop over columns
                {
                    pthisSV.fK10x7[i][j] = 0.0F;
                }
            }
        }

        // calculate the a posteriori gravity and geomagnetic tilt quaternion errors and gyro offset error vector
        // from the Kalman matrix fK10x7 and from the measurement error vector fZErr.
        for (i = CHX; i <= CHZ; i++)
        {
            pthisSV.fqgErrPl[i] = pthisSV.fqmErrPl[i] = pthisSV.fbErrPl[i] = 0.0F;
            for (j = 0; j < 7; j++)
            {
                // gravity tilt quaternion vector error component fqgErrPl
                if (pthisSV.fK10x7[i][j] != 0.0F)
                    pthisSV.fqgErrPl[i] += pthisSV.fK10x7[i][j] * pthisSV.fZErr[j];

                // geomagnetic tilt quaternion vector error component fqmErrPl
                if (pthisSV.fK10x7[i + 3][j] != 0.0F)
                    pthisSV.fqmErrPl[i] += pthisSV.fK10x7[i + 3][j] * pthisSV.fZErr[j];

                // gyro offset vector error component fbErrPl
                if (pthisSV.fK10x7[i + 6][j] != 0.0F)
                    pthisSV.fbErrPl[i] += pthisSV.fK10x7[i + 6][j] * pthisSV.fZErr[j];
            }
        }

        // calculate the a posteriori geomagnetic inclination angle error estimate fDeltaErrPl
        pthisSV.fDeltaErrPl = 0.0F;
        for (j = 0; j < 7; j++)
        {
            if (pthisSV.fK10x7[9][j] != 0.0F)
                pthisSV.fDeltaErrPl += pthisSV.fK10x7[9][j] * pthisSV.fZErr[j];
        }

        // limit a posteriori gravity tilt correction (conjugate) quaternion to 90 deg correction and apply
        ftmp = pthisSV.fqgErrPl[CHX] * pthisSV.fqgErrPl[CHX] + pthisSV.fqgErrPl[CHY] * pthisSV.fqgErrPl[CHY] +
                pthisSV.fqgErrPl[CHZ] * pthisSV.fqgErrPl[CHZ];
        if (ftmp <= 0.5F)
        {
            // normal case with less than 90 deg correction
            ftmpq.q0 = (float) Math.sqrt(Math.abs(1.0F - ftmp));
            ftmpq.q1 = -pthisSV.fqgErrPl[CHX];
            ftmpq.q2 = -pthisSV.fqgErrPl[CHY];
            ftmpq.q3 = -pthisSV.fqgErrPl[CHZ];
        }
        else
        {
            // limit to 90 deg rotation with q0=1/sqrt(2) and quaternion vector magnitude=1/sqrt(2)
            ftmpq.q0 = ONEOVERSQRT2;
            ftmp = ONEOVERSQRT2 / (float) Math.sqrt(Math.abs(ftmp));
            pthisSV.fqgErrPl[CHX] *= ftmp;
            pthisSV.fqgErrPl[CHY] *= ftmp;
            pthisSV.fqgErrPl[CHZ] *= ftmp;
            ftmpq.q1 = -pthisSV.fqgErrPl[CHX];
            ftmpq.q2 = -pthisSV.fqgErrPl[CHY];
            ftmpq.q3 = -pthisSV.fqgErrPl[CHZ];
        }
        // apply the gravity tilt correction quaternion so fqPl = fqMi.(fqgErrPl)*
        Orientation.qAeqBxC(pthisSV.fqPl, fqMi, ftmpq);

        // limit a posteriori geomagnetic tilt correction (conjugate) quaternion to 90 deg correction and apply
        ftmp = pthisSV.fqmErrPl[CHX] * pthisSV.fqmErrPl[CHX] + pthisSV.fqmErrPl[CHY] * pthisSV.fqmErrPl[CHY] +
                pthisSV.fqmErrPl[CHZ] * pthisSV.fqmErrPl[CHZ];
        if (ftmp <= 0.5F)
        {
            // normal case with less than 90 deg correction
            ftmpq.q0 = (float) Math.sqrt(Math.abs(1.0F - ftmp));
            ftmpq.q1 = -pthisSV.fqmErrPl[CHX];
            ftmpq.q2 = -pthisSV.fqmErrPl[CHY];
            ftmpq.q3 = -pthisSV.fqmErrPl[CHZ];
        }
        else
        {
            // limit to 90 deg rotation with q0=1/sqrt(2) and quaternion vector magnitude=1/sqrt(2)
            ftmpq.q0 = ONEOVERSQRT2;
            ftmp = ONEOVERSQRT2 / (float) Math.sqrt(Math.abs(ftmp));
            pthisSV.fqmErrPl[CHX] *= ftmp;
            pthisSV.fqmErrPl[CHY] *= ftmp;
            pthisSV.fqmErrPl[CHZ] *= ftmp;
            ftmpq.q1 = -pthisSV.fqmErrPl[CHX];
            ftmpq.q2 = -pthisSV.fqmErrPl[CHY];
            ftmpq.q3 = -pthisSV.fqmErrPl[CHZ];
        }
        // apply the geomagnetic tilt correction quaternion so fqPl = fqMi.(fqgErrPl)*.(fqmErrPl)*
        Orientation.qAeqAxB(pthisSV.fqPl, ftmpq);

        // normalize the a posteriori quaternion and compute the a posteriori rotation matrix and rotation vector
        Orientation.fqAeqNormqA(pthisSV.fqPl);
        Orientation.fRotationMatrixFromQuaternion(pthisSV.fRPl, pthisSV.fqPl);
        Orientation.fRotationVectorDegFromQuaternion(pthisSV.fqPl, pthisSV.fRVecPl);

        // update the a posteriori gyro offset vector: b+[k] = b-[k] - be+[k] = b+[k] - be+[k] (deg/s)
        // and limit gyro offset vector range as defensive programming.
        for (i = CHX; i <= CHZ; i++)
        {
            pthisSV.fbPl[i] -= pthisSV.fbErrPl[i];
            if (pthisSV.fbPl[i] < FGYRO_OFFSET_MIN_9DOF_GBY_KALMAN) pthisSV.fbPl[i] = FGYRO_OFFSET_MIN_9DOF_GBY_KALMAN;
            if (pthisSV.fbPl[i] > FGYRO_OFFSET_MAX_9DOF_GBY_KALMAN) pthisSV.fbPl[i] = FGYRO_OFFSET_MAX_9DOF_GBY_KALMAN;
        }

        // update a posteriori geomagnetic inclination angle: delta+[k] = delta-[k] - deltae+[k] = delta+[k] - deltae+[k]
        pthisSV.fDeltaPl -= pthisSV.fDeltaErrPl;
        if (pthisSV.fDeltaPl > 90.0F) pthisSV.fDeltaPl = 90.0F;
        else if (pthisSV.fDeltaPl < -90.0F) pthisSV.fDeltaPl = -90.0F;
        pthisSV.fsinDeltaPl = (float) Math.sin(pthisSV.fDeltaPl * FPIOVER180);
        pthisSV.fcosDeltaPl = (float) Math.sqrt(1.0F - pthisSV.fsinDeltaPl * pthisSV.fsinDeltaPl);

        // compute the linear acceleration in the global frame
        // de-rotate the accelerometer from the sensor to global frame using the transpose (inverse) of the orientation matrix
        pthisSV.fAccGl[CHX] = pthisSV.fRPl[CHX][CHX] * pthisAccel.fGsAvg[CHX] + pthisSV.fRPl[CHY][CHX] * pthisAccel.fGsAvg[CHY] +
                pthisSV.fRPl[CHZ][CHX] * pthisAccel.fGsAvg[CHZ];
        pthisSV.fAccGl[CHY] = pthisSV.fRPl[CHX][CHY] * pthisAccel.fGsAvg[CHX] + pthisSV.fRPl[CHY][CHY] * pthisAccel.fGsAvg[CHY] +
                pthisSV.fRPl[CHZ][CHY] * pthisAccel.fGsAvg[CHZ];
        pthisSV.fAccGl[CHZ] = pthisSV.fRPl[CHX][CHZ] * pthisAccel.fGsAvg[CHX] + pthisSV.fRPl[CHY][CHZ] * pthisAccel.fGsAvg[CHY] +
                pthisSV.fRPl[CHZ][CHZ] * pthisAccel.fGsAvg[CHZ];
        // sutract the fixed gravity vector in the global frame leaving linear acceleration
/*        switch (THISCOORDSYSTEM)
        {
            case NED:
                // gravity positive NED
                pthisSV.fAccGl[CHX] = -pthisSV.fAccGl[CHX];
                pthisSV.fAccGl[CHY] = -pthisSV.fAccGl[CHY];
                pthisSV.fAccGl[CHZ] = -(pthisSV.fAccGl[CHZ] - 1.0F);
                break;
            case ANDROID:
                // acceleration positive ENU
                pthisSV.fAccGl[CHZ] = pthisSV.fAccGl[CHZ] - 1.0F;
                break;
            case WIN8:
            default:
                // gravity positive ENU
                pthisSV.fAccGl[CHX] = -pthisSV.fAccGl[CHX];
                pthisSV.fAccGl[CHY] = -pthisSV.fAccGl[CHY];
                pthisSV.fAccGl[CHZ] = -(pthisSV.fAccGl[CHZ] + 1.0F);
                break;
        }*/
        // acceleration positive ENU
        pthisSV.fAccGl[CHZ] = pthisSV.fAccGl[CHZ] - 1.0F;

        // integrate the acceleration to velocity and displacement in the global frame.
        // Note: integration errors accumulate without limit over time and this code should only be
        // used for inertial integration of the order of seconds.
        for (i = CHX; i <= CHZ; i++)
        {
            // integrate acceleration (in g) to velocity in m/s
            pthisSV.fVelGl[i] += pthisSV.fAccGl[i] * pthisSV.fgKalmandeltat;
            // integrate velocity (in m/s) to displacement (m)
            pthisSV.fDisGl[i] += pthisSV.fVelGl[i] * pthisSV.fKalmandeltat;
        }

        // compute the a posteriori Euler angles from the a posteriori orientation matrix fRPl
       /*switch (THISCOORDSYSTEM)
        {
            case NED:
                fNEDAnglesDegFromRotationMatrix(pthisSV.fRPl, &(pthisSV.fPhiPl), &(pthisSV.fThePl), &(pthisSV.fPsiPl), &(pthisSV.fRhoPl), &(pthisSV.fChiPl));
                break;
            case ANDROID:
                fAndroidAnglesDegFromRotationMatrix(pthisSV.fRPl, &(pthisSV.fPhiPl), &(pthisSV.fThePl), &(pthisSV.fPsiPl), &(pthisSV.fRhoPl), &(pthisSV.fChiPl));
                break;
            case WIN8:
            default:
                fWin8AnglesDegFromRotationMatrix(pthisSV.fRPl, &(pthisSV.fPhiPl), &(pthisSV.fThePl), &(pthisSV.fPsiPl), &(pthisSV.fRhoPl), &(pthisSV.fChiPl));
                break;
        }*/

        Orientation.fAndroidAnglesDegFromRotationMatrix(pthisSV.fRPl,pthisSV.fPhiPl,pthisSV.fThePl,pthisSV.fPsiPl,pthisSV.fRhoPl,pthisSV.fChiPl);

        //return;
    } // end fRun_9DOF_GBY_KALMAN





}
