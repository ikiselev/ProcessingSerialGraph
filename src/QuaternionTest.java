import Preprocessors.QuaternionPreprocessor;
import processing.core.PApplet;


public class QuaternionTest extends ProcessingApplet {

    float xAngle = 0.0f;
    float yAngle = 0.0f;


    static public void main(String args[]) {
        PApplet.main(new String[]{"QuaternionTest"});
    }



    public void setup() {
        super.setup();
        dataProcessor.addPreprocessor(new QuaternionPreprocessor());
        dataProcessor.setAccumulateMillisBetweenPack(false);
        smooth();
    }

    public void draw()
    {
        if(!dataProcessor.graphData.columnNamesInited)
        {
            return;
        }
        background(0xFF000000);

        text("xAngle: " + xAngle, 100, 100);
        text("yAngle: " + yAngle, 100, 140);



        pushMatrix();
            translate(windowWidth / 2, windowHeight / 2, 0);
            rotateZ(radians(-xAngle));
            rotateX(radians(-yAngle)); //it is normal Z axis on Y
            box(100, 12, 400);
        popMatrix();

    }

    @Override
    void processData(String serialData) {
        serialData = trim(serialData);
        if (serialData != null && !serialData.equals("")) {
            dataProcessor.processData(serialData);
        }

        xAngle = dataProcessor.graphData.COLUMN_DATA[0][getDataProcessorWidth() - 1];
        yAngle = dataProcessor.graphData.COLUMN_DATA[1][getDataProcessorWidth() - 1];

    }

    @Override
    public String getRenderer() {
        return OPENGL;
    }
}
