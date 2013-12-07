package Preprocessors;
import processing.core.PConstants;

public class GravitySubtract extends CalibratedPreprocessorAbstract
{
    static final double ACCEL_DEVIATION_LIMIT = 0.005;
    static final int ACCEL_DEVIATION_LENGTH = 5;
    static final double GYRO_NOISE_LIMIT = 0.006;

    public static final int IDX_X = 0;
    public static final int IDX_Y = 1;
    public static final int IDX_Z = 2;

    private double gravityAccelHighLimit;
    private double gravityAccelLowLimit;
    private int gravityAccelLimitLen;
    private double simulatedGravity[] = new double[3];
    private double previousSimulatedGravity[] = null;

    boolean isFirstRun = true;



    float gyroDpsPerDigit = 0.00875f;
    float accGPerDigit = 0.001f; // 1 mg/digit
    float gravityAcceleration = 9.8f;


    float currentSpeedX = 0.0f;
    float currentSpeedY = 0.0f;
    float currentSpeedZ = 0.0f;


    @Override
    public String[] getColumnNames(String serialData)
    {
        String[] result = new String[3];

        result[0] = "X {-20;20}";
        result[1] = "Y {-200;200}";
        result[2] = "Z {-20;20}";

        return result;
    }

    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack)
    {
        if(!this.preCalibrate(incomingValues))
        {

            return null;
        }

        //Rad/sec
        incomingValues[0] = ((incomingValues[0] - errorMiddleValue[0]) * gyroDpsPerDigit * PConstants.DEG_TO_RAD);
        incomingValues[1] = ((incomingValues[1] - errorMiddleValue[1]) * gyroDpsPerDigit * PConstants.DEG_TO_RAD);
        incomingValues[2] = ((incomingValues[2] - errorMiddleValue[2]) * gyroDpsPerDigit * PConstants.DEG_TO_RAD);

        //m/s^2
        incomingValues[3] = incomingValues[3] * accGPerDigit * gravityAcceleration;
        incomingValues[4] = incomingValues[4] * accGPerDigit * gravityAcceleration;
        incomingValues[5] = incomingValues[5] * accGPerDigit * gravityAcceleration;



        if(isFirstRun)
        {
            isFirstRun = false;


            /**
             * Calibrating
             */
            gravityAccelLimitLen = -1;

            simulatedGravity[IDX_X] = incomingValues[3];
            simulatedGravity[IDX_Y] = incomingValues[4];
            simulatedGravity[IDX_Z] = incomingValues[5];


            double gravityAccelLen = Math.sqrt(
                    simulatedGravity[IDX_X]*simulatedGravity[IDX_X] +
                            simulatedGravity[IDX_Y]*simulatedGravity[IDX_Y] +
                            simulatedGravity[IDX_Z]*simulatedGravity[IDX_Z]);

            gravityAccelHighLimit = gravityAccelLen * ( 1.0 + ACCEL_DEVIATION_LIMIT );
            gravityAccelLowLimit = gravityAccelLen * ( 1.0 - ACCEL_DEVIATION_LIMIT );
        }



        double dv[] = new double[3];

        dv[IDX_X] = (double)incomingValues[0];
        dv[IDX_Y] = (double)incomingValues[1];
        dv[IDX_Z] = (double)incomingValues[2];
        measureGyro(dv, millisBetweenPack);

        dv[IDX_X] = (double)incomingValues[3];
        dv[IDX_Y] = (double)incomingValues[4];
        dv[IDX_Z] = (double)incomingValues[5];
        double[] rotatedDiff = measureAccel(dv);








        float[] result = new float[rotatedDiff.length];
        for(int i = 0; i < rotatedDiff.length; i++)
        {
            result[i] = (float)rotatedDiff[i];
        }


        currentSpeedX = currentSpeedX + (float)rotatedDiff[0] * 3.6f * millisBetweenPack / 1000.0f;
        currentSpeedY = currentSpeedY + (float)rotatedDiff[1] * 3.6f * millisBetweenPack / 1000.0f;
        currentSpeedZ = currentSpeedZ + (float)rotatedDiff[2] * 3.6f * millisBetweenPack / 1000.0f;

        result[0] = currentSpeedX;
        result[1] = currentSpeedY;
        result[2] = currentSpeedZ;

        return result;
    }


    public double[] measureAccel(double[] dv)
    {
        double accelLen = Math.sqrt(
                dv[IDX_X]*dv[IDX_X] +
                    dv[IDX_Y]*dv[IDX_Y] +
                    dv[IDX_Z]*dv[IDX_Z] );

        if( ( accelLen < gravityAccelHighLimit ) &&
                ( accelLen > gravityAccelLowLimit ) ) {
            if( gravityAccelLimitLen < 0 )
            {
                gravityAccelLimitLen = ACCEL_DEVIATION_LENGTH;
            }

            --gravityAccelLimitLen;

            if( gravityAccelLimitLen <= 0 ) {
                gravityAccelLimitLen = 0;
                simulatedGravity[IDX_X] = dv[IDX_X];
                simulatedGravity[IDX_Y] = dv[IDX_Y];
                simulatedGravity[IDX_Z] = dv[IDX_Z];
            }
        } else
        {
            gravityAccelLimitLen = -1;
        }

        double[] diff = vecdiff( dv, simulatedGravity );
        double[] rotatedDiff = rotateToEarth( diff );
        /*if( captureFile != null ) {
            captureFile.println( timeStamp+
                    ","+
                    "vecdiff"+
                    ","+
                    diff[IDX_X]+
                    ","+
                    diff[IDX_Y]+
                    ","+
                    diff[IDX_Z]);
            captureFile.println( timeStamp+
                    ","+
                    "rotateddiff"+
                    ","+
                    rotatedDiff[IDX_X]+
                    ","+
                    rotatedDiff[IDX_Y]+
                    ","+
                    rotatedDiff[IDX_Z]);
        }*/


        return rotatedDiff;
    }


    public void measureGyro(double[] dv, int millisBetweenPack)
    {
        double dt = millisBetweenPack / 1000.0f;
        double dx = gyroNoiseLimiter( dv[IDX_X] * dt );
        double dy = gyroNoiseLimiter( dv[IDX_Y] * dt );
        double dz = gyroNoiseLimiter( dv[IDX_Z] * dt );
        rotx( simulatedGravity, -dx);
        roty( simulatedGravity, -dy);
        rotz( simulatedGravity, -dz);
        /*if( captureFile != null ) {
            if( difflimit())
                captureFile.println( timeStamp+
                        ","+
                        "simul"+
                        ","+
                        simulatedGravity[IDX_X]+
                        ","+
                        simulatedGravity[IDX_Y]+
                        ","+
                        simulatedGravity[IDX_Z]);
        }*/
    }


    private double gyroNoiseLimiter( double gyroValue ) {
        double v = gyroValue;
        if( Math.abs( v ) < GYRO_NOISE_LIMIT )
            v = 0.0;
        return v;
    }

    private void copySimulatedGravity() {
        previousSimulatedGravity[IDX_X] = simulatedGravity[IDX_X];
        previousSimulatedGravity[IDX_Y] = simulatedGravity[IDX_Y];
        previousSimulatedGravity[IDX_Z] = simulatedGravity[IDX_Z];
    }

    private boolean difflimit() {
        if( previousSimulatedGravity == null ) {
            previousSimulatedGravity = new double[3];
            copySimulatedGravity();
            return true;
        } else
        if( ( Math.abs( previousSimulatedGravity[IDX_X]-simulatedGravity[ IDX_X ]) > 0.1) ||
                ( Math.abs( previousSimulatedGravity[IDX_Y]-simulatedGravity[ IDX_Y ]) > 0.1) ||
                ( Math.abs( previousSimulatedGravity[IDX_Z]-simulatedGravity[ IDX_Z ]) > 0.1) ) {
            copySimulatedGravity();
            return true;
        }
        return false;
    }

    private void rotz( double vec[], double dz ) {
        double x = vec[IDX_X];
        double y = vec[IDX_Y];
        double z = vec[IDX_Z];
        vec[IDX_X] = x*Math.cos(dz)-y*Math.sin(dz);
        vec[IDX_Y] = x*Math.sin(dz)+y*Math.cos(dz);
    }

    private void rotx( double vec[], double dx ) {
        double x = vec[IDX_X];
        double y = vec[IDX_Y];
        double z = vec[IDX_Z];
        vec[IDX_Y] = y*Math.cos(dx)-z*Math.sin(dx);
        vec[IDX_Z] = y*Math.sin(dx)+z*Math.cos(dx);
    }

    private void roty( double vec[], double dy ) {
        double x = vec[IDX_X];
        double y = vec[IDX_Y];
        double z = vec[IDX_Z];
        vec[IDX_Z] = z*Math.cos(dy)-x*Math.sin(dy);
        vec[IDX_X] = z*Math.sin(dy)+x*Math.cos(dy);
    }

    private double[] vecdiff( double v1[], double v2[] ) {
        double diff[] = new double[3];
        diff[IDX_X] = v1[IDX_X] - v2[IDX_X];
        diff[IDX_Y] = v1[IDX_Y] - v2[IDX_Y];
        diff[IDX_Z] = v1[IDX_Z] - v2[IDX_Z];
        return diff;
    }

    private double fixAtanDegree( double deg, double y, double x ) {
        double rdeg = deg;
        if( ( x < 0.0 ) && ( y > 0.0 ) )
            rdeg = Math.PI - deg;
        if( ( x < 0.0 ) && ( y < 0.0 ) )
            rdeg = Math.PI + deg;
        return rdeg;
    }

    private double[] rotateToEarth( double diff[] ) {
        double rotatedDiff[] = new double[3];
        rotatedDiff[IDX_X] = diff[IDX_X];
        rotatedDiff[IDX_Y] = diff[IDX_Y];
        rotatedDiff[IDX_Z] = diff[IDX_Z];

        double gravity[] = new double[3];
        gravity[ IDX_X ] = simulatedGravity[ IDX_X ];
        gravity[ IDX_Y ] = simulatedGravity[ IDX_Y ];
        gravity[ IDX_Z ] = simulatedGravity[ IDX_Z ];

        double dz = Math.atan2( gravity[IDX_Y], gravity[IDX_X]);
        dz = fixAtanDegree( dz, gravity[IDX_Y], gravity[IDX_X] );
        rotz( rotatedDiff, -dz );
        rotz( gravity, -dz );

        double dy = Math.atan2( gravity[IDX_X], gravity[IDX_Z]);
        dy = fixAtanDegree( dy, gravity[IDX_X], gravity[IDX_Z]);
        roty( rotatedDiff, -dy );
        return rotatedDiff;
    }
}
