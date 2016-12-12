package com.fusion;

import android.util.Log;

import com.fusion.types.AccelSensor;
import com.fusion.types.GyroSensor;
import com.fusion.types.MagCalibration;
import com.fusion.types.MagSensor;
import com.fusion.types.MagneticBuffer;
import com.fusion.types.ProjectGlobals;
import com.fusion.types.SV_6DOF_GY_KALMAN;
import com.fusion.types.SV_9DOF_GBY_KALMAN;
import com.fusion.types.Types;

/**
 * Created by zozo on 2016/9/28.
 */

public class FusionTask extends Types {

     private boolean D = true;
     private String TAG = "FusionTask";

     public final boolean EnableMag = false;

     private static int iCounter = 0;		// decimation counter range 0 to OVERSAMPLE_RATIO-1

     public ProjectGlobals globals = new ProjectGlobals();
     //public PressureSensor thisPressure;
     public AccelSensor thisAccel = new AccelSensor();

     public GyroSensor thisGyro = new GyroSensor();

     public MagSensor thisMag = new MagSensor();
     public SV_9DOF_GBY_KALMAN thisSV_9DOF_GBY_KALMAN = new SV_9DOF_GBY_KALMAN();
     public MagCalibration thisMagCal = new MagCalibration();
     MagneticBuffer thisMagBuffer = new MagneticBuffer();

     public SV_6DOF_GY_KALMAN thisSV_6DOF_GY_KALMAN = new SV_6DOF_GY_KALMAN();

     public void RdSensData_task_init()
     {
          if(D) Log.d(TAG,"RdSensData_task_init");
          int i;				// counters
          // initialize globals
//          globals.AngularVelocityPacketOn = true;
//          globals.DebugPacketOn = true;
//          globals.RPCPacketOn = true;
//          globals.AltPacketOn = true;

          thisAccel.iCountsPerg = ANDROID_ACC_COUNTSPERG;
          thisAccel.fgPerCount = 1.0F / ANDROID_ACC_COUNTSPERG;

          thisGyro.iCountsPerDegPerSec = ANDROID_GYRO_COUNTSPERDEGPERSEC;
          thisGyro.fDegPerSecPerCount = 1.0F / ANDROID_GYRO_COUNTSPERDEGPERSEC;

          thisMag.iCountsPeruT = ANDROID_MAG_COUNTSPERUT;
          thisMag.fCountsPeruT = (float)ANDROID_MAG_COUNTSPERUT;
          thisMag.fuTPerCount = 1.0F / ANDROID_MAG_COUNTSPERUT;
          // initialize magnetometer data structure
          // zero the calibrated measurement since this is used for indexing the magnetic buffer even before first calibration
          if(EnableMag){
               thisSV_9DOF_GBY_KALMAN.resetflag = true;
               for (i = CHX; i <= CHZ; i++)
                    thisMag.iBcAvg[i]= 0;
          }else {
               thisSV_6DOF_GY_KALMAN.resetflag = true;
          }

     }

