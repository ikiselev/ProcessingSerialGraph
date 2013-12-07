package Preprocessors;


public class GyroDegreesPerSecond extends CalibratedPreprocessorAbstract
{
    int X = 0;
    int Y = 1;
    int Z = 2;

    float [] Gyro = new float[3];


    int SENSOR_SIGN[] = new int[]{1,1,1};



    public float[] processValues(float[] incomingValues, int millisBetweenPack)
    {
        if(!this.preCalibrate(incomingValues))
        {
            return null;
        }

        float[] result = new float[6];

        // / 14.375f for quadcopter
        Gyro[X] = (incomingValues[0] - errorMiddleValue[0]) * 0.00875f;
        Gyro[Y] = (incomingValues[1] - errorMiddleValue[1]) * 0.00875f;
        Gyro[Z] = (incomingValues[2] - errorMiddleValue[2]) * 0.00875f;

        Gyro[X] *= SENSOR_SIGN[X];
        Gyro[Y] *= SENSOR_SIGN[Y];
        Gyro[Z] *= SENSOR_SIGN[Z];

        result[0] = Gyro[X] * millisBetweenPack / 1000;
        result[1] = Gyro[Y] * millisBetweenPack / 1000;
        result[2] = Gyro[Z] * millisBetweenPack / 1000;

        result[3] = errorMiddleValue[0];
        result[4] = errorMiddleValue[1];
        result[5] = errorMiddleValue[2];

        return result;
    }

}
