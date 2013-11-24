import Preprocessors.RecursiveFilter;
import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class ProcessingSerialGraph extends PApplet {

    int windowWidth = 1200;
    int windowHeight = 700;


    Serial arduino;
    DataProcessor dataProcessor;
    Graph graph;

    static public void main(String args[]) {
        PApplet.main(new String[]{"ProcessingSerialGraph"});
    }

    public void setup()
    {
        size(windowWidth, windowHeight);

        graph = new Graph(this, windowWidth);

        dataProcessor = new DataProcessor(windowWidth);
        dataProcessor.addPreprocessor(new RecursiveFilter());

        try
        {
            arduino = SerialPort.getSerial(this);
        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        smooth();
        noLoop();
    }

    public void draw()
    {
        background(0); // black
        this.getGraph().drawGraph(dataProcessor.graphData);
    }

    public void serialEvent (Serial arduino)
    {
        String serialData = arduino.readStringUntil('\n');
        serialData = trim(serialData);
        if (serialData != null && !serialData.equals("")) {
            dataProcessor.processData(serialData);
            redraw();
        }

        arduino.clear();
    }


    public Graph getGraph() {
        return this.graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
