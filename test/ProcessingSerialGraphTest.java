import org.junit.Test;
import processing.core.PApplet;



public class ProcessingSerialGraphTest {
    @Test
    public void testDraw() throws Exception {
        ProcessingSerialGraph window = new ProcessingSerialGraphMock();

        String[] args = {""};
        PApplet.runSketch(args, window);

        window.graph.initColumnNames("TestColumns:gyro.x{-32768;32767},gyro.y{-32768;32767},gyro.z{-32768;32767},acc.x{-2048;2047},acc.y{-2048;2047},acc.z{-2048;2047}", 1200);
        window.graph.processData("-72,-59,-78,-69,11,-1011|100");
        window.graph.processData("-17,-32,-71,-61,-5,-1026|200");
        window.graph.processData("-32,-71,-78,-55,0,-1021|300");
        window.graph.processData("-65,-40,-35,-54,1,-1000|400");
        window.graph.processData("-2,-79,-61,-46,-13,-1014|500");
        window.graph.processData("-11,-63,-80,-45,-10,-1014|600");
        window.graph.processData("827,-13855,342,35,268,-1109|700");
        window.graph.processData("3063,-17567,367,-89,191,-1171|800");
        window.graph.processData("6892,-18698,4,-117,191,-1095|900");
        window.graph.processData("8245,-16985,96,-117,184,-1095|1100");
        window.graph.processData("9143,-13561,-145,-231,191,-1146|1200");
        window.graph.processData("8770,3649,254,-214,194,-1030|1300");
        window.graph.processData("7630,8853,502,-214,194,-1030|1400");
        window.graph.processData("3834,7811,710,-130,109,-829|1500");
        window.graph.processData("1784,12073,757,-229,97,-976|1600");
        window.graph.processData("-2296,25494,594,-152,65,-909|1700");


        /**
         * This loop should be here to avoid window closing
         */
        while (true)
        {

        }
    }
}
