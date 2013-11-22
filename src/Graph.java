import processing.core.PApplet;

/**
 * TODO: set public or protected access specifiers
 */

public class Graph {
    boolean showAllGraphsOnOneAxis = false;


    int showGraphTime = 10000; //10sec

    //Graph colors
    int[] graphColors = { 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF };

    //Default graph color
    int defaultGraphColor = 0xcccccc;

    //This array is actually used for performance reasons
    int[] initedColorsArray;
    boolean isColorsInited = false;



    int width;

    PApplet mainWindow;

    public Graph(PApplet mainWindow, int width) {
        this.mainWindow = mainWindow;
        this.width = width;
    }

    public void drawGraph(GraphData graphData)
    {
        //Must init column names
        if(!graphData.columnNamesInited)
        {
            return;
        }

        if(!isColorsInited)
        {
            initColorsArray(graphData);
        }

        this.drawNet(graphData.lineSeparatorEvery, graphData.timingOffset, graphData.elapsedTime);

        // redraw each graph
        for(int val_num = 0; val_num < graphData.COLUMN_DATA.length; val_num++)
        {
            float graphBottom = val_num * mainWindow.height/graphData.COLUMN_DATA.length;
            drawLineSeparator(graphBottom);

            //Draw zero axis
            int center_val = (graphData.MIN_VALUES[val_num] + graphData.MAX_VALUES[val_num]) / 2;
            float half = PApplet.map(center_val, graphData.MIN_VALUES[val_num], graphData.MAX_VALUES[val_num], 0, mainWindow.height/graphData.COLUMN_DATA.length);
            mainWindow.stroke(180);
            mainWindow.line(0, half + graphBottom, width, half + graphBottom);


            mainWindow.stroke(initedColorsArray[val_num]);
            mainWindow.fill(initedColorsArray[val_num]);

            mainWindow.textSize(12);
            mainWindow.text(center_val, 10, half + graphBottom - 5);


            mainWindow.textSize(18);
            mainWindow.text(graphData.COLUMN_NAMES[val_num] + " [" + String.valueOf(graphData.MIN_VALUES[val_num]) + "; " + String.valueOf(graphData.MAX_VALUES[val_num]) + "]", 10, graphBottom + 20);
            mainWindow.noFill();


            float xpos = width;
            float ypos;

            int leftIndex = graphData.COLUMN_DATA[val_num].length - graphData.valuesFilled - 1;
            if(leftIndex < 0)
            {
                leftIndex = 0;
            }
            mainWindow.beginShape();
            for(int i = graphData.COLUMN_DATA[val_num].length - 1; i > leftIndex; i--)
            {
                //TODO: move xpos calc upper
                if(i < width - 1)
                {
                    float offset = (float) width / (float) showGraphTime * (float)graphData.MILLIS_BETWEEN_PACK[i];
                    xpos = xpos - offset;
                }

                if(showAllGraphsOnOneAxis)
                {
                    ypos = PApplet.map(graphData.COLUMN_DATA[val_num][i], graphData.MIN_VALUES[val_num], graphData.MAX_VALUES[val_num], 0, mainWindow.height);
                }
                else
                {
                    ypos = PApplet.map(graphData.COLUMN_DATA[val_num][i], graphData.MIN_VALUES[val_num], graphData.MAX_VALUES[val_num], 0, mainWindow.height/graphData.COLUMN_DATA.length);
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

    public void drawNet(int lineSeparatorEvery, int timingOffset, int elapsedTime)
    {
        for (int i = 0; i<=width/10; i++) {
            mainWindow.stroke(20); // gray
            mainWindow.line(0, i*10, width, i*10);
        }

        mainWindow.fill(70);
        mainWindow.textSize(11);

        int rightValueSeconds = (int)Math.floor(elapsedTime / lineSeparatorEvery);
        for(int i=0; i < showGraphTime / lineSeparatorEvery; i++)
        {
            float lineTimeMs = timingOffset + lineSeparatorEvery * i;
            float lineXpos = (float)width - ((float)width / (float)showGraphTime * lineTimeMs);
            drawSecondsLine(lineXpos);
            if(rightValueSeconds >= 0)
            {
                mainWindow.text(rightValueSeconds + "s",lineXpos,30);
                rightValueSeconds--;
            }
        }

    }

    public void drawSecondsLine(float xPos)
    {
        mainWindow.line(xPos, 0, xPos, mainWindow.height);
    }


    public void initColorsArray(GraphData graphData)
    {
        initedColorsArray = new int[graphData.COLUMN_NAMES.length];

        for(int i=0; i < graphData.COLUMN_NAMES.length; i++)
        {
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

        isColorsInited = true;
    }
}
