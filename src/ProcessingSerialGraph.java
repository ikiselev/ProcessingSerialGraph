import Preprocessors.AccelerometerToSI;
import Preprocessors.RecursiveFilter;
import processing.core.PApplet;

public class ProcessingSerialGraph extends ProcessingApplet {

    Graph graph;

    static public void main(String args[]) {
        PApplet.main(new String[]{"ProcessingSerialGraph"});
    }

    public void setup()
    {
        super.setup();

        graph = new Graph(this, windowWidth);

        dataProcessor.addPreprocessor(new RecursiveFilter());
        dataProcessor.addPreprocessor(new AccelerometerToSI());

        noLoop();
    }

    public void draw()
    {
        background(0); // black
        this.getGraph().drawGraph(dataProcessor.graphData);
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
}
