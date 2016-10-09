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

     private boolean D = false;
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

          // initialize magnetometer data structure
          // zero the calibrated measurement since this is used for indexing the magnetic buffer even before first calibration
          if(EnableMag){
               thisMag.fBcAvg[i]= 0;
          }else {

          }

     }

     private void RdSensData_task(float g[],float m[],float y[])
     {
          int i,j, k=0,l=0;				// counters
          float fSum[] = new float[3];					// array of sums
          float ftmp;						// scratch

          if(D) Log.d(TAG,"RdSensData_task");
          // store measurement in a buffer for later end of block processing
          thisAccel.fGs = g;
          for (i = CHX; i <= CHZ; i++)
               thisAccel.fGsBuffer[iCounter][i] = thisAccel.fGs[i];

          // every OVERSAMPLE_RATIO passes calculate the block averaged measurement
          if (iCounter == (OVERSAMPLE_RATIO - 1))
          {
               // calculate the block averaged measurement in counts and g
               for (i = CHX; i <= CHZ; i++)
               {
                    fSum[i] = 0;
                    for (j = 0; j < OVERSAMPLE_RATIO; j++)
                         fSum[i] += thisAccel.fGsBuffer[j][i];
                    // compute the average with nearest integer rounding
                    if (fSum[i] >= 0)
                         thisAccel.fGsAvg[i] = ((fSum[i] ) / OVERSAMPLE_RATIO);//+ (OVERSAMPLE_RATIO >> 1)
                    else
                         thisAccel.fGsAvg[i] = ((fSum[i]) / OVERSAMPLE_RATIO); //- (OVERSAMPLE_RATIO >> 1)
                    // convert from integer counts to float g
                    //thisAccel.fGsAvg[i] = (float)thisAccel.iGsAvg[i] * thisAccel.fgPerCount;
               }
          } // end of test for end of OVERSAMPLE_RATIO block

          if(EnableMag){
               // store in a buffer for later end of block processing
               thisMag.fBs = m;
               for (i = CHX; i <= CHZ; i++)
                    thisMag.fBsBuffer[iCounter][i] = thisMag.fBs[i];
               // every OVERSAMPLE_RATIO passes calculate the block averaged and calibrated measurement using an anti-glitch filter
               // that rejects the measurement furthest from the mean. magnetometer sensors are sensitive
               // to occasional current pulses from power supply traces and so on and this is a simple method to remove these.
               if (iCounter == (OVERSAMPLE_RATIO - 1))
               {
                    // calculate the channel means using all measurements
                    for (i = CHX; i <= CHZ; i++)
                    {
                         // accumulate channel sums
                         fSum[i] = 0;
                         for (j = 0; j < OVERSAMPLE_RATIO; j++)
                              fSum[i] += thisMag.fBsBuffer[j][i];
                    }
                    // store axis k in buffer measurement l furthest from its mean
                    ftmp = 0;
                    for (i = CHX; i <= CHZ; i++)
                    {
                         for (j = 0; j < OVERSAMPLE_RATIO; j++)
                         {
                              if (Math.abs(thisMag.fBsBuffer[j][i] * OVERSAMPLE_RATIO - fSum[i]) >= ftmp)
                              {
                                   k = i;
                                   l = j;
                                   ftmp = Math.abs(thisMag.fBsBuffer[j][i] * OVERSAMPLE_RATIO - fSum[i]);
                              }
                         }
                    }

                    // re-calculate the block averaged measurement ignoring channel k in measurement l
                    if (OVERSAMPLE_RATIO == 1)
                    {
                         // use the one available measurement for averaging in this case
                         for (i = CHX; i <= CHZ; i++)
                         {
                              thisMag.fBsAvg[i] = thisMag.fBsBuffer[0][i];
                         }
                    } // end of compute averages for OVERSAMPLE_RATIO = 1
                    else
                    {
                         // sum all measurements ignoring channel k in measurement l
                         for (i = CHX; i <= CHZ; i++)
                         {
                              fSum[i] = 0;
                              for (j = 0; j < OVERSAMPLE_RATIO; j++)
                              {
                                   if (!((i == k) && (j == l)))
                                        fSum[i] += thisMag.fBsBuffer[j][i];
                              }
                         }
                         // compute the average with nearest integer rounding
                         for (i = CHX; i <= CHZ; i++)
                         {
                              if (i != k)
                              {
                                   // OVERSAMPLE_RATIO measurements were used
                                   if (fSum[i] >= 0)
                                        thisMag.fBsAvg[i] = ((fSum[i] + (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);
                                   else
                                        thisMag.fBsAvg[i] = ((fSum[i] - (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);
                              }
                              else
                              {
                                   // OVERSAMPLE_RATIO - 1 measurements were used
                                   if (fSum[i] >= 0)
                                        thisMag.fBsAvg[i] = ((fSum[i] + ((OVERSAMPLE_RATIO - 1) >> 1)) / (OVERSAMPLE_RATIO - 1));
                                   else
                                        thisMag.fBsAvg[i] = ((fSum[i] - ((OVERSAMPLE_RATIO - 1) >> 1)) / (OVERSAMPLE_RATIO - 1));
                              }
                         }
                    } // end of compute averages for OVERSAMPLE_RATIO = 1

                    // convert the averages to float
//               for (i = CHX; i <= CHZ; i++)
//                    thisMag.fBsAvg[i] = (float)thisMag.iBsAvg[i] * thisMag.fuTPerCount;

                    // remove hard and soft iron terms from fBsAvg (uT) to get calibrated data fBcAvg (uT), iBc (counts)
                    Magnetic.fInvertMagCal(thisMag, thisMagCal);

               }
          }


          // store in a buffer for later gyro integration by sensor fusion algorithms
          thisGyro.fYs = y;
          for (i = CHX; i <= CHZ; i++)
               thisGyro.fYsBuffer[iCounter][i] = thisGyro.fYs[i];

          // every OVERSAMPLE_RATIO passes zero the decimation counter and enable the sensor fusion task
          if (iCounter++ == (OVERSAMPLE_RATIO - 1))
          {
               iCounter = 0;
               globals.RunKFEventStruct = true;
          }

     }

     public void Fusion_task_init()
     {
          thisSV_9DOF_GBY_KALMAN.resetflag = true;
          thisSV_6DOF_GY_KALMAN.resetflag = true;
          RdSensData_task_init();
          if(EnableMag)
               Magnetic.fInitMagCalibration(thisMagCal, thisMagBuffer);
          //Fusion.fInit_9DOF_GBY_KALMAN(thisSV_9DOF_GBY_KALMAN, thisAccel,thisMag,thisMagCal);
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
