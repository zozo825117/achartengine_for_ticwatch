package com.fusion;

import com.fusion.types.Fquaternion;
import com.fusion.types.Types;

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
// This file contains functions designed to operate on, or compute, orientations.
// These may be in rotation matrix form, quaternion form, or Euler angles.
// It also includes functions designed to operate with specify reference frames
// (Android, Windows 8, NED).
//

public class Orientation extends Types{


    // compile time constants that are private to this file
    private static final float SMALLQ0 = 0.01F;		// limit of quaternion scalar component requiring special algorithm
    private static final float CORRUPTQUAT = 0.001F;	// threshold for deciding rotation quaternion is corrupt
    private static final float SMALLMODULUS = 0.01F;	// limit where rounding errors may appear

    // Aerospace NED accelerometer 3DOF tilt function computing rotation matrix fR
    static void f3DOFTiltNED(float fR[][], float fGs[])
    {
        // the NED self-consistency twist occurs at 90 deg pitch

        // local variables
        int i;				// counter
        float fmodGxyz;			// modulus of the x, y, z accelerometer readings
        float fmodGyz;			// modulus of the y, z accelerometer readings
        float frecipmodGxyz;	// reciprocal of modulus
        float ftmp;				// scratch variable

        // compute the accelerometer squared magnitudes
        fmodGyz = fGs[CHY] * fGs[CHY] + fGs[CHZ] * fGs[CHZ];
        fmodGxyz = fmodGyz + fGs[CHX] * fGs[CHX];

        // check for freefall special case where no solution is possible
        if (fmodGxyz == 0.0F)
        {
            Matrix.f3x3matrixAeqI(fR);
            return;
        }

        // check for vertical up or down gimbal lock case
        if (fmodGyz == 0.0F)
        {
            Matrix.f3x3matrixAeqScalar(fR, 0.0F);
            fR[CHY][CHY] = 1.0F;
            if (fGs[CHX] >= 0.0F)
            {
                fR[CHX][CHZ] = 1.0F;
                fR[CHZ][CHX] = -1.0F;
            }
            else
            {
                fR[CHX][CHZ] = -1.0F;
                fR[CHZ][CHX] = 1.0F;
            }
            return;
        }

        // compute moduli for the general case
        fmodGyz = (float) Math.sqrt(fmodGyz);
        fmodGxyz = (float) Math.sqrt(fmodGxyz);
        frecipmodGxyz = 1.0F / fmodGxyz;
        ftmp = fmodGxyz / fmodGyz;

        // normalize the accelerometer reading into the z column
        for (i = CHX; i <= CHZ; i++)
        {
            fR[i][CHZ] = fGs[i] * frecipmodGxyz;
        }

        // construct x column of orientation matrix
        fR[CHX][CHX] = fmodGyz * frecipmodGxyz;
        fR[CHY][CHX] = -fR[CHX][CHZ] * fR[CHY][CHZ] * ftmp;
        fR[CHZ][CHX] = -fR[CHX][CHZ] * fR[CHZ][CHZ] * ftmp;

        // construct y column of orientation matrix
        fR[CHX][CHY] = 0.0F;
        fR[CHY][CHY] = fR[CHZ][CHZ] * ftmp;
        fR[CHZ][CHY] = -fR[CHY][CHZ] * ftmp;

        //return;
    }

    // Android accelerometer 3DOF tilt function computing rotation matrix fR
    static void f3DOFTiltAndroid(float fR[][], float fGs[])
    {
        // the Android tilt matrix is mathematically identical to the NED tilt matrix
        // the Android self-consistency twist occurs at 90 deg roll
        f3DOFTiltNED(fR, fGs);
        //return;
    }

