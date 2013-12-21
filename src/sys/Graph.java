package sys;

import processing.core.PApplet;

/**
 * TODO: set public or protected access specifiers
 */

public class Graph {
    boolean showAllGraphsOnOneAxis = false;

    //Graph colors
    int[] graphColors = { 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF };

    //Default graph color
    int defaultGraphColor = 0xcccccc;

    //This array is actually used for performance reasons
    int[] initedColorsArray;
    boolean isColorsInited = false;



    int width;

    public PApplet mainWindow;

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

        this.drawNet(graphData.lineSeparatorEvery, graphData.showGraphTime, graphData.timingOffset, graphData.elapsedTime);

        // redraw each graph

        //TODO: extract methods
        for(int group_num = 0; group_num < graphData.groups.length; group_num++)
        {
        for(int group_val_num = 0; group_val_num < graphData.groups[group_num].length; group_val_num++)
        {
            int val_num = graphData.groups[group_num][group_val_num];


            int textOffsetY = group_val_num * 20;
            float graphBottom = group_num * mainWindow.height / getGraphSectionsCount(graphData);
            drawLineSeparator(graphBottom);

            //Draw zero axis
            int center_val = (graphData.MIN_VALUES[val_num] + graphData.MAX_VALUES[val_num]) / 2;
            float half = PApplet.map(center_val, graphData.MIN_VALUES[val_num], graphData.MAX_VALUES[val_num], 0, mainWindow.height/getGraphSectionsCount(graphData));
            mainWindow.stroke(180);
            mainWindow.line(0, half + graphBottom, width, half + graphBottom);


            mainWindow.stroke(initedColorsArray[val_num]);
            mainWindow.fill(initedColorsArray[val_num]);

            mainWindow.textSize(12);
            mainWindow.text(center_val, 10, half + graphBottom - 5 + textOffsetY);


            mainWindow.textSize(18);
            mainWindow.text(graphData.COLUMN_NAMES[val_num] + " [" + String.valueOf(graphData.MIN_VALUES[val_num]) + "; " + String.valueOf(graphData.MAX_VALUES[val_num]) + "]", 10, graphBottom + 20 + textOffsetY);
            mainWindow.noFill();


            float xpos = width;
            float ypos;

            mainWindow.text(graphData.COLUMN_DATA[val_num][0], 220, graphBottom + 20 + textOffsetY);

            mainWindow.beginShape();
            for(int i = 0; i < graphData.valuesFilled; i++)
            {
                //TODO: move xpos calc upper
                float offset = (float) width / (float) graphData.showGraphTime * (float)graphData.MILLIS_BETWEEN_PACK[i];
                xpos = xpos - offset;

                if(showAllGraphsOnOneAxis)
                {
                    ypos = mainWindow.height - PApplet.map(graphData.COLUMN_DATA[val_num][i], graphData.MIN_VALUES[val_num], graphData.MAX_VALUES[val_num], 0, mainWindow.height);
                }
                else
                {
                    float max = mainWindow.height/getGraphSectionsCount(graphData);
                    ypos = max - PApplet.map(graphData.COLUMN_DATA[val_num][i], graphData.MIN_VALUES[val_num], graphData.MAX_VALUES[val_num], 0, max);
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

    }

    protected int getGraphSectionsCount(GraphData graphData)
    {
        if(graphData.groups.length > 0)
        {

            return graphData.groups.length;
        }


        return graphData.COLUMN_DATA.length;
    }

    public void drawLineSeparator(float graphBottom)
    {
        mainWindow.stroke(80);
        mainWindow.strokeWeight(3);
        mainWindow.line(0, graphBottom, width, graphBottom);
        mainWindow.strokeWeight(1);
    }

    public void drawNet(int lineSeparatorEvery, int showGraphTime, int timingOffset, int elapsedTime)
    {
        for (int i = 0; i<=mainWindow.height/10; i++) {
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
