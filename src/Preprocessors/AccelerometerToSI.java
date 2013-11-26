package Preprocessors;


public class AccelerometerToSI extends PreprocessorAbstract
{

    float zeroX = 67.8f;
    float zeroY = 131.8f;
    float zeroZ = -1007.0f;

    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        float[] result = new float[3];


        result[0] = incomingValues[3] - zeroX ;//* 0.001f;
        result[1] = incomingValues[4] - zeroY ;//* 0.001f;
        result[2] = incomingValues[5] - zeroZ ;//* 0.001f;


        return result;
    }

    @Override
    public String[] getColumnNames(String serialData)
    {
        String[] result = new String[3];

        result[0] = "X{-250;250}";
        result[1] = "Y{-250;250}";
        result[2] = "Z{-250;250}";

        return result;
    }

    @Override
    public boolean allowShiftData() {
        return true;
    }
}