    // Android: 6DOF e-Compass function computing least squares fit to orientation quaternion fq
    // on the assumption that the geomagnetic field fB and magnetic inclination angle fDelta are known
    static void fLeastSquareseCompassAndroid(Fquaternion pfq, float fB, float fDelta, float fsinDelta, float fcosDelta,
                                      float pfDelta6DOF, float fBc[], float fGs[], float pfQvBQd, float pfQvGQa)
    {
        // local variables
        float fK[][] = new float[3][3];					// K measurement matrix
        float eigvec[][] = new float[4][4];				// matrix of eigenvectors of K
        float eigval[] = new float[4];				// vector of eigenvalues of K
        float fmodGsSq;					// modulus of fGs[] squared
        float fmodBcSq;					// modulus of fBc[] squared
        float fmodGs;					// modulus of fGs[]
        float fmodBc;					// modulus of fBc[]
        float fGsdotBc;					// scalar product of Gs and Bc
        float fag, fam;					// relative weightings
        float fagOvermodGs;				// a0 / |Gs|
        float famOvermodBc;				// a1 / |Bc|
        float famOvermodBccosDelta;		// a1 / |Bc| * cos(Delta)
        float famOvermodBcsinDelta;		// a1 / |Bc| * sin(Delta)
        float ftmp;						// scratch
        int i;							// loop counter

        // calculate the measurement vector moduli and return with identity quaternion if either is null.
        fmodGsSq = fGs[CHX] * fGs[CHX] + fGs[CHY] * fGs[CHY] + fGs[CHZ] * fGs[CHZ];
        fmodBcSq = fBc[CHX] * fBc[CHX] + fBc[CHY] * fBc[CHY] + fBc[CHZ] * fBc[CHZ];
        fmodGs = (float) Math.sqrt(fmodGsSq);
        fmodBc = (float) Math.sqrt(fmodBcSq);
        if ((fmodGs == 0.0F) || (fmodBc == 0.0F) || (fB == 0.0))
        {
            pfq.q0 = 1.0F;
            pfq.q1 = pfq.q2 = pfq.q3 = 0.0F;
            return;
        }

        // calculate the accelerometer and magnetometer noise covariances (units rad^2) and least squares weightings
        pfQvGQa = Math.abs(fmodGsSq - 1.0F);
        pfQvBQd = Math.abs(fmodBcSq - fB * fB);
        fag = pfQvBQd / (fB * fB * pfQvGQa + pfQvBQd);
        fam = 1.0F - fag;

        // compute useful ratios to reduce computation
        fagOvermodGs = fag / fmodGs;
        famOvermodBc = fam / fmodBc;
        famOvermodBccosDelta = famOvermodBc * fcosDelta;
        famOvermodBcsinDelta = famOvermodBc * fsinDelta;

        // compute the scalar product Gs.Bc and 6DOF accelerometer plus magnetometer geomagnetic inclination angle (deg)
        fGsdotBc = fGs[CHX] * fBc[CHX] + fGs[CHY] * fBc[CHY] + fGs[CHZ] * fBc[CHZ];
        pfDelta6DOF = (float)Math.toDegrees(Math.asin((fGsdotBc) / (fmodGs * fmodBc)));

        // set the K matrix to the non-zero accelerometer components
        fK[0][0] = fK[3][3] = fagOvermodGs * fGs[CHZ];
        fK[1][1] = fK[2][2] = -fK[0][0];
        fK[0][1] = fK[2][3] = fagOvermodGs * fGs[CHY];
        fK[1][3] = fagOvermodGs * fGs[CHX];
        fK[0][2] = -fK[1][3];

        // update the K matrix with the magnetometer component
        ftmp = famOvermodBcsinDelta * fBc[CHX];
        fK[0][2] += ftmp;
        fK[1][3] -= ftmp;
        fK[0][3] = fK[1][2] = famOvermodBccosDelta * fBc[CHX];
        ftmp = famOvermodBccosDelta * fBc[CHY];
        fK[0][0] += ftmp;
        fK[1][1] -= ftmp;
        fK[2][2] += ftmp;
        fK[3][3] -= ftmp;
        ftmp = famOvermodBcsinDelta * fBc[CHZ];
        fK[0][0] -= ftmp;
        fK[1][1] += ftmp;
        fK[2][2] += ftmp;
        fK[3][3] -= ftmp;
        ftmp = famOvermodBccosDelta * fBc[CHZ];
        fK[0][1] -= ftmp;
        fK[2][3] += ftmp;
        ftmp = famOvermodBcsinDelta * fBc[CHY];
        fK[0][1] -= ftmp;
        fK[2][3] -= ftmp;

        // copy above diagonal elements to below diagonal
        fK[1][0] = fK[0][1];
        fK[2][0] = fK[0][2];
        fK[2][1] = fK[1][2];
        fK[3][0] = fK[0][3];
        fK[3][1] = fK[1][3];
        fK[3][2] = fK[2][3];

        // set eigval to the unsorted eigenvalues and eigvec to the unsorted normalized eigenvectors of fK
        Matrix.eigencompute4(fK, eigval, eigvec, 4);

        // copy the largest eigenvector into the orientation quaternion fq
        i = 0;
        if (eigval[1] > eigval[i]) i = 1;
        if (eigval[2] > eigval[i]) i = 2;
        if (eigval[3] > eigval[i]) i = 3;
        pfq.q0 = eigvec[0][i];
        pfq.q1 = eigvec[1][i];
        pfq.q2 = eigvec[2][i];
        pfq.q3 = eigvec[3][i];

        // force q0 to be non-negative
        if (pfq.q0 < 0.0F)
        {
            pfq.q0 = -pfq.q0;
            pfq.q1 = -pfq.q1;
            pfq.q2 = -pfq.q2;
            pfq.q3 = -pfq.q3;
        }

        //return;
    }

