package com.fusion;

import com.fusion.types.MagCalibration;
import com.fusion.types.MagSensor;
import com.fusion.types.Types;

/**
 * Created by zozo on 2016/9/27.
 */

public class Magnetic extends Types{


    // function maps the uncalibrated magnetometer data fBsAvg (uT) onto calibrated averaged data fBcAvg (uT), iBcAvg (counts)
    public static void fInvertMagCal(MagSensor pthisMag, MagCalibration pthisMagCal)
    {
        // local variables
        float ftmp[] = new float[3];					// temporary array
        int i; 						// loop counter

        // check for a valid calibration to reduce clock ticks particularly at power on when the
        // magnetic buffer is being filled as rapidly as possible
        if (pthisMagCal.iValidMagCal)
        {
            // remove the computed hard iron offsets (uT): ftmp[]=fBs[]-V[]
            for (i = CHX; i <= CHZ; i++)
            {
                ftmp[i] = pthisMag.fBsAvg[i] - pthisMagCal.fV[i];
            }
            // remove the computed soft iron offsets (uT and counts): fBc=inv(W)*(fBs[]-V[])
            for (i = CHX; i <= CHZ; i++)
            {
                pthisMag.fBcAvg[i] = pthisMagCal.finvW[i][CHX] * ftmp[CHX] + pthisMagCal.finvW[i][CHY] * ftmp[CHY] + pthisMagCal.finvW[i][CHZ] * ftmp[CHZ];
                pthisMag.iBcAvg[i] = (int) (pthisMag.fBcAvg[i] * pthisMag.fCountsPeruT);
            }
        }
        else
        {
            // simply set the calibrated reading to the uncalibrated reading
            for (i = CHX; i <= CHZ; i++)
            {
                pthisMag.fBcAvg[i] = pthisMag.fBsAvg[i];
                pthisMag.iBcAvg[i] = pthisMag.iBsAvg[i];
            }
        }

        //return;
    }


}
