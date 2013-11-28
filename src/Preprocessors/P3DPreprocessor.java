package Preprocessors;
import processing.core.PConstants;

//TODO: many copy-paste from GyroDrift (Accelerometer to SI)
public class P3DPreprocessor extends PreprocessorAbstract
{

    int GyroX = 0;
    int GyroY = 1;
    int GyroZ = 2;

    int accX = 3;
    int accY = 4;
    int accZ = 5;

    boolean allowShiftData = false;

    boolean isCalibrated = false;
    int readValues = 32;

    /**
     * AHRS
     */

    int OUTPUTMODE = 1;

    //int SENSOR_SIGN[] = new int[]{1,1,1,-1,-1,-1,1,1,1};
    int SENSOR_SIGN[] = new int[]{-1,-1,-1,1,1,1,1,-1,-1};

    float Kp_ROLLPITCH = 0.02f;
    float Ki_ROLLPITCH = 0.00002f;
    float Kp_YAW = 1.2f;
    float Ki_YAW = 0.00002f;


    float G_Dt; // Integration time


    //TODO: this value could be calculated at runtime
    float GRAVITY = 1038.8f;

    float Gyro_Gain_X = 0.00875f;
    float Gyro_Gain_Y = 0.00875f;
    float Gyro_Gain_Z = 0.00875f;


    int AN[] = new int[6]; //array that stores the gyro and accelerometer data
    int AN_OFFSET[] = new int[]{0,0,0,0,0,0}; //Array that stores the Offset of the sensors


    int gyro_x;
    int gyro_y;
    int gyro_z;
    int accel_x;
    int accel_y;
    int accel_z;
    int magnetom_x;
    int magnetom_y;
    int magnetom_z;
    float c_magnetom_x;
    float c_magnetom_y;
    float c_magnetom_z;
    float MAG_Heading;

    float Accel_Vector[] = new float[]{0, 0, 0}; //Store the acceleration in a vector
    float Gyro_Vector[] = new float[]{0, 0, 0};//Store the gyros turn rate in a vector
    float Omega_Vector[] = new float[]{0, 0, 0}; //Corrected Gyro_Vector data
    float Omega_P[] = new float[]{0, 0, 0};//Omega Proportional correction
    float Omega_I[] = new float[]{0, 0, 0};//Omega Integrator
    float Omega[] = new float[]{0, 0, 0};

    // Euler angles
    float roll;
    float pitch;
    float yaw;

    float errorRollPitch[] = new float[]{0, 0, 0};
    float errorYaw[] = new float[]{0, 0, 0};


    float DCM_Matrix[][] = new float[][] {
        {1, 0, 0},
        {0, 1, 0},
        {0, 0, 1}
    };

    //Gyros here
    float Update_Matrix[][] = new float[][] {
        {0, 1, 2},
        {3, 4, 5},
        {6, 7, 8}
    };


    float Temporary_Matrix[][] = new float[][] {
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    };

    /**
     * From Drift correction
     */
    static float Scaled_Omega_P[] = new float[3];
    static float Scaled_Omega_I[] = new float[3];

    @Override
    public float[] processValues(float[] incomingValues, int millisBetweenPack)
    {
        if(!isCalibrated)
        {
            this.preCalibrate(incomingValues);
            return null;
        }

        G_Dt = millisBetweenPack / 1000.0f;

        Read_Gyro(incomingValues);
        Read_Accel(incomingValues);

        float[] result = new float[3];

        // Calculations...
        Matrix_update();
        Normalize();
        Drift_correction();
        Euler_angles();

        result[0] = ToDeg(roll);
        result[1] = ToDeg(pitch);
        result[2] = ToDeg(yaw);

        if(!allowShiftData)
        {
            setAllowShiftData(isCalibrated);
        }

        return result;
    }

    @Override
    public String[] getColumnNames(String serialData)
    {
        String[] result = new String[3];

        result[0] = "X {-2000;2000}";
        result[1] = "Y {-2000;2000}";
        result[2] = "Z {-2000;2000}";

        return result;
    }

    public boolean preCalibrate(float[] incomingValues)
    {
        if(readValues > 0)    // We take some readings...
        {
            Read_Gyro(incomingValues);
            Read_Accel(incomingValues);

            for(int y=0; y < 6; y++) // Cumulate values
            {
                AN_OFFSET[y] += AN[y];
            }

            readValues--;

            return false;
        }

        for(int y=0; y < 6; y++)
        {
            AN_OFFSET[y] = AN_OFFSET[y] / 32;
        }

        AN_OFFSET[5] -= GRAVITY * SENSOR_SIGN[5];

        isCalibrated = true;


        return true;
    }

    public void Read_Gyro(float[] incomingValues)
    {
        //TODO: Check conversion
        AN[0] = (int)incomingValues[GyroX];
        AN[1] = (int)incomingValues[GyroY];
        AN[2] = (int)incomingValues[GyroZ];
        gyro_x = SENSOR_SIGN[0] * (AN[0] - AN_OFFSET[0]);
        gyro_y = SENSOR_SIGN[1] * (AN[1] - AN_OFFSET[1]);
        gyro_z = SENSOR_SIGN[2] * (AN[2] - AN_OFFSET[2]);
    }