    // extract the Android angles in degrees from the Android rotation matrix
    static void fAndroidAnglesDegFromRotationMatrix(float R[][], float pfPhiDeg, float pfTheDeg, float pfPsiDeg,
                                             float pfRhoDeg, float pfChiDeg)
    {
        // calculate the roll angle -90.0 <= Phi <= 90.0 deg
        pfPhiDeg = (float)Math.toDegrees(Math.asin(R[CHX][CHZ]));

        // calculate the pitch angle -180.0 <= The < 180.0 deg
        pfTheDeg = (float) Math.toDegrees(Math.atan2(-R[CHY][CHZ], R[CHZ][CHZ]));

        // map +180 pitch onto the functionally equivalent -180 deg pitch
        if (pfTheDeg == 180.0F)
        {
            pfTheDeg = -180.0F;
        }

        // calculate the yaw (compass) angle 0.0 <= Psi < 360.0 deg
        if (pfPhiDeg == 90.0F)
        {
            // vertical downwards gimbal lock case
            pfPsiDeg = (float) Math.toDegrees(Math.atan2(R[CHY][CHX], R[CHY][CHY])) - pfTheDeg;
        }
        else if (pfPhiDeg == -90.0F)
        {
            // vertical upwards gimbal lock case
            pfPsiDeg = (float) Math.toDegrees(Math.atan2(R[CHY][CHX], R[CHY][CHY])) + pfTheDeg;
        }
        else
        {
            // general case
            pfPsiDeg = (float) Math.toDegrees(Math.atan2(-R[CHX][CHY], R[CHX][CHX]));
        }

        // map yaw angle Psi onto range 0.0 <= Psi < 360.0 deg
        if (pfPsiDeg < 0.0F)
        {
            pfPsiDeg += 360.0F;
        }

        // check for rounding errors mapping small negative angle to 360 deg
        if (pfPsiDeg >= 360.0F)
        {
            pfPsiDeg = 0.0F;
        }

        // the compass heading angle Rho equals the yaw angle Psi
        // this definition is compliant with Motorola Xoom tablet behavior
        pfRhoDeg = pfPsiDeg;

        // calculate the tilt angle from vertical Chi (0 <= Chi <= 180 deg)
        pfChiDeg = (float) Math.toDegrees(Math.acos(R[CHZ][CHZ]));

        //return;
    }

