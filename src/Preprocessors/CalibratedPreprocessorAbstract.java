package Preprocessors;


abstract public class CalibratedPreprocessorAbstract extends PreprocessorAbstract
{
    boolean allowShiftData = false;
    boolean isCalibrated = false;

    int skipPackets = 10;
    int calibrationIterations = 250;
    int currentCalibrationIteration = 0;


    boolean isInitialized = false;
    /**
     * result of calibrated preprocessor
     */
    float[] errorMiddleValue;

    public boolean preCalibrate(float[] incomingValues)
    {
        if(isCalibrated)
        {

            return true;
        }
        /**
         * Initialize error container
         */
        if(!isInitialized)
        {
            errorMiddleValue = new float[incomingValues.length];
            isInitialized = true;
        }

        if(skipPackets > 0)
        {
            skipPackets--;
            return false;
        }

        if(currentCalibrationIteration < calibrationIterations)    // We take some readings...
        {
            /**
             * Sum all values
             */
            for(int index=0; index < incomingValues.length; index++)
            {
                errorMiddleValue[index] += incomingValues[index];
            }

            currentCalibrationIteration++;

            return false;
        }

        for(int index=0; index < incomingValues.length; index++)
        {
            errorMiddleValue[index] /= (float)currentCalibrationIteration;
        }

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