    public void Read_Accel(float[] incomingValues)
    {
        //TODO: Check conversion
        AN[3] = (int)incomingValues[accX];
        AN[4] = (int)incomingValues[accY];
        AN[5] = (int)incomingValues[accZ];
        accel_x = SENSOR_SIGN[3] * (AN[3] - AN_OFFSET[3]);
        accel_y = SENSOR_SIGN[4] * (AN[4] - AN_OFFSET[4]);
        accel_z = SENSOR_SIGN[5] * (AN[5] - AN_OFFSET[5]);
    }


    void Matrix_update()
    {
        Gyro_Vector[0]=Gyro_Scaled_X(gyro_x); //gyro x roll
        Gyro_Vector[1]=Gyro_Scaled_Y(gyro_y); //gyro y pitch
        Gyro_Vector[2]=Gyro_Scaled_Z(gyro_z); //gyro Z yaw

        Accel_Vector[0] = accel_x;
        Accel_Vector[1] = accel_y;
        Accel_Vector[2] = accel_z;

        Omega = Vector_Add(Gyro_Vector, Omega_I);  //adding proportional term
        Omega_Vector = Vector_Add(Omega, Omega_P); //adding Integrator term

        //Accel_adjust();    //Remove centrifugal acceleration.   We are not using this function in this version - we have no speed measurement

        if (OUTPUTMODE==1) {
            Update_Matrix[0][0]=0;
            Update_Matrix[0][1]=-G_Dt*Omega_Vector[2];//-z
            Update_Matrix[0][2]=G_Dt*Omega_Vector[1];//y
            Update_Matrix[1][0]=G_Dt*Omega_Vector[2];//z
            Update_Matrix[1][1]=0;
            Update_Matrix[1][2]=-G_Dt*Omega_Vector[0];//-x
            Update_Matrix[2][0]=-G_Dt*Omega_Vector[1];//-y
            Update_Matrix[2][1]=G_Dt*Omega_Vector[0];//x
            Update_Matrix[2][2]=0;
        }
            else // Uncorrected data (no drift correction)
        {
            Update_Matrix[0][0]=0;
            Update_Matrix[0][1]=-G_Dt*Gyro_Vector[2];//-z
            Update_Matrix[0][2]=G_Dt*Gyro_Vector[1];//y
            Update_Matrix[1][0]=G_Dt*Gyro_Vector[2];//z
            Update_Matrix[1][1]=0;
            Update_Matrix[1][2]=-G_Dt*Gyro_Vector[0];
            Update_Matrix[2][0]=-G_Dt*Gyro_Vector[1];
            Update_Matrix[2][1]=G_Dt*Gyro_Vector[0];
            Update_Matrix[2][2]=0;
        }

        Temporary_Matrix = Matrix_Multiply(DCM_Matrix, Update_Matrix); //a*b=c

        for(int x=0; x<3; x++) //Matrix Addition (update)
        {
            for(int y=0; y<3; y++)
            {
                DCM_Matrix[x][y] += Temporary_Matrix[x][y];
            }
        }
    }

    float[] Vector_Add(float vectorIn1[], float vectorIn2[])
    {
        float[] vectorOut = new float[3];
        for(int c=0; c < 3; c++)
        {
            vectorOut[c] = vectorIn1[c] + vectorIn2[c];
        }

        return vectorOut;
    }


    float[][] Matrix_Multiply(float a[][], float b[][])
    {
        float result[][] = new float[3][3];

        float op[] = new float[3];
        for(int x=0; x<3; x++)
        {
            for(int y=0; y<3; y++)
            {
                for(int w=0; w<3; w++)
                {
                    op[w]=a[x][w]*b[w][y];
                }
                result[x][y]=0;
                result[x][y]=op[0]+op[1]+op[2];
            }
        }

        return result;
    }


    void Normalize()
    {
        float error=0;
        float temporary[][] = new float[3][3];
        float renorm=0;

        error = - Vector_Dot_Product(DCM_Matrix[0], DCM_Matrix[1])*0.5f; //eq.19

        temporary[0] = Vector_Scale(DCM_Matrix[1], error); //eq.19
        temporary[1] = Vector_Scale(DCM_Matrix[0], error); //eq.19

        temporary[0] = Vector_Add(temporary[0], DCM_Matrix[0]);//eq.19
        temporary[1] = Vector_Add(temporary[1], DCM_Matrix[1]);//eq.19

        temporary[2] = Vector_Cross_Product(temporary[0], temporary[1]); // c= a x b //eq.20

        renorm = 0.5f *(3 - Vector_Dot_Product(temporary[0], temporary[0])); //eq.21
        DCM_Matrix[0] = Vector_Scale(temporary[0], renorm);

        renorm = 0.5f *(3 - Vector_Dot_Product(temporary[1], temporary[1])); //eq.21
        DCM_Matrix[1] = Vector_Scale(temporary[1], renorm);

        renorm = 0.5f *(3 - Vector_Dot_Product(temporary[2], temporary[2])); //eq.21
        DCM_Matrix[2] = Vector_Scale(temporary[2], renorm);
    }