    // computes normalized rotation quaternion from a rotation vector (deg)
    static void fQuaternionFromRotationVectorDeg(Fquaternion pq, float rvecdeg[], float fscaling)
    {
        float fetadeg;			// rotation angle (deg)
        float fetarad;			// rotation angle (rad)
        float fetarad2;			// eta (rad)^2
        float fetarad4;			// eta (rad)^4
        float sinhalfeta;		// sin(eta/2)
        float fvecsq;			// q1^2+q2^2+q3^2
        float ftmp;				// scratch variable

        // compute the scaled rotation angle eta (deg) which can be both positve or negative
        fetadeg = fscaling * (float) Math.sqrt(rvecdeg[CHX] * rvecdeg[CHX] + rvecdeg[CHY] * rvecdeg[CHY] + rvecdeg[CHZ] * rvecdeg[CHZ]);
        fetarad = fetadeg * FPIOVER180;
        fetarad2 = fetarad * fetarad;

        // calculate the sine and cosine using small angle approximations or exact
        // angles under sqrt(0.02)=0.141 rad is 8.1 deg and 1620 deg/s (=936deg/s in 3 axes) at 200Hz and 405 deg/s at 50Hz
        if (fetarad2 <= 0.02F)
        {
            // use MacLaurin series up to and including third order
            sinhalfeta = fetarad * (0.5F - ONEOVER48 * fetarad2);
        }
        else if (fetarad2 <= 0.06F)
        {
            // use MacLaurin series up to and including fifth order
            // angles under sqrt(0.06)=0.245 rad is 14.0 deg and 2807 deg/s (=1623deg/s in 3 axes) at 200Hz and 703 deg/s at 50Hz
            fetarad4 = fetarad2 * fetarad2;
            sinhalfeta = fetarad * (0.5F - ONEOVER48 * fetarad2 + ONEOVER3840 * fetarad4);
        }
        else
        {
            // use exact calculation
            sinhalfeta = (float) Math.sin(0.5F * fetarad);
        }

        // compute the vector quaternion components q1, q2, q3
        if (fetadeg != 0.0F)
        {
            // general case with non-zero rotation angle
            ftmp = fscaling * sinhalfeta / fetadeg;
            pq.q1 = rvecdeg[CHX] * ftmp;		// q1 = nx * sin(eta/2)
            pq.q2 = rvecdeg[CHY] * ftmp;		// q2 = ny * sin(eta/2)
            pq.q3 = rvecdeg[CHZ] * ftmp;		// q3 = nz * sin(eta/2)
        }
        else
        {
            // zero rotation angle giving zero vector component
            pq.q1 = pq.q2 = pq.q3 = 0.0F;
        }

        // compute the scalar quaternion component q0 by explicit normalization
        // taking care to avoid rounding errors giving negative operand to sqrt
        fvecsq = pq.q1 * pq.q1 + pq.q2 * pq.q2 + pq.q3 * pq.q3;
        if (fvecsq <= 1.0F)
        {
            // normal case
            pq.q0 = (float)Math.sqrt(1.0F - fvecsq);
        }
        else
        {
            // rounding errors are present
            pq.q0 = 0.0F;
        }

        //return;
    }

