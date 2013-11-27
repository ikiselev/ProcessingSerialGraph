package Preprocessors;

//TODO: many copy-paste from GyroDrift (Accelerometer to SI)
public class P3DPreprocessor extends PreprocessorAbstract
{
    int X = 0;
    int Y = 1;
    int Z = 2;

    float [] Gyro = new float[3];

    float gyroErrX_250dps = 31.2f;
    float gyroErrY_250dps = 59.7f;
    float gyroErrZ_250dps = 77.8f;


    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        float[] result = new float[3];

        Gyro[X] = (incomingValues[0] + gyroErrX_250dps) * 0.00875f;
        Gyro[Y] = (incomingValues[1] + gyroErrY_250dps) * 0.00875f;
        Gyro[Z] = (incomingValues[2] + gyroErrZ_250dps) * 0.00875f;


        result[0] = Gyro[X] * millisBetweenPack / 1000;
        result[1] = Gyro[Y] * millisBetweenPack / 1000;
        result[2] = Gyro[Z] * millisBetweenPack / 1000;

        return result;
    }


    @Override
    public boolean allowShiftData() {
        return true;
    }
}
