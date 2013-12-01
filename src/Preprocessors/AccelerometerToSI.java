package Preprocessors;

/**
 * Препроцессор показывает значения с акселерометра и гироскопа в человекопонятных измерениях.
 *
 * Гироскоп: градусы в секунду (в СИ: радианы в сек)
 * Акселерометр: метры в секунду в квадрате
 */
public class AccelerometerToSI extends CalibratedPreprocessorAbstract
{

    /**
     * Values from gyroscope datasheet
     */
    int gyroSensevity = 250; //dps
    float gyroDpsPerDigit = 0.00875f;

    int accRange = 2; //g
    float accGPerDigit = 0.001f; // 1 mg/digit

    float gravityAcceleration = 9.8f;
    int graphRange = (int)Math.ceil(accRange * gravityAcceleration);


    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack) {
        if(!this.preCalibrate(incomingValues))
        {
            return null;
        }

        float[] result = new float[6];

        result[0] = ((incomingValues[0] - errorMiddleValue[0]) * gyroDpsPerDigit);
        result[1] = ((incomingValues[1] - errorMiddleValue[1]) * gyroDpsPerDigit);
        result[2] = ((incomingValues[2] - errorMiddleValue[2]) * gyroDpsPerDigit);
        result[3] = incomingValues[3] * accGPerDigit * gravityAcceleration;
        result[4] = incomingValues[4] * accGPerDigit * gravityAcceleration;
        result[5] = incomingValues[5] * accGPerDigit * gravityAcceleration;

        return result;
    }


    @Override
    public String[] getColumnNames(String serialData)
    {
        String[] result = new String[6];

        //Gyro
        result[0] = "X deg/s{-" + gyroSensevity + ";" + gyroSensevity + "}";
        result[1] = "Y deg/s{-" + gyroSensevity + ";" + gyroSensevity + "}";
        result[2] = "Z deg/s{-" + gyroSensevity + ";" + gyroSensevity + "}";

        //Accel
        result[3] = "X m/s2{-" + graphRange + ";" + graphRange + "}";
        result[4] = "Y m/s2{-" + graphRange + ";" + graphRange + "}";
        result[5] = "Z m/s2{-" + graphRange + ";" + graphRange + "}";

        return result;
    }
}
