package Preprocessors;


import lib.TR_IMUFilter;

public class QuaternionPreprocessor extends CalibratedPreprocessorAbstract
{
    TR_IMUFilter imu = new TR_IMUFilter();
    float[] angles = new float[3];


    int xOffsetIMU = 0;
    int yOffsetIMU = 0;


    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        if(!this.preCalibrate(incomingValues))
        {
            return null;
        }

        float[] result = new float[2];

        float gyrox = (incomingValues[0] - errorMiddleValue[0]) * 0.00875f;
        float gyroy = (incomingValues[1] - errorMiddleValue[1]) * 0.00875f;
        float gyroz = (incomingValues[2] - errorMiddleValue[2]) * 0.00875f;

        angles = imu.getRPY(gyrox, gyroy, gyroz, (int)incomingValues[3], (int)incomingValues[4], (int)incomingValues[5], millisBetweenPack);
        result[0] = angles[1] + 180 + xOffsetIMU;
        result[1] = angles[0] + 180 + yOffsetIMU;



        return result;
    }
}
