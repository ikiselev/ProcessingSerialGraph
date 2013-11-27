package Preprocessors;

import processing.core.PApplet;

//TODO: many copy-paste from GyroDrift (Accelerometer to SI)
public class P3DPreprocessor extends PreprocessorAbstract
{
    int X = 0;
    int Y = 1;
    int Z = 2;


    float X_1g_negative = -1020.2f;
    float X_1g_positive = 1040.5f;

    float Y_1g_negative = -1045.7f;
    float Y_1g_positive = 1050.6f;

    float Z_1g_negative = -1012.4f;
    float Z_1g_positive = 1038.8f;

    float EarthGravityVector = Z_1g_negative;

    float [] Gyro = new float[3];
    float [] Acc = new float[3];

    float gyroErrX_250dps = 31.2f;
    float gyroErrY_250dps = 59.7f;
    float gyroErrZ_250dps = 77.8f;


    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        float[] result = new float[5];

        Gyro[X] = (incomingValues[0] + gyroErrX_250dps) * 0.00875f;
        Gyro[Y] = (incomingValues[1] + gyroErrY_250dps) * 0.00875f;
        Gyro[Z] = (incomingValues[2] + gyroErrZ_250dps) * 0.00875f;


        Acc[X] = (float) Math.atan2(PApplet.map(incomingValues[3], X_1g_negative, X_1g_positive, Z_1g_negative, Z_1g_positive), EarthGravityVector);
        Acc[Y] = (float) Math.atan2(PApplet.map(incomingValues[4], Y_1g_negative, Y_1g_positive, Z_1g_negative, Z_1g_positive), EarthGravityVector);
        Acc[Z] = (float) Math.asin(incomingValues[5] / EarthGravityVector);


        result[0] = Gyro[X] * millisBetweenPack / 1000;
        result[1] = Gyro[Y] * millisBetweenPack / 1000;
        result[2] = Gyro[Z] * millisBetweenPack / 1000;



        result[3] = Acc[X];
        result[4] = Acc[Y];

        return result;
    }


    @Override
    public boolean allowShiftData() {
        return true;
    }
}
