import processing.core.PApplet;
import sys.Graph;

import java.util.ArrayList;

public class GraphMock extends Graph {

    int size = mainWindow.width / 1000; //TODO: Don't know how to get it properly at the moment
    public ArrayList<Float> SecondsLineSeparatorXPos = new ArrayList<Float>(size);

    public GraphMock(PApplet mainWindow, int width) {
        super(mainWindow, width);
    }

    @Override
    public void drawNet(int lineSeparatorEvery, int timingOffset, int elapsedTime) {
        SecondsLineSeparatorXPos.clear();
        super.drawNet(lineSeparatorEvery, timingOffset, elapsedTime);
    }

    @Override
    public void drawSecondsLine(float xPos) {
        SecondsLineSeparatorXPos.add(xPos);
        super.drawSecondsLine(xPos);
    }
}
