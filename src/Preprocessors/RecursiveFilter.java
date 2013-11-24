package Preprocessors;


public class RecursiveFilter implements PreprocessorI {

    float[] periodicalData;
    float[] summ;
    int maxValues = 3;
    int counter = 0;
    boolean isFirstRun = true;
    boolean allowShiftData = false;

    float alpha = 0.1f;

    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        float[] result = new float[incomingValues.length];


        if(isFirstRun)
        {
            periodicalData = new float[incomingValues.length];
            summ = new float[incomingValues.length];
            isFirstRun = false;
        }

        if(counter < maxValues)
        {
            for(int val_num = 0; val_num < incomingValues.length; val_num++)
            {
                float filter = periodicalData[val_num] + alpha * (incomingValues[val_num] - periodicalData[val_num]);
                summ[val_num] += filter;
            }
            counter++;
        }
        else
        {
            for(int val_num = 0; val_num < periodicalData.length; val_num++)
            {
                periodicalData[val_num] = summ[val_num] / counter;
                result[val_num] = periodicalData[val_num];
                summ[val_num] = 0;
            }
            counter = 0;

            setAllowShiftData(true);
        }


        return result;
    }

    @Override
    public boolean allowShiftData() {
        if(this.allowShiftData)
        {
            setAllowShiftData(false);


            return true;
        }


        return false;
    }

    public void setAllowShiftData(boolean allowShiftData) {
        this.allowShiftData = allowShiftData;
    }
}
