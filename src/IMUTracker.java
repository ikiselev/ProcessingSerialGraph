import Preprocessors.P3DPreprocessor;
import processing.core.PApplet;

import java.math.RoundingMode;
import java.text.DecimalFormat;

//TODO: Remove copy-pase from GyroDrift
public class IMUTracker extends ProcessingApplet
{
    int dataProcessorWidth = 2;

    int textOffest = 0;

    float gyroAngleX;
    float gyroAngleY;
    float gyroAngleZ;

    float accAngleX;
    float accAngleY;

    static public void main(String args[]) {
        PApplet.main(new String[]{"IMUTracker"});
    }

    public void setup()
    {
        super.setup();
        dataProcessor.addPreprocessor(new P3DPreprocessor());
    }

    public void draw()
    {
        background(0xff000000);
        textSize(25);
        lights();


        textOffest = 0;
        drawBarText("X axis", gyroAngleX);
        drawBarText("   X Acc axis", accAngleX * RAD_TO_DEG);
        drawBarText("Y axis", gyroAngleY);
        drawBarText("   Y Acc axis", accAngleY );
        drawBarText("Z axis", gyroAngleZ);

        pushMatrix();

        translate(windowWidth / 2, windowHeight / 2, 0);

        rotateX(radians(gyroAngleX));
        rotateY(radians(gyroAngleZ)); //it is normal Z axis on Y
        rotateZ(radians(gyroAngleY));

        drawBox();
        popMatrix();

    }

    public void drawBox()
    {
        box(100, 12, 400);
    }

    public void drawBarText(String text, float angle)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(2);
        text(text + ": " + df.format(angle), 10, 20 + textOffest);
        textOffest += 25;
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

        gyroAngleX -= dataProcessor.graphData.COLUMN_DATA[0][getDataProcessorWidth() - 1];
        gyroAngleY -= dataProcessor.graphData.COLUMN_DATA[1][getDataProcessorWidth() - 1];
        gyroAngleZ -= dataProcessor.graphData.COLUMN_DATA[2][getDataProcessorWidth() - 1];

        accAngleX = dataProcessor.graphData.COLUMN_DATA[3][getDataProcessorWidth() - 1];
        accAngleY = dataProcessor.graphData.COLUMN_DATA[4][getDataProcessorWidth() - 1];
    }

    public String getRenderer()
    {
        return P3D;
    }

    public int getDataProcessorWidth() {
        return dataProcessorWidth;
    }
}
