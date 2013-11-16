package Preprocessors;


public class RecursiveFilter implements PreprocessorI {

    int[] periodicalData;
    int[] summ;
    int maxValues = 6;
    int counter = 0;
    boolean isFirstRun = true;
    boolean allowShiftData = false;

    float alpha = 0.1f;

    @Override
    public int[] processValues(int[] incomingValues) {
        int[] result = new int[incomingValues.length];


        if(isFirstRun)
        {
            periodicalData = new int[incomingValues.length];
            summ = new int[incomingValues.length];
            isFirstRun = false;
        }

        if(counter < maxValues)
        {
            for(int val_num = 0; val_num < incomingValues.length; val_num++)
            {
                float filter = periodicalData[val_num] + alpha * (incomingValues[val_num] - periodicalData[val_num]);
                summ[val_num] += (int) filter;
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
