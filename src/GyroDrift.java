import Preprocessors.GyroDegreesPerSecond;
import processing.core.PApplet;

public class GyroDrift extends ProcessingApplet
{
    int dataProcessorWidth = 2;

    int BarLength = 250;
    int BarHight = 10;

    float gyroAngleX;
    float gyroAngleY;
    float gyroAngleZ;

    int packets = 0;
    int lastSecond = 0;
    int index = 0;
    int maxIndex = 2;
    int packetsPerSeconds[] = new int[maxIndex];

    static public void main(String args[]) {
        PApplet.main(new String[]{"GyroDrift"});
    }

    public void setup()
    {
        super.setup();
        dataProcessor.addPreprocessor(new GyroDegreesPerSecond());
        /**
         * Avoid millis between pack be too large after allow shift data in preCalibration
         */
        dataProcessor.setAccumulateMillisBetweenPack(false);
    }

    public void draw()
    {
        background(0xff000000);
        textSize(25);


        text("Frames: " + frameCount, 20, 20);
        text("Packets: " + packets, 20, 50);

        text("FPS: " + getFormatedFloat(frameRate), 220, 20);
        int lastIndex = (index - 1 < 0) ? maxIndex - 1 : index - 1;
        text("PPS: " + packetsPerSeconds[lastIndex], 220, 50);

        text("Self-calibration:", 400, 20);

        if(!dataProcessor.graphData.columnNamesInited)
        {
            return;
        }

        text("X: " + getFormatedFloat(dataProcessor.graphData.COLUMN_DATA[3][getDataProcessorWidth() - 1]), 400, 40);
        text("Y: " + getFormatedFloat(dataProcessor.graphData.COLUMN_DATA[4][getDataProcessorWidth() - 1]), 400, 60);
        text("Z: " + getFormatedFloat(dataProcessor.graphData.COLUMN_DATA[5][getDataProcessorWidth() - 1]), 400, 80);

        translate(windowWidth / 3 - 200, height / 2);
        drawBarText("X axis", gyroAngleX);
        pushMatrix();
        rotate(radians(gyroAngleX));
        drawBar();
        popMatrix();

        translate(windowWidth / 3, 0);
        drawBarText("Y axis", gyroAngleY);
        pushMatrix();
        rotate(radians(gyroAngleY));
        drawBar();
        popMatrix();

        translate(windowWidth / 3, 0);
        drawBarText("Z axis", gyroAngleZ);
        pushMatrix();
        rotate(radians(gyroAngleZ));
        drawBar();
        popMatrix();
    }

    public void drawBar()
    {
        rect(-BarLength / 2, -BarHight / 2, BarLength, BarHight);
    }

    public void drawBarText(String text, float angle)
    {
        text(text + ": " + getFormatedFloat(angle), -BarLength / 2, -BarLength / 2);
    }


    public void mouseClicked() {
        gyroAngleX = 0.0f;
        gyroAngleY = 0.0f;
        gyroAngleZ = 0.0f;
    }

    public void processData (String serialData)
    {
        serialData = trim(serialData);
        if (serialData != null && !serialData.equals("")) {
            dataProcessor.processData(serialData);
        }


        int currentSecond = (int)Math.ceil(millis() / 1000);
        if(lastSecond != currentSecond )
        {
            lastSecond = currentSecond;
            index++;
            if(index >= maxIndex)
            {
                index = 0;
            }
            packetsPerSeconds[index] = 0;
        }

        gyroAngleX += dataProcessor.graphData.COLUMN_DATA[0][getDataProcessorWidth() - 1];
        gyroAngleY += dataProcessor.graphData.COLUMN_DATA[1][getDataProcessorWidth() - 1];
        gyroAngleZ += dataProcessor.graphData.COLUMN_DATA[2][getDataProcessorWidth() - 1];

        packets++;
        packetsPerSeconds[index] += 1;


    }

    public int getDataProcessorWidth() {
        return dataProcessorWidth;
    }
}
