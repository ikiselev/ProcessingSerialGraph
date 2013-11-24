package Preprocessors;


public class AccelerometerToSI implements PreprocessorI {

    int counter = 0;

    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        float[] result = new float[incomingValues.length];

        for(int val_num = 0; val_num < incomingValues.length; val_num++)
        {
            result[val_num] = incomingValues[val_num];
        }

        return result;
    }

    @Override
    public boolean allowShiftData() {
        return true;
    }
}
