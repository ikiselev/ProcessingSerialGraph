package lib;


public class TR_IMUFilter
{
    float gyroX, gyroY, gyroZ;
    float accelX, accelY, accelZ;

    float sampleFreq; // half the sample period expressed in seconds

    float q0 = 1.0f;
    float q1 = 0.0f;
    float q2 = 0.0f;
    float q3 = 0.0f;


    float twoKp = 2.0f * 2.0f;
    float twoKi = 2.0f * 0.005f;

    float integralFBx = 0.0f,  integralFBy = 0.0f, integralFBz = 0.0f;



    /**
     * Computes the euler angles derived from the quaternion
     */
    public void getEuler(float[] angles, int millisBetweenPack) {
        float[] q = getQuaternion(millisBetweenPack);

        angles[0] = (float)(Math.atan2(2 * q[1] * q[2] - 2 * q[0] * q[3], 2 * q[0]*q[0] + 2 * q[1] * q[1] - 1) * 180/Math.PI); // psi
        angles[1] = (float)-(Math.asin(2 * q[1] * q[3] + 2 * q[0] * q[2]) * 180/Math.PI); // theta
        angles[2] = (float)(Math.atan2(2 * q[2] * q[3] - 2 * q[0] * q[1], 2 * q[0] * q[0] + 2 * q[3] * q[3] - 1) * 180/Math.PI); // phi
    }

    /**
     * Updates and outputs the current quaternion into q
     */
    public float[] getQuaternion(int millisBetweenPack) {

        float[] q = new float[4];


        sampleFreq = 1.0f / (millisBetweenPack / 1000.0f);
        updateAHRS((float)(gyroX * Math.PI / 180), (float)(gyroY * Math.PI/180), (float)(gyroZ * Math.PI/180), accelX, accelY, accelZ);

        q[0] = q0;
        q[1] = q1;
        q[2] = q2;
        q[3] = q3;

        return q;
    }


    /**
     * Returns the roll pitch and yaw angles
     *
     */
    public float[] getRPY(float gyroX, float gyroY, float gyroZ, float accelX, float accelY, float accelZ, int millisBetweenPack) {
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;


        float gx, gy, gz;

        float[] q = getQuaternion(millisBetweenPack);

        gx = 2 * (q[1]*q[3] - q[0]*q[2]);
        gy = 2 * (q[0]*q[1] + q[2]*q[3]);
        gz = q[0]*q[0] - q[1]*q[1] - q[2]*q[2] + q[3]*q[3];

        float[] angles = new float[3];
        angles[0] = (float)(Math.atan(gy / Math.sqrt(gx*gx + gz*gz))  * 180/Math.PI);
        angles[1] = (float)(Math.atan(gx / Math.sqrt(gy*gy + gz*gz))  * 180/Math.PI);
        angles[2] = (float)(Math.atan2(2 * q[1] * q[2] - 2 * q[0] * q[3], 2 * q[0]*q[0] + 2 * q[1] * q[1] - 1) * 180/Math.PI);

        return angles;
    }




    /**
     * Taken from FreeIMU
     */
    void updateAHRS(float gx, float gy, float gz, float ax, float ay, float az) {
        float recipNorm;
        float q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;
        float halfex = 0.0f, halfey = 0.0f, halfez = 0.0f;
        float qa, qb, qc;

        // Auxiliary variables to avoid repeated arithmetic
        q0q0 = q0 * q0;
        q0q1 = q0 * q1;
        q0q2 = q0 * q2;
        q0q3 = q0 * q3;
        q1q1 = q1 * q1;
        q1q2 = q1 * q2;
        q1q3 = q1 * q3;
        q2q2 = q2 * q2;
        q2q3 = q2 * q3;
        q3q3 = q3 * q3;

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if((ax != 0.0f) && (ay != 0.0f) && (az != 0.0f)) {
            float halfvx, halfvy, halfvz;

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Estimated direction of gravity
            halfvx = q1q3 - q0q2;
            halfvy = q0q1 + q2q3;
            halfvz = q0q0 - 0.5f + q3q3;

            // Error is sum of cross product between estimated direction and measured direction of field vectors
            halfex += (ay * halfvz - az * halfvy);
            halfey += (az * halfvx - ax * halfvz);
            halfez += (ax * halfvy - ay * halfvx);
        }

        // Apply feedback only when valid data has been gathered from the accelerometer or magnetometer
        if(halfex != 0.0f && halfey != 0.0f && halfez != 0.0f) {
            // Compute and apply integral feedback if enabled
            if(twoKi > 0.0f) {
                integralFBx += twoKi * halfex * (1.0f / sampleFreq);  // integral error scaled by Ki
                integralFBy += twoKi * halfey * (1.0f / sampleFreq);
                integralFBz += twoKi * halfez * (1.0f / sampleFreq);
                gx += integralFBx;  // apply integral feedback
                gy += integralFBy;
                gz += integralFBz;
            }
            else {
                integralFBx = 0.0f; // prevent integral windup
                integralFBy = 0.0f;
                integralFBz = 0.0f;
            }

            // Apply proportional feedback
            gx += twoKp * halfex;
            gy += twoKp * halfey;
            gz += twoKp * halfez;
        }

        // Integrate rate of change of quaternion
        gx *= (0.5f * (1.0f / sampleFreq));   // pre-multiply common factors
        gy *= (0.5f * (1.0f / sampleFreq));
        gz *= (0.5f * (1.0f / sampleFreq));
        qa = q0;
        qb = q1;
        qc = q2;
        q0 += (-qb * gx - qc * gy - q3 * gz);
        q1 += (qa * gx + qc * gz - q3 * gy);
        q2 += (qa * gy - qb * gz + q3 * gx);
        q3 += (qa * gz + qb * gy - qc * gx);

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;
    }


    /**
     * Inverted sqrt first seen in quake3
     */
    public static float invSqrt(float x) {
        float xhalf = 0.5f*x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i>>1);
        x = Float.intBitsToFloat(i);
        x = x*(1.5f - xhalf*x*x);
        return x;
    }
}