     private void RdSensData_task(float g[],float m[],float y[])
     {
          int i,j, k=0,l=0;				// counters
          int iSum[] = new int[3];					// array of sums
          int itmp;						// scratch

          if(D) Log.d(TAG,"RdSensData_task");
          // store measurement in a buffer for later end of block processing
          int gs[] = new int[3];
          for (i = CHX; i <= CHZ; i++) {
               gs[i] = (int) (g[i] * thisAccel.iCountsPerg / GRAVITY);
          }

          thisAccel.iGs = gs;
          for (i = CHX; i <= CHZ; i++)
               thisAccel.iGsBuffer[iCounter][i] = thisAccel.iGs[i];

          // every OVERSAMPLE_RATIO passes calculate the block averaged measurement
          if (iCounter == (OVERSAMPLE_RATIO - 1))
          {
               // calculate the block averaged measurement in counts and g
               for (i = CHX; i <= CHZ; i++)
               {
                    iSum[i] = 0;
                    for (j = 0; j < OVERSAMPLE_RATIO; j++)
                         iSum[i] += thisAccel.iGsBuffer[j][i];
                    // compute the average with nearest integer rounding
                    if (iSum[i] >= 0)
                         thisAccel.iGsAvg[i] = ((iSum[i] + (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);//
                    else
                         thisAccel.iGsAvg[i] = ((iSum[i] + (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO); //
                    // convert from integer counts to float g
                    thisAccel.fGsAvg[i] = (float)thisAccel.iGsAvg[i] * thisAccel.fgPerCount;
               }
          } // end of test for end of OVERSAMPLE_RATIO block

          if(EnableMag){
               // store in a buffer for later end of block processing
               int ms[] = new int[3];
               for (i = CHX; i <= CHZ; i++) {
                    ms[i] = (int) (m[i] * thisMag.iCountsPeruT);
               }
               thisMag.iBs = ms;
               for (i = CHX; i <= CHZ; i++)
                    thisMag.iBsBuffer[iCounter][i] = thisMag.iBs[i];
               // update magnetic buffer with iBs avoiding a write to the shared structure while a calibration is in progress.
               if (!thisMagCal.iCalInProgress)
                    Magnetic.iUpdateMagnetometerBuffer(thisMagBuffer, thisMag, globals.loopcounter);

               // every OVERSAMPLE_RATIO passes calculate the block averaged and calibrated measurement using an anti-glitch filter
               // that rejects the measurement furthest from the mean. magnetometer sensors are sensitive
               // to occasional current pulses from power supply traces and so on and this is a simple method to remove these.
               if (iCounter == (OVERSAMPLE_RATIO - 1))
               {
                    // calculate the channel means using all measurements
                    for (i = CHX; i <= CHZ; i++)
                    {
                         // accumulate channel sums
                         iSum[i] = 0;
                         for (j = 0; j < OVERSAMPLE_RATIO; j++)
                              iSum[i] += thisMag.iBsBuffer[j][i];
                    }
                    // store axis k in buffer measurement l furthest from its mean
                    itmp = 0;
                    for (i = CHX; i <= CHZ; i++)
                    {
                         for (j = 0; j < OVERSAMPLE_RATIO; j++)
                         {
                              if (Math.abs(thisMag.iBsBuffer[j][i] * OVERSAMPLE_RATIO - iSum[i]) >= itmp)
                              {
                                   k = i;
                                   l = j;
                                   itmp = Math.abs(thisMag.iBsBuffer[j][i] * OVERSAMPLE_RATIO - iSum[i]);
                              }
                         }
                    }

                    // re-calculate the block averaged measurement ignoring channel k in measurement l
                    if (OVERSAMPLE_RATIO == 1)
                    {
                         // use the one available measurement for averaging in this case
                         for (i = CHX; i <= CHZ; i++)
                         {
                              thisMag.fBsAvg[i] = thisMag.iBsBuffer[0][i];
                         }
                    } // end of compute averages for OVERSAMPLE_RATIO = 1
                    else
                    {
                         // sum all measurements ignoring channel k in measurement l
                         for (i = CHX; i <= CHZ; i++)
                         {
                              iSum[i] = 0;
                              for (j = 0; j < OVERSAMPLE_RATIO; j++)
                              {
                                   if (!((i == k) && (j == l)))
                                        iSum[i] += thisMag.iBsBuffer[j][i];
                              }
                         }
                         // compute the average with nearest integer rounding
                         for (i = CHX; i <= CHZ; i++)
                         {
                              if (i != k)
                              {
                                   // OVERSAMPLE_RATIO measurements were used
                                   if (iSum[i] >= 0)
                                        thisMag.iBsAvg[i] = ((iSum[i] + (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);
                                   else
                                        thisMag.iBsAvg[i] = ((iSum[i] - (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);
                              }
                              else
                              {
                                   // OVERSAMPLE_RATIO - 1 measurements were used
                                   if (iSum[i] >= 0)
                                        thisMag.iBsAvg[i] = ((iSum[i] + ((OVERSAMPLE_RATIO - 1) >> 1)) / (OVERSAMPLE_RATIO - 1));
                                   else
                                        thisMag.iBsAvg[i] = ((iSum[i] - ((OVERSAMPLE_RATIO - 1) >> 1)) / (OVERSAMPLE_RATIO - 1));
                              }
                         }
                    } // end of compute averages for OVERSAMPLE_RATIO = 1

                    // convert the averages to float
               for (i = CHX; i <= CHZ; i++)
                    thisMag.fBsAvg[i] = (float)thisMag.iBsAvg[i] * thisMag.fuTPerCount;

                    // remove hard and soft iron terms from fBsAvg (uT) to get calibrated data fBcAvg (uT), iBc (counts)
                    Magnetic.fInvertMagCal(thisMag, thisMagCal);

               }
          }


          // store in a buffer for later gyro integration by sensor fusion algorithms
          int ys[] = new int[3];
          for (i = CHX; i <= CHZ; i++){
               ys[i] = (int)(Math.toDegrees(y[i]) * thisGyro.iCountsPerDegPerSec);
          }
          thisGyro.iYs = ys;
          for (i = CHX; i <= CHZ; i++)
               thisGyro.iYsBuffer[iCounter][i] = thisGyro.iYs[i] ;

          // every OVERSAMPLE_RATIO passes zero the decimation counter and enable the sensor fusion task
          if (iCounter++ == (OVERSAMPLE_RATIO - 1))
          {
               iCounter = 0;
               globals.RunKFEventStruct = true;
          }

     }

     public void Fusion_task_init()
     {
          RdSensData_task_init();
          if(EnableMag){
               Magnetic.fInitMagCalibration(thisMagCal, thisMagBuffer);
          }else {

          }
     }

     public boolean Fusion_Task(float g[],float m[],float y[])
     {
          boolean initiatemagcal;				// flag to initiate a new magnetic calibration
          if(D) Log.d(TAG,"Fusion_Task");

          RdSensData_task(g,m,y);

          if(globals.RunKFEventStruct) {

               if(EnableMag){
                    Fusion.fRun_9DOF_GBY_KALMAN(thisSV_9DOF_GBY_KALMAN, thisAccel, thisMag,
                            thisGyro, thisMagCal);
                    // check no magnetic calibration is in progress
                    if (!thisMagCal.iCalInProgress) {
                         if(D) Log.d(TAG,"test4 -- iMagBufferCount"+thisMagBuffer.iMagBufferCount);
                         // do the first 4 element calibration immediately there are a minimum of MINMEASUREMENTS4CAL
                         initiatemagcal = (!thisMagCal.iMagCalHasRun && (thisMagBuffer.iMagBufferCount >= MINMEASUREMENTS4CAL));

                         // otherwise initiate a calibration at intervals depending on the number of measurements available
                         initiatemagcal |= ((thisMagBuffer.iMagBufferCount >= MINMEASUREMENTS4CAL) &&
                                 (thisMagBuffer.iMagBufferCount < MINMEASUREMENTS7CAL) &&
                                 ((globals.loopcounter % INTERVAL4CAL)==0));
                         initiatemagcal |= ((thisMagBuffer.iMagBufferCount >= MINMEASUREMENTS7CAL) &&
                                 (thisMagBuffer.iMagBufferCount < MINMEASUREMENTS10CAL) &&
                                 ((globals.loopcounter % INTERVAL7CAL)==0));
                         initiatemagcal |= ((thisMagBuffer.iMagBufferCount >= MINMEASUREMENTS10CAL) &&
                                 ((globals.loopcounter % INTERVAL10CAL)==0));

                         // initiate the magnetic calibration if any of the conditions are met
                         if (initiatemagcal) {
                              thisMagCal.iCalInProgress = true;
                              thisMagCal.iMagCalHasRun = true;
                              Magnetic.fRunMagCalibration(thisMagCal, thisMagBuffer, thisMag);
                         }

                    } // end of test that no calibration is already in progress
               }else {
                    Fusion.fRun_6DOF_GY_KALMAN(thisSV_6DOF_GY_KALMAN, thisAccel, thisGyro);
               }

               globals.RunKFEventStruct = false;
               globals.loopcounter++;
               return true;
          }
          return false;
     }

}
