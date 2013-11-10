import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class ProcessingSerialGraph extends PApplet {

    Serial arduino;
    String serialData;

    boolean showAllGraphsOnOneAxis = false;

    String[] COLUMN_NAMES;
    int[][] COLUMN_DATA;

    int showGraphTime = 10000; //10sec

    int[] MILLIS_BETWEEN_PACK;
    int lastMillis = 0;


    int[] MIN_VALUES;
    int[] MAX_VALUES;

    //Override
    int minValue = -40000;
    int maxValue = 40000;

    //Graph colors
    int[] graphColors = { 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF };
    //int[] graphColors = { 234, 44, 21, 76,160 };
    //Default graph color
    int defaultGraphColor = 0xcccccc;

    //This array is actually used for performance reasons
    int[] initedColorsArray;

    boolean columnNamesInited = false;

    String useSerialPort[] = {"COM7"};


    boolean deltaMillisCalc = false;


    protected Serial getSerial()
    {
        Serial result = null;

        println(Serial.list()); // Use this to print connected serial devices
        String list[] = Serial.list();
        for (String sPort: useSerialPort)
        {
            int portIndex = Arrays.asList(list).indexOf(sPort);
            if(portIndex != -1)
            {
                result = new Serial(this, Serial.list()[portIndex], 115200);
            }
        }

        if(result == null)
        {
            result = new Serial(this, Serial.list()[0], 115200);
        }


        return result;
    }

    public void setup() {
        size(1200, 700);

        MILLIS_BETWEEN_PACK = new int[width];


        arduino = getSerial();
        arduino.clear();
        arduino.bufferUntil('\n'); // Buffer until line feed
        smooth();
    }

    public void draw()
    {
        // Draw graphPaper
        background(0); // white
        /*for (int i = 0; i<=width/10; i++) {
            stroke(20); // gray
            line((-frameCount%10)+i*10, 0, (-frameCount%10)+i*10, height);
            line(0, i*10, width, i*10);
        }*/

        //Must init column names
        if(!columnNamesInited)
        {
            return;
        }

        // redraw each graph
        for(int val_num = 0; val_num < COLUMN_DATA.length; val_num++)
        {
            float graphBottom = val_num * height/COLUMN_DATA.length;
            drawLineSeparator(graphBottom);

            //Draw zero axis
            int center_val = (MIN_VALUES[val_num] + MAX_VALUES[val_num]) / 2;
            float half = map(center_val, MIN_VALUES[val_num], MAX_VALUES[val_num], 0, height/COLUMN_DATA.length);
            stroke(180);
            line(0, half + graphBottom, width, half + graphBottom);


            stroke(initedColorsArray[val_num]);
            fill(initedColorsArray[val_num]);

            textSize(12);
            text(center_val, 10, half + graphBottom - 5);


            textSize(18);
            text(COLUMN_NAMES[val_num] + " [" + String.valueOf(MIN_VALUES[val_num]) + "; " + String.valueOf(MAX_VALUES[val_num]) + "]", 10, graphBottom + 20);
            noFill();


            float xpos = width;
            float ypos;

            beginShape();
            for(int i = COLUMN_DATA[val_num].length - 1; i > 0; i--)
            {
                if(i < width - 1)
                {
                    float offset = (float) width / showGraphTime * MILLIS_BETWEEN_PACK[i];
                    xpos = xpos - offset;
                }

                if(showAllGraphsOnOneAxis)
                {
                    ypos = map(COLUMN_DATA[val_num][i], MIN_VALUES[val_num], MAX_VALUES[val_num], 0, height);
                }
                else
                {
                    ypos = map(COLUMN_DATA[val_num][i], MIN_VALUES[val_num], MAX_VALUES[val_num], 0, height/COLUMN_DATA.length);
                    ypos = ypos + graphBottom;
                }

                vertex(xpos, ypos);

                if(xpos < 0)
                {
                    break;
                }
            }
            endShape();
        }

    }

    public void drawLineSeparator(float graphBottom)
    {
        stroke(80);
        strokeWeight(3);
        line(0, graphBottom, width, graphBottom);
        strokeWeight(1);
    }

    public void serialEvent (Serial arduino) {
        String[] incomingValues;

        serialData = arduino.readStringUntil('\n');
        serialData = trim(serialData);
        if (serialData != null && !serialData.equals("")) {
            if(columnNamesInited)
            {
                // put all data and related timings one array back
                for(int i = 1; i < width; i++)
                {
                    for(int val_num = 0; val_num < COLUMN_DATA.length; val_num++)
                    {
                        COLUMN_DATA[val_num][i-1] = COLUMN_DATA[val_num][i];
                    }

                    MILLIS_BETWEEN_PACK[i-1] = MILLIS_BETWEEN_PACK[i];
                }


                String[] packMillis = split(serialData, "|");
                if(packMillis.length == 2)
                {
                    int arduinoMillis = Integer.parseInt(packMillis[packMillis.length - 1]);
                    if(deltaMillisCalc)
                    {
                        MILLIS_BETWEEN_PACK[width-1] = arduinoMillis - lastMillis;
                    }
                    else
                    {
                        deltaMillisCalc = true;
                    }
                    lastMillis = arduinoMillis;
                    serialData = packMillis[0];
                }


                incomingValues = split(serialData, ",");

                for(int val_num = 0; val_num < incomingValues.length; val_num++)
                {
                    COLUMN_DATA[val_num][width-1] = Integer.parseInt(incomingValues[val_num]);
                }
            }
            else
            {
                initColumnNames();
            }
        }

        arduino.clear();
    }

    public void initColumnNames()
    {
        int startColumnsIndex = serialData.indexOf(':');
        if(startColumnsIndex == -1)
        {

            //If Arduino already writes in serial port, we've got garbage in serialData.
            //So wait untill arduino resets
            return;
        }

        print("Init string from arduino: ");
        println(serialData);

        COLUMN_NAMES = split(serialData.substring(startColumnsIndex + 1), ",");

        //Init min and max array length
        MIN_VALUES = new int[COLUMN_NAMES.length];
        MAX_VALUES = new int[COLUMN_NAMES.length];

        initedColorsArray = new int[COLUMN_NAMES.length];

        for(int i=0; i < COLUMN_NAMES.length; i++)
        {
            int additionalDataIndex = COLUMN_NAMES[i].indexOf('{');
            if(additionalDataIndex != -1)
            {
                //Есть дополнительные данные
                //Обрезаем название колонки, а за ней фигурные скобки с дополнительными данными
                String AdditionalData = COLUMN_NAMES[i].substring(additionalDataIndex + 1, COLUMN_NAMES[i].length() - 1);
                //Самой колонке оставляем только имя
                COLUMN_NAMES[i] = COLUMN_NAMES[i].substring(0, additionalDataIndex);


                String[] aAdditionalData = split(AdditionalData, ";");

                for(int minMaxIndex = 0; minMaxIndex < aAdditionalData.length; minMaxIndex++) {
                    if(minMaxIndex == 0)
                    {
                        MIN_VALUES[i] = Integer.parseInt(aAdditionalData[minMaxIndex]);
                    }
                    else if(minMaxIndex == 1)
                    {
                        MAX_VALUES[i] = Integer.parseInt(aAdditionalData[minMaxIndex]);
                    }
                }


            }
            else
            {
                MIN_VALUES[i] = minValue;
                MAX_VALUES[i] = maxValue;
            }

            //Init colors
            if(graphColors.length > i)
            {
                initedColorsArray[i] = graphColors[i];
            }
            else
            {
                initedColorsArray[i] = defaultGraphColor;
            }
            //set alpha to FF
            initedColorsArray[i] |= 0xFF000000;
        }


        //Making buffer
        COLUMN_DATA = new int[COLUMN_NAMES.length][width];


        columnNamesInited = true;
    }



}
