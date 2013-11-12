import processing.core.PApplet;

import java.util.ArrayList;

public class GraphMock extends Graph {

    int size = mainWindow.width / this.lineSeparatorEvery;
    public ArrayList<Float> SecondsLineSeparatorXPos = new ArrayList<Float>(size);

    public GraphMock(PApplet mainWindow) {
        super(mainWindow);
    }

    @Override
    public void drawNet() {
        SecondsLineSeparatorXPos.clear();
        super.drawNet();
    }

    @Override
    public void drawSecondsLine(float xPos) {
        SecondsLineSeparatorXPos.add(xPos);
        super.drawSecondsLine(xPos);
    }
}