    // compute the orientation quaternion from a 3x3 rotation matrix
    static void fQuaternionFromRotationMatrix(float R[][], Fquaternion pq)
    {
        float fq0sq;			// q0^2
        float recip4q0;			// 1/4q0

        // the quaternion is not explicitly normalized in this function on the assumption that it
        // is supplied with a normalized rotation matrix. if the rotation matrix is normalized then
        // the quaternion will also be normalized even if the case of small q0

        // get q0^2 and q0
        fq0sq = 0.25F * (1.0F + R[CHX][CHX] + R[CHY][CHY] + R[CHZ][CHZ]);
        pq.q0 = (float) Math.sqrt(Math.abs(fq0sq));

        // normal case when q0 is not small meaning rotation angle not near 180 deg
        if (pq.q0 > SMALLQ0)
        {
            // calculate q1 to q3
            recip4q0 = 0.25F / pq.q0;
            pq.q1 = recip4q0 * (R[CHY][CHZ] - R[CHZ][CHY]);
            pq.q2 = recip4q0 * (R[CHZ][CHX] - R[CHX][CHZ]);
            pq.q3 = recip4q0 * (R[CHX][CHY] - R[CHY][CHX]);
        } // end of general case
        else
        {
            // special case of near 180 deg corresponds to nearly symmetric matrix
            // which is not numerically well conditioned for division by small q0
            // instead get absolute values of q1 to q3 from leading diagonal
            pq.q1 = (float) Math.sqrt(Math.abs(0.5F * (1.0F + R[CHX][CHX]) - fq0sq));
            pq.q2 = (float) Math.sqrt(Math.abs(0.5F * (1.0F + R[CHY][CHY]) - fq0sq));
            pq.q3 = (float) Math.sqrt(Math.abs(0.5F * (1.0F + R[CHZ][CHZ]) - fq0sq));

            // correct the signs of q1 to q3 by examining the signs of differenced off-diagonal terms
            if ((R[CHY][CHZ] - R[CHZ][CHY]) < 0.0F) pq.q1 = -pq.q1;
            if ((R[CHZ][CHX] - R[CHX][CHZ]) < 0.0F) pq.q2 = -pq.q2;
            if ((R[CHX][CHY] - R[CHY][CHX]) < 0.0F) pq.q3 = -pq.q3;
        } // end of special case

        //return;
    }

    // compute the rotation matrix from an orientation quaternion
    static void fRotationMatrixFromQuaternion(float R[][], Fquaternion pq)
    {
        float f2q;
        float f2q0q0, f2q0q1, f2q0q2, f2q0q3;
        float f2q1q1, f2q1q2, f2q1q3;
        float f2q2q2, f2q2q3;
        float f2q3q3;

        // set f2q to 2*q0 and calculate products
        f2q = 2.0F * pq.q0;
        f2q0q0 = f2q * pq.q0;
        f2q0q1 = f2q * pq.q1;
        f2q0q2 = f2q * pq.q2;
        f2q0q3 = f2q * pq.q3;
        // set f2q to 2*q1 and calculate products
        f2q = 2.0F * pq.q1;
        f2q1q1 = f2q * pq.q1;
        f2q1q2 = f2q * pq.q2;
        f2q1q3 = f2q * pq.q3;
        // set f2q to 2*q2 and calculate products
        f2q = 2.0F * pq.q2;
        f2q2q2 = f2q * pq.q2;
        f2q2q3 = f2q * pq.q3;
        f2q3q3 = 2.0F * pq.q3 * pq.q3;

        // calculate the rotation matrix assuming the quaternion is normalized
        R[CHX][CHX] = f2q0q0 + f2q1q1 - 1.0F;
        R[CHX][CHY] = f2q1q2 + f2q0q3;
        R[CHX][CHZ] = f2q1q3 - f2q0q2;
        R[CHY][CHX] = f2q1q2 - f2q0q3;
        R[CHY][CHY] = f2q0q0 + f2q2q2 - 1.0F;
        R[CHY][CHZ] = f2q2q3 + f2q0q1;
        R[CHZ][CHX] = f2q1q3 + f2q0q2;
        R[CHZ][CHY] = f2q2q3 - f2q0q1;
        R[CHZ][CHZ] = f2q0q0 + f2q3q3 - 1.0F;

        //return;
    }

