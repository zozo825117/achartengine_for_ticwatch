package com.fusion;

import com.fusion.types.AccelSensor;
import com.fusion.types.GyroSensor;
import com.fusion.types.MagCalibration;
import com.fusion.types.MagSensor;
import com.fusion.types.SV_9DOF_GBY_KALMAN;
import com.fusion.types.Types;

/**
 * Created by zozo on 2016/9/28.
 */

public class FusionTask extends Types{

     private static int iSum[] = new int[3];					// array of sums
     private static int itmp;						// scratch
     private static int iCounter = 0;		// decimation counter range 0 to OVERSAMPLE_RATIO-1

     public void RdSensData_task_init()
     {
          int i;				// counters
          // initialize globals
          globals.AngularVelocityPacketOn = true;
          globals.DebugPacketOn = true;
          globals.RPCPacketOn = true;
          globals.AltPacketOn = true;

          // initialize magnetometer data structure
          // zero the calibrated measurement since this is used for indexing the magnetic buffer even before first calibration
          for (i = Types.CHX; i <= Types.CHZ; i++)
               thisMag.iBcAvg[i]= 0;
     }

     public void RdSensData_task(int g[],int m[],int y[])
     {
          int i,j, k=0,l=0;				// counters

          // store measurement in a buffer for later end of block processing
          thisAccel.iGs = g;
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
                         thisAccel.iGsAvg[i] = ((iSum[i] + (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);
                    else
                         thisAccel.iGsAvg[i] = ((iSum[i] - (OVERSAMPLE_RATIO >> 1)) / OVERSAMPLE_RATIO);
                    // convert from integer counts to float g
                    thisAccel.fGsAvg[i] = (float)thisAccel.iGsAvg[i] * thisAccel.fgPerCount;
               }
          } // end of test for end of OVERSAMPLE_RATIO block

          // store in a buffer for later end of block processing
          thisMag.iBs = m;
          for (i = CHX; i <= CHZ; i++)
               thisMag.iBsBuffer[iCounter][i] = thisMag.iBs[i];
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
                         thisMag.iBsAvg[i] = thisMag.iBsBuffer[0][i];
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

          // store in a buffer for later gyro integration by sensor fusion algorithms
          thisGyro.iYs = y;
          for (i = CHX; i <= CHZ; i++)
               thisGyro.iYsBuffer[iCounter][i] = thisGyro.iYs[i];

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
          RdSensData_task_init();
          Fusion.fInit_9DOF_GBY_KALMAN(thisSV_9DOF_GBY_KALMAN, thisAccel,thisMag,thisMagCal);
     }

     public void Fusion_Task(int g[],int m[],int y[])
     {
          RdSensData_task(g,m,y);
          if(globals.RunKFEventStruct)
          {
               Fusion.fRun_9DOF_GBY_KALMAN(thisSV_9DOF_GBY_KALMAN, thisAccel, thisMag,
                       thisGyro, thisMagCal);
               globals.RunKFEventStruct = false;
               globals.loopcounter++;
          }
     }
}
