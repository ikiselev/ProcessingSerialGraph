import processing.core.PApplet;

/**
 * TODO: set public or protected access specifiers
 */

public class Graph {
    boolean showAllGraphsOnOneAxis = false;

    String[] COLUMN_NAMES;
    int[][] COLUMN_DATA;

    int showGraphTime = 10000; //10sec
    int lineSeparatorEvery = 1000; //1sec

    int[] MILLIS_BETWEEN_PACK;
    int timingOffset = 0;

    int lastMillis = 0;


    int[] MIN_VALUES;
    int[] MAX_VALUES;

    //Override
    int minValue = -40000;
    int maxValue = 40000;

    //Graph colors
    int[] graphColors = { 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF };

    //Default graph color
    int defaultGraphColor = 0xcccccc;

    //This array is actually used for performance reasons
    int[] initedColorsArray;

    public boolean columnNamesInited = false;

    boolean deltaMillisCalc = false;


    int width;

    PApplet mainWindow;

    public Graph(PApplet mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void drawGraph()
    {
        //Must init column names
        if(!columnNamesInited)
        {
            return;
        }

        this.drawNet();

        // redraw each graph
        for(int val_num = 0; val_num < COLUMN_DATA.length; val_num++)
        {
            float graphBottom = val_num * mainWindow.height/COLUMN_DATA.length;
            drawLineSeparator(graphBottom);

            //Draw zero axis
            int center_val = (MIN_VALUES[val_num] + MAX_VALUES[val_num]) / 2;
            float half = PApplet.map(center_val, MIN_VALUES[val_num], MAX_VALUES[val_num], 0, mainWindow.height/COLUMN_DATA.length);
            mainWindow.stroke(180);
            mainWindow.line(0, half + graphBottom, width, half + graphBottom);


            mainWindow.stroke(initedColorsArray[val_num]);
            mainWindow.fill(initedColorsArray[val_num]);

            mainWindow.textSize(12);
            mainWindow.text(center_val, 10, half + graphBottom - 5);


            mainWindow.textSize(18);
            mainWindow.text(COLUMN_NAMES[val_num] + " [" + String.valueOf(MIN_VALUES[val_num]) + "; " + String.valueOf(MAX_VALUES[val_num]) + "]", 10, graphBottom + 20);
            mainWindow.noFill();


            float xpos = width;
            float ypos;

            mainWindow.beginShape();
            for(int i = COLUMN_DATA[val_num].length - 1; i > 0; i--)
            {
                //TODO: move xpos calc upper
                if(i < width - 1)
                {
                    float offset = (float) width / (float) showGraphTime * (float)MILLIS_BETWEEN_PACK[i];
                    xpos = xpos - offset;
                }

                if(showAllGraphsOnOneAxis)
                {
                    ypos = PApplet.map(COLUMN_DATA[val_num][i], MIN_VALUES[val_num], MAX_VALUES[val_num], 0, mainWindow.height);
                }
                else
                {
                    ypos = PApplet.map(COLUMN_DATA[val_num][i], MIN_VALUES[val_num], MAX_VALUES[val_num], 0, mainWindow.height/COLUMN_DATA.length);
                    ypos = ypos + graphBottom;
                }

                mainWindow.vertex(xpos, ypos);

                if(xpos < 0)
                {
                    break;
                }
            }
            mainWindow.endShape();
        }
    }

    public void drawLineSeparator(float graphBottom)
    {
        mainWindow.stroke(80);
        mainWindow.strokeWeight(3);
        mainWindow.line(0, graphBottom, width, graphBottom);
        mainWindow.strokeWeight(1);
    }

    public void drawNet()
    {
        for (int i = 0; i<=width/10; i++) {
            mainWindow.stroke(20); // gray
            mainWindow.line(0, i*10, width, i*10);
        }

        for(int i=0; i < showGraphTime / lineSeparatorEvery; i++)
        {
            float lineTimeMs = timingOffset + lineSeparatorEvery * i;
            float lineXpos = (float)width - ((float)width / (float)showGraphTime * lineTimeMs);
            drawSecondsLine(lineXpos);
        }

    }

    public void drawSecondsLine(float xPos)
    {
        mainWindow.line(xPos, 0, xPos, mainWindow.height);
    }

    public void processData(String serialData)
    {
        if(!columnNamesInited)
        {
            System.out.print("Columns are not inited");
            return;
        }

        // put all data and related timings one array back
        for(int i = 1; i < width; i++)
        {
            for(int val_num = 0; val_num < COLUMN_DATA.length; val_num++)
            {
                COLUMN_DATA[val_num][i-1] = COLUMN_DATA[val_num][i];
            }

            MILLIS_BETWEEN_PACK[i-1] = MILLIS_BETWEEN_PACK[i];
        }


        String[] packMillis = serialData.split("\\|");
        if(packMillis.length == 2)
        {
            int arduinoMillis = Integer.parseInt(packMillis[packMillis.length - 1]);

            if(!deltaMillisCalc)
            {
                lastMillis = 0;
            }
            int diff = arduinoMillis - lastMillis;
            MILLIS_BETWEEN_PACK[width - 1] = diff;
            timingOffset += diff;
            if(timingOffset > lineSeparatorEvery)
            {
                timingOffset = timingOffset % lineSeparatorEvery;
            }


            lastMillis = arduinoMillis;
            deltaMillisCalc = true;


            serialData = packMillis[0];
        }


        String[] incomingValues = serialData.split(",");

        for(int val_num = 0; val_num < incomingValues.length; val_num++)
        {
            COLUMN_DATA[val_num][width-1] = Integer.parseInt(incomingValues[val_num]);
        }

    }


    public boolean initColumnNames(String serialData, int width)
    {
        int startColumnsIndex = serialData.indexOf(':');
        if(startColumnsIndex == -1)
        {

            //If Arduino already writes in serial port, we've got garbage in serialData.
            //So wait untill arduino resets
            return false;
        }

        System.out.print("Init string from arduino: ");
        System.out.println(serialData);

        serialData = serialData.substring(startColumnsIndex + 1);
        COLUMN_NAMES = serialData.split(",");

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


                String[] aAdditionalData = AdditionalData.split(";");

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
        MILLIS_BETWEEN_PACK = new int[width];
        Graph.this.width = width;

        columnNamesInited = true;

        return true;
    }
}