    // computes rotation vector (deg) from rotation quaternion
    static void fRotationVectorDegFromQuaternion(Fquaternion pq, float rvecdeg[])
    {
        float fetarad;			// rotation angle (rad)
        float fetadeg;			// rotation angle (deg)
        float sinhalfeta;		// sin(eta/2)
        float ftmp;				// scratch variable

        // calculate the rotation angle in the range 0 <= eta < 360 deg
        if ((pq.q0 >= 1.0F) || (pq.q0 <= -1.0F))
        {
            // rotation angle is 0 deg or 2*180 deg = 360 deg = 0 deg
            fetarad = 0.0F;
            fetadeg = 0.0F;
        }
        else
        {
            // general case returning 0 < eta < 360 deg
            fetarad = 2.0F * (float) Math.acos(pq.q0);
            fetadeg = fetarad * F180OVERPI;
        }

        // map the rotation angle onto the range -180 deg <= eta < 180 deg
        if (fetadeg >= 180.0F)
        {
            fetadeg -= 360.0F;
            fetarad = fetadeg * FPIOVER180;
        }

        // calculate sin(eta/2) which will be in the range -1 to +1
        sinhalfeta = (float) Math.sin(0.5F * fetarad);

        // calculate the rotation vector (deg)
        if (sinhalfeta == 0.0F)
        {
            // the rotation angle eta is zero and the axis is irrelevant
            rvecdeg[CHX] = rvecdeg[CHY] = rvecdeg[CHZ] = 0.0F;
        }
        else
        {
            // general case with non-zero rotation angle
            ftmp = fetadeg / sinhalfeta;
            rvecdeg[CHX] = pq.q1 * ftmp;
            rvecdeg[CHY] = pq.q2 * ftmp;
            rvecdeg[CHZ] = pq.q3 * ftmp;
        }

        //return;
    }

    // function compute the quaternion product qA * qB
    static void qAeqBxC(Fquaternion pqA,Fquaternion pqB, Fquaternion pqC)
    {
        pqA.q0 = pqB.q0 * pqC.q0 - pqB.q1 * pqC.q1 - pqB.q2 * pqC.q2 - pqB.q3 * pqC.q3;
        pqA.q1 = pqB.q0 * pqC.q1 + pqB.q1 * pqC.q0 + pqB.q2 * pqC.q3 - pqB.q3 * pqC.q2;
        pqA.q2 = pqB.q0 * pqC.q2 - pqB.q1 * pqC.q3 + pqB.q2 * pqC.q0 + pqB.q3 * pqC.q1;
        pqA.q3 = pqB.q0 * pqC.q3 + pqB.q1 * pqC.q2 - pqB.q2 * pqC.q1 + pqB.q3 * pqC.q0;

        //return;
    }
    // function compute the quaternion product qA = qA * qB
    public static void qAeqAxB(Fquaternion pqA, Fquaternion pqB)
    {
        Fquaternion qProd = new Fquaternion();

        // perform the quaternion product
        qProd.q0 = pqA.q0 * pqB.q0 - pqA.q1 * pqB.q1 - pqA.q2 * pqB.q2 - pqA.q3 * pqB.q3;
        qProd.q1 = pqA.q0 * pqB.q1 + pqA.q1 * pqB.q0 + pqA.q2 * pqB.q3 - pqA.q3 * pqB.q2;
        qProd.q2 = pqA.q0 * pqB.q2 - pqA.q1 * pqB.q3 + pqA.q2 * pqB.q0 + pqA.q3 * pqB.q1;
        qProd.q3 = pqA.q0 * pqB.q3 + pqA.q1 * pqB.q2 - pqA.q2 * pqB.q1 + pqA.q3 * pqB.q0;

        // copy the result back into qA
        pqA.q0 = qProd.q0;
        pqA.q1 = qProd.q1;
        pqA.q2 = qProd.q2;
        pqA.q3 = qProd.q3;

        //return;
    }

