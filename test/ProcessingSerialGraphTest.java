import org.junit.Assert;
import org.junit.Test;
import processing.core.PApplet;



public class ProcessingSerialGraphTest {
    @Test
    public void testDraw() throws Exception {
        ProcessingSerialGraph window = new ProcessingSerialGraphMock();

        GraphMock graph = new GraphMock(window);
        window.graph = graph;

        String[] args = {""};
        PApplet.runSketch(args, window);

        window.graph.initColumnNames("TestColumns:gyro.x{-32768;32767},gyro.y{-32768;32767},gyro.z{-32768;32767},acc.x{-2048;2047},acc.y{-2048;2047},acc.z{-2048;2047}", 1200);


        /**
         * One line, 200 ms behind
         */
        processData(window, "-14472,0,-78,-169,200,-501|200");
        /**
         * One line, 700 ms behind
         */
        processData(window, "-8572,-59,-78,-269,11,-801|700");
        /**
         * One line, 1000ms ahead, 100ms behind
         */
        processData(window, "-17,-32,-71,-161,-5,-926|1100");
        //One line, 1000ms ahead, 600ms behind
        processData(window, "-32,-71,-78,-55,0,-1021|1600");
        //And so on..,  1000ms ahead, 900 ms behind
        processData(window, "-65,-40,-35,-54,-61,-1000|1900");

        /**
         * Two lines, 100ms from last line to right side of window
         */
        processData(window, "-2,-79,-61,-46,-113,-1014|2100");
        /**
         * Two lines, 800ms from second line to right side of window
         */
        processData(window, "-11,-63,-80,-45,-99,-1014|2800");
        /**
         * Three lines, |____nothing_____|_1000ms_|_1000ms_|_200ms|
         */
        processData(window, "3063,-17567,367,-89,191,-1171|3200");


        //window.graph.drawNet();
        /*Assert.assertEquals(1, graph.SecondsLineSeparatorXPos.size());
        float xPosSecondLine = graph.SecondsLineSeparatorXPos.get(0);
        //TODO: in real it is not 1080.0f. Make test-config for size(w,h) first!
        Assert.assertEquals(1080.0f, xPosSecondLine, 0.1f);*/

        /**
         * This loop should be here to avoid window closing
         */
        while (true)
        {

        }
    }

    protected void processData(ProcessingSerialGraph window, String sData)
    {
        /**
         * Visualize graph
         */
        window.graph.processData(sData);
        window.delay(250);
    }
}