    float Vector_Dot_Product(float vector1[], float vector2[])
    {
        float op=0;

        for(int c=0; c<3; c++)
        {
            op += vector1[c] * vector2[c];
        }

        return op;
    }

    float[] Vector_Scale(float vectorIn[], float scale2)
    {
        float vectorOut[] = new float[3];

        for(int c=0; c<3; c++)
        {
            vectorOut[c] = vectorIn[c]*scale2;
        }

        return vectorOut;
    }

    //Computes the cross product of two vectors
    float[] Vector_Cross_Product(float v1[],float v2[])
    {
        float vectorOut[] = new float[3];

        vectorOut[0]= (v1[1]*v2[2]) - (v1[2]*v2[1]);
        vectorOut[1]= (v1[2]*v2[0]) - (v1[0]*v2[2]);
        vectorOut[2]= (v1[0]*v2[1]) - (v1[1]*v2[0]);

        return vectorOut;
    }


    void Drift_correction()
    {
        float mag_heading_x;
        float mag_heading_y;
        float errorCourse;
        //Compensation the Roll, Pitch and Yaw drift.

        float Accel_magnitude;
        float Accel_weight;


        //*****Roll and Pitch***************

        // Calculate the magnitude of the accelerometer vector
        Accel_magnitude = (float)Math.sqrt(Accel_Vector[0] * Accel_Vector[0] + Accel_Vector[1] * Accel_Vector[1] + Accel_Vector[2] * Accel_Vector[2]);
        Accel_magnitude = Accel_magnitude / GRAVITY; // Scale to gravity.

        // Dynamic weighting of accelerometer info (reliability filter)
        // Weight for accelerometer info (<0.5G = 0.0, 1G = 1.0 , >1.5G = 0.0)
        Accel_weight = 1 - 2 * Math.abs(1 - Accel_magnitude);  //
        if(Accel_weight < 0)
        {
            Accel_weight = 0;
        }
        else if(Accel_weight > 1)
        {
            Accel_weight = 1;
        }

        errorRollPitch = Vector_Cross_Product(Accel_Vector, DCM_Matrix[2]); //adjust the ground of reference
        Omega_P = Vector_Scale(errorRollPitch, Kp_ROLLPITCH * Accel_weight);

        Scaled_Omega_I = Vector_Scale(errorRollPitch, Ki_ROLLPITCH * Accel_weight);
        Omega_I = Vector_Add(Omega_I, Scaled_Omega_I);

        //*****YAW***************
        // We make the gyro YAW drift correction based on compass magnetic heading

//        mag_heading_x = cos(MAG_Heading);
//        mag_heading_y = sin(MAG_Heading);
        mag_heading_x = 0f;
        mag_heading_y = 0f;
        errorCourse=(DCM_Matrix[0][0] * mag_heading_y) - (DCM_Matrix[1][0] * mag_heading_x);  //Calculating YAW error
        errorYaw = Vector_Scale(DCM_Matrix[2], errorCourse); //Applys the yaw correction to the XYZ rotation of the aircraft, depeding the position.

        Scaled_Omega_P = Vector_Scale(errorYaw, Kp_YAW);//.01proportional of YAW.
        Omega_P = Vector_Add(Omega_P, Scaled_Omega_P);//Adding  Proportional.

        Scaled_Omega_I = Vector_Scale(errorYaw, Ki_YAW);//.00001Integrator
        Omega_I = Vector_Add(Omega_I, Scaled_Omega_I);//adding integrator to the Omega_I
    }

    void Euler_angles()
    {
        pitch = (float)-Math.asin(DCM_Matrix[2][0]);
        roll = (float)Math.atan2(DCM_Matrix[2][1], DCM_Matrix[2][2]);
        yaw = (float)Math.atan2(DCM_Matrix[1][0], DCM_Matrix[0][0]);
    }

    public float ToRad(float x)
    {
        return x * PConstants.DEG_TO_RAD;
    }

    public float ToDeg(float x)
    {
        return x * PConstants.RAD_TO_DEG;
    }

    public float Gyro_Scaled_X(float x)
    {
        return x * ToRad(Gyro_Gain_X);
    }

    public float Gyro_Scaled_Y(float x)
    {
        return x * ToRad(Gyro_Gain_Y);
    }

    public float Gyro_Scaled_Z(float x)
    {
        return x * ToRad(Gyro_Gain_Z);
    }


    @Override
    public boolean allowShiftData() {
        return this.allowShiftData;
    }

    public void setAllowShiftData(boolean allowShiftData) {
        this.allowShiftData = allowShiftData;
    }
}
