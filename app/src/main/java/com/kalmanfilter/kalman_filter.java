package com.kalmanfilter;
/*
 * FileName : kalman_filter.c
 * Author   : xiahouzuoxin @163.com
 * Version  : v1.0
 * Date     : 2014/9/24 20:36:51
 * Brief    : 
 * 
 * Copyright (C) MICL,USTB
 */
 


/*
 * @brief   
 *   Init fields of structure @kalman1_state.
 *   I make some defaults in this init function:
 *     A = 1;
 *     H = 1; 
 *   and @q,@r are valued after prior tests.
 *
 *   NOTES: Please change A,H,q,r according to your application.
 *
 * @inputs  
 *   state - Klaman filter structure
 *   init_x - initial x state value   
 *   init_p - initial estimated error convariance
 * @outputs 
 * @retval  
 */
public class kalman_filter{

    float x1;  /* state */
    float A1;  /* x(n)=A*x(n-1)+u(n),u(n)~N(0,q) */
    float H1;  /* z(n)=H*x(n)+w(n),w(n)~N(0,r)   */
    float q1;  /* process(predict) noise convariance */
    float r1;  /* measure noise convariance */
    float p1;  /* estimated error convariance */
    float gain1;

    float x[] = new float[2];     /* state: [0]-angle [1]-diffrence of angle, 2x1 */
    float A[][] = new float[2][2];  /* X(n)=A*X(n-1)+U(n),U(n)~N(0,q), 2x2 */
    float H[] = new float[2];     /* Z(n)=H*X(n)+W(n),W(n)~N(0,r), 1x2   */
    float q[] = new float[2];     /* process(predict) noise convariance,2x1 [q0,0; 0,q1] */
    float r;        /* measure noise convariance */
    float p[][] = new float[2][2];  /* estimated error convariance,2x2 [p0 p1; p2 p3] */
    float gain[] = new float[2];  /* 2x1 */

    float init_x1 = 1,init_p1 = 0;
    float init_x[] = {1,0},init_p[][] = {{1,0},{0,1}};
    
    public void kalman1_init()
    {
        x1 = init_x1;
        p1 = init_p1;
        A1 = 1;
        H1 = 1;
        q1 = 2e+1f;//10e-6;  /* predict noise convariance */
        r1 = 5e+2f;//10e-5;  /* measure error convariance */
    }

    /*
     * @brief   
     *   1 Dimension Kalman filter
     * @inputs  
     *   state - Klaman filter structure
     *   z_measure - Measure value
     * @outputs 
     * @retval  
     *   Estimated result
     */
    public float kalman1_filter(float z_measure)
    {
        /* Predict */
        x1 = A1 * x1;
        p1 = A1 * A1 * p1 + q1;  /* p(n|n-1)=A^2*p(n-1|n-1)+q */
    
        /* Measurement */
        gain1 = p1 * H1 / (p1 * H1 * H1 + r1);
        x1 = x1 + gain1 * (z_measure - H1 * x1);
        p1 = (1 - gain1 * H1) * p1;
    
        return x1;
    }

    /*
     * @brief
     *   Init fields of structure @kalman1_state.
     *   I make some defaults in this init function:
     *     A = {{1, 0.1}, {0, 1}};
     *     H = {1,0};
     *   and @q,@r are valued after prior tests.
     *
     *   NOTES: Please change A,H,q,r according to your application.
     *
     * @inputs
     * @outputs
     * @retval
     */
    public void kalman2_init()
    {
        x[0]    = init_x[0];
        x[1]    = init_x[1];
        p[0][0] = init_p[0][0];
        p[0][1] = init_p[0][1];
        p[1][0] = init_p[1][0];
        p[1][1] = init_p[1][1];
        //A       = {{1, 0.1}, {0, 1}};
        A[0][0] = 1;
        A[0][1] = 0.1f;
        A[1][0] = 0;
        A[1][1] = 1;
        //H       = {1,0};
        H[0]    = 1;
        H[1]    = 0;
        //q       = {{10e-6,0}, {0,10e-6}};  /* measure noise convariance */
        q[0]    = 10e-7f;
        q[1]    = 10e-7f;
        r       = 10e-5f;  /* estimated error convariance */
    }

    /*
     * @brief
     *   2 Dimension kalman filter
     * @inputs
     *   state - Klaman filter structure
     *   z_measure - Measure value
     * @outputs
     *   x[0] - Updated state value, Such as angle,velocity
     *   x[1] - Updated state value, Such as diffrence angle, acceleration
     *   p    - Updated estimated error convatiance matrix
     * @retval
     *   Return value is equals to x[0], so maybe angle or velocity.
     */
    public float kalman2_filter(float z_measure)
    {
        float temp0 = 0.0f;
        float temp1 = 0.0f;
        float temp = 0.0f;

        /* Step1: Predict */
        x[0] = A[0][0] * x[0] + A[0][1] * x[1];
        x[1] = A[1][0] * x[0] + A[1][1] * x[1];
        /* p(n|n-1)=A^2*p(n-1|n-1)+q */
        p[0][0] = A[0][0] * p[0][0] + A[0][1] * p[1][0] + q[0];
        p[0][1] = A[0][0] * p[0][1] + A[1][1] * p[1][1];
        p[1][0] = A[1][0] * p[0][0] + A[0][1] * p[1][0];
        p[1][1] = A[1][0] * p[0][1] + A[1][1] * p[1][1] + q[1];

        /* Step2: Measurement */
        /* gain = p * H^T * [r + H * p * H^T]^(-1), H^T means transpose. */
        temp0 = p[0][0] * H[0] + p[0][1] * H[1];
        temp1 = p[1][0] * H[0] + p[1][1] * H[1];
        temp  = r + H[0] * temp0 + H[1] * temp1;
        gain[0] = temp0 / temp;
        gain[1] = temp1 / temp;
        /* x(n|n) = x(n|n-1) + gain(n) * [z_measure - H(n)*x(n|n-1)]*/
        temp = H[0] * x[0] + H[1] * x[1];
        x[0] = x[0] + gain[0] * (z_measure - temp);
        x[1] = x[1] + gain[1] * (z_measure - temp);

        /* Update @p: p(n|n) = [I - gain * H] * p(n|n-1) */
        p[0][0] = (1 - gain[0] * H[0]) * p[0][0];
        p[0][1] = (1 - gain[0] * H[1]) * p[0][1];
        p[1][0] = (1 - gain[1] * H[0]) * p[1][0];
        p[1][1] = (1 - gain[1] * H[1]) * p[1][1];

        return x[0];
    }
}