    // function normalizes a rotation quaternion and ensures q0 is non-negative
    static void fqAeqNormqA(Fquaternion pqA)
    {
        float fNorm;					// quaternion Norm

        // calculate the quaternion Norm
        fNorm = (float) Math.sqrt(pqA.q0 * pqA.q0 + pqA.q1 * pqA.q1 + pqA.q2 * pqA.q2 + pqA.q3 * pqA.q3);
        if (fNorm > CORRUPTQUAT)
        {
            // general case
            fNorm = 1.0F / fNorm;
            pqA.q0 *= fNorm;
            pqA.q1 *= fNorm;
            pqA.q2 *= fNorm;
            pqA.q3 *= fNorm;
        }
        else
        {
            // return with identity quaternion since the quaternion is corrupted
            pqA.q0 = 1.0F;
            pqA.q1 = pqA.q2 = pqA.q3 = 0.0F;
        }

        // correct a negative scalar component if the function was called with negative q0
        if (pqA.q0 < 0.0F)
        {
            pqA.q0 = -pqA.q0;
            pqA.q1 = -pqA.q1;
            pqA.q2 = -pqA.q2;
            pqA.q3 = -pqA.q3;
        }

        //return;
    }

    // function computes the rotation quaternion that rotates unit vector u onto unit vector v as v=q*.u.q
// using q = 1/sqrt(2) * {sqrt(1 + u.v) - u x v / sqrt(1 + u.v)}
    static void fveqconjgquq(Fquaternion pfq, float fu[], float fv[])
    {
        float fuxv[] = new float[3];				// vector product u x v
        float fsqrt1plusudotv;		// sqrt(1 + u.v)
        float ftmp;					// scratch

        // compute sqrt(1 + u.v) and scalar quaternion component q0 (valid for all angles including 180 deg)
        fsqrt1plusudotv = (float) Math.sqrt(Math.abs(1.0F + fu[CHX] * fv[CHX] + fu[CHY] * fv[CHY] + fu[CHZ] * fv[CHZ]));
        pfq.q0 = ONEOVERSQRT2 * fsqrt1plusudotv;

        // calculate the vector product uxv
        fuxv[CHX] = fu[CHY] * fv[CHZ] - fu[CHZ] * fv[CHY];
        fuxv[CHY] = fu[CHZ] * fv[CHX] - fu[CHX] * fv[CHZ];
        fuxv[CHZ] = fu[CHX] * fv[CHY] - fu[CHY] * fv[CHX];

        // compute the vector component of the quaternion
        if (fsqrt1plusudotv != 0.0F)
        {
            // general case where u and v are not anti-parallel where u.v=-1
            ftmp = ONEOVERSQRT2 / fsqrt1plusudotv;
            pfq.q1 = -fuxv[CHX] * ftmp;
            pfq.q2 = -fuxv[CHY] * ftmp;
            pfq.q3 = -fuxv[CHZ] * ftmp;
        }
        else
        {
            // degenerate case where u and v are anti-aligned and the 180 deg rotation quaternion is not uniquely defined.
            // first calculate the un-normalized vector component (the scalar component q0 is already set to zero)
            pfq.q1 = fu[CHY] - fu[CHZ];
            pfq.q2 = fu[CHZ] - fu[CHX];
            pfq.q3 = fu[CHX] - fu[CHY];
            // and normalize the quaternion for this case checking for fu[CHX]=fu[CHY]=fuCHZ] where q1=q2=q3=0.
            ftmp = (float) Math.sqrt(Math.abs(pfq.q1 * pfq.q1 + pfq.q2 * pfq.q2 + pfq.q3 * pfq.q3));
            if (ftmp != 0.0F)
            {
                // normal case where all three components of fu (and fv=-fu) are not equal
                ftmp = 1.0F / ftmp;
                pfq.q1 *= ftmp;
                pfq.q2 *= ftmp;
                pfq.q3 *= ftmp;
            }
            else
            {
                // final case where the three entries are equal but since the vectors are known to be normalized
                // the vector u must be 1/root(3)*{1, 1, 1} or -1/root(3)*{1, 1, 1} so simply set the
                // rotation vector to 1/root(2)*{1, -1, 0} to cover both cases
                pfq.q1 = ONEOVERSQRT2;
                pfq.q2 = -ONEOVERSQRT2;
                pfq.q3 = 0.0F;
            }
        }

        //return;
    }

}
