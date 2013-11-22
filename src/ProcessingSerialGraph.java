import Preprocessors.RecursiveFilter;
import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class ProcessingSerialGraph extends PApplet {

    String useSerialPort[] = {"COM7", "COM5", "COM8", "/dev/tty.usbserial-AH01RRV0"};
    int windowWidth = 1200;
    int windowHeight = 700;


    Serial arduino;
    DataProcessor dataProcessor;
    Graph graph;

    static public void main(String args[]) {
        PApplet.main(new String[]{"ProcessingSerialGraph"});
    }

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

        result.clear();
        result.bufferUntil('\n'); // Buffer until line feed
        return result;
    }

    public void setup()
    {
        size(windowWidth, windowHeight);
        noLoop();

        graph = new Graph(this, windowWidth);

        dataProcessor = new DataProcessor(windowWidth);
        dataProcessor.addPreprocessor(new RecursiveFilter());

        arduino = getSerial();
        smooth();
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
