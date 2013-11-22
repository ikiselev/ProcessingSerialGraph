import processing.serial.Serial;


public class ProcessingSerialGraphMock extends ProcessingSerialGraph
{

    GraphMock graph;

    @Override
    public void setup()
    {
        size(windowWidth, windowHeight);
        noLoop();
        smooth();
    }

    protected Serial getSerial()
    {
        return null;
    }

    public GraphMock getGraph() {
        return this.graph;
    }

    public void setGraph(GraphMock graph) {
        this.graph = graph;
    }

}
