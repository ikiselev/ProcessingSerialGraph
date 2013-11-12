import org.junit.Test;
import processing.core.PApplet;



public class ProcessingSerialGraphTest {
    @Test
    public void testDraw() throws Exception {
        ProcessingSerialGraph window = new ProcessingSerialGraph();


        window.graph.initColumnNames("Columns:gyro.x{-32768;32767},gyro.y{-32768;32767},gyro.z{-32768;32767},acc.x{-2048;2047},acc.y{-2048;2047},acc.z{-2048;2047}", 1200);
        window.graph.processData("20,25,76,11,65,34|100");
        window.graph.processData("20,25,76,110,65,304|100");
        window.graph.processData("20,250,76,11,650,34|100");
        window.graph.processData("200,25,76,11,65,34|100");
        window.graph.processData("200,25,76,11,65,34|100");
        window.graph.processData("200,25,76,11,65,34|100");
        window.graph.processData("200,25,76,11,65,34|100");
        window.graph.processData("200,25,76,11,65,34|100");
        window.graph.processData("200,25,76,11,65,34|100");
        window.graph.processData("200,25,76,11,65,34|100");


        String[] args = {""};
        PApplet.runSketch(args, window);
        while (true){

        }
    }
}
