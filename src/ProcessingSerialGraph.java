import processing.core.PApplet;
import sys.Graph;
import sys.ProcessingApplet;

public class ProcessingSerialGraph extends ProcessingApplet {

    Graph graph;

    static public void main(String args[]) {
        PApplet.main(new String[]{"ProcessingSerialGraph"});
    }

    public void userSetup()
    {
        graph = new Graph(this, windowWidth);

        noLoop();
    }

    public void draw()
    {
        background(0); // black
        this.getGraph().drawGraph(dataProcessor.graphData);
    }



    public void processData(String serialData)
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
