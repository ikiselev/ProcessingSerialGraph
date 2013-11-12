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
        processData(window, "-72,-59,-78,-69,11,-1011|100");
        processData(window, "-17,-32,-71,-61,-5,-1026|200");
        processData(window, "-32,-71,-78,-55,0,-1021|300");
        processData(window, "-65,-40,-35,-54,1,-1000|400");
        processData(window, "-2,-79,-61,-46,-13,-1014|500");
        processData(window, "-11,-63,-80,-45,-10,-1014|600");
        processData(window, "827,-13855,342,35,268,-1109|700");
        processData(window, "3063,-17567,367,-89,191,-1171|800");
        processData(window, "6892,-18698,4,-117,191,-1095|900");
        processData(window, "8245,-16985,96,-117,184,-1095|1100");


        window.graph.drawNet();
        Assert.assertEquals(1, graph.SecondsLineSeparatorXPos.size());
        float xPosSecondLine = graph.SecondsLineSeparatorXPos.get(0);
        //TODO: in real it is not 1080.0f. Make test-config for size(w,h) first!
        Assert.assertEquals(1080.0f, xPosSecondLine, 0.1f);

        processData(window, "9143,-13561,-145,-231,191,-1146|1200");
        processData(window, "8770,3649,254,-214,194,-1030|1300");
        processData(window, "7630,8853,502,-214,194,-1030|1400");
        processData(window, "3834,7811,710,-130,109,-829|1500");
        processData(window, "1784,12073,757,-229,97,-976|1600");
        processData(window, "-2296,25494,594,-152,65,-909|1700");


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
        window.delay(150);
    }
}
