package Preprocessors;


public class GyroDegreesPerSecond extends PreprocessorAbstract
{
    boolean allowShiftData = false;
    boolean isCalibrated = false;
    int calibrationIterations = 250;
    int currentCalibrationIteration = 0;

    int X = 0;
    int Y = 1;
    int Z = 2;

    float [] Gyro = new float[3];

    float gyroErrX_250dps = 0.0f;
    float gyroErrY_250dps = 0.0f;
    float gyroErrZ_250dps = 0.0f;

    int SENSOR_SIGN[] = new int[]{1,1,1};



    public float[] processValues(float[] incomingValues, int millisBetweenPack)
    {
        if(!isCalibrated && !this.preCalibrate(incomingValues))
        {
            return null;
        }

        float[] result = new float[6];

        Gyro[X] = (incomingValues[0] - gyroErrX_250dps) * 0.00875f;
        Gyro[Y] = (incomingValues[1] - gyroErrY_250dps) * 0.00875f;
        Gyro[Z] = (incomingValues[2] - gyroErrZ_250dps) * 0.00875f;


        result[0] = Gyro[X] * millisBetweenPack / 1000;
        result[1] = Gyro[Y] * millisBetweenPack / 1000;
        result[2] = Gyro[Z] * millisBetweenPack / 1000;

        result[3] = gyroErrX_250dps;
        result[4] = gyroErrY_250dps;
        result[5] = gyroErrZ_250dps;

        return result;
    }



    public boolean preCalibrate(float[] incomingValues)
    {
        if(currentCalibrationIteration < calibrationIterations)    // We take some readings...
        {

            gyroErrX_250dps += (int)incomingValues[X];
            gyroErrY_250dps += (int)incomingValues[Y];
            gyroErrZ_250dps += (int)incomingValues[Z];

            currentCalibrationIteration++;

            return false;
        }

        gyroErrX_250dps /= currentCalibrationIteration;
        gyroErrY_250dps /= currentCalibrationIteration;
        gyroErrZ_250dps /= currentCalibrationIteration;

        gyroErrX_250dps *= SENSOR_SIGN[X];
        gyroErrY_250dps *= SENSOR_SIGN[Y];
        gyroErrZ_250dps *= SENSOR_SIGN[Z];

        isCalibrated = true;
        setAllowShiftData(true);

        return true;
    }

    @Override
    public boolean allowShiftData() {
        return this.allowShiftData;
    }

    public void setAllowShiftData(boolean allowShiftData) {
        this.allowShiftData = allowShiftData;
    }
}
