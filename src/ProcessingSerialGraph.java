import Preprocessors.RecursiveFilter;
import processing.core.PApplet;
import processing.serial.Serial;

public class ProcessingSerialGraph extends PApplet {

    int windowWidth = 1200;
    int windowHeight = 700;


    Serial arduino;
    String filename = "SerialMockFile.txt";

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
        //dataProcessor.addPreprocessor(new RecursiveFilter());


        if(filename != null)
        {
            SerialMockReader serialMockReader = new SerialMockReader(this, filename);
            serialMockReader.start();
        }
        else
        {
            arduino = SerialPort.getSerial(this);
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

        this.processData(serialData);

        arduino.clear();
    }

    public void fileEvent(String line)
    {
        this.processData(line);
    }

    protected void processData(String serialData)
    {
        serialData = trim(serialData);
        if (serialData != null && !serialData.equals("")) {
            dataProcessor.processData(serialData);
            redraw();
        }
    }

    public Graph getGraph() {
        return this.graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
