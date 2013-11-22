import org.junit.Assert;
import org.junit.Test;
import processing.core.PApplet;



public class ProcessingSerialGraphTest {

    @Test
    public void testDraw() throws Exception {
        ProcessingSerialGraphMock window = new ProcessingSerialGraphMock();

        window.setGraph(new GraphMock(window, 1200));
        window.dataProcessor = new DataProcessor(1200);

        window.dataProcessor.initColumnNames("TestColumns:gyro.x{-32768;32767},gyro.y{-32768;32767},gyro.z{-32768;32767},acc.x{-2048;2047},acc.y{-2048;2047},acc.z{-2048;2047}");

        String[] args = {""};
        PApplet.runSketch(args, window);

        /**
         * I'm sorry for commented code. I must go on...
         */

        /**
         * One line, 200 ms behind
         */
        processEvent(window, "-14472,0,-78,-169,200,-501|200");

        //TODO: Asserts are available only after redraw. But we can't redraw directly bypass draw method.

//        Assert.assertEquals(1, graph.SecondsLineSeparatorXPos.size());
        // 1176.0f = 1200 - (1200 / 10000 * 200)
        // 1176.0f = width - (width / windowShowTime * 200ms). windowShowTime - time that fits on window, 10sec
        //Assert.assertEquals(1176.0f, window.graph.SecondsLineSeparatorXPos.get(0), 0.1f);



        /**
         * One line, 700 ms behind
         */
        processEvent(window, "-8572,-59,-78,-269,11,-801|700");
        //1116.0f = 1200 - (1200 / 10000 * 700)
        //Assert.assertEquals(1116.0f, window.graph.SecondsLineSeparatorXPos.get(0), 0.1f);



        /**
         * One line, 1000ms ahead, 100ms behind
         */
        processEvent(window, "-17,-32,-71,-161,-5,-926|1100");
        // 1100 > 1000, so 2 lines:
        //1188.0f = 1200 - (1200 / 10000 * (1100 - 1000))
        //Assert.assertEquals(1188.0f, window.graph.SecondsLineSeparatorXPos.get(0), 0.1f);
        //1068.0f = 1200 - (1200 / 10000 * 1100)
        //Assert.assertEquals(1068.0f, window.graph.SecondsLineSeparatorXPos.get(1), 0.1f);
        //Assert.assertEquals(1188.0f, graph.SecondsLineSeparatorXPos.get(0), 0.1f); // If not in this order


        /**
         * We had delay in microcontroller, now is 10.7 sec!
         * TODO: unexpected graph trajectory
         */
        processEvent(window, "-32152,-32100,-25000,-2000,2000,-1800|10700");
        /*Assert.assertEquals(10, graph.SecondsLineSeparatorXPos.size());
        //1116.0f = 1200 - (1200 / 10000 * 700)
        Assert.assertEquals(1116.0f, graph.SecondsLineSeparatorXPos.get(0), 0.1f);
        //996.0f = 1200 - (1200 / 10000 * 1700)
        Assert.assertEquals(996.0f, graph.SecondsLineSeparatorXPos.get(1), 0.1f);
        //876.0f = 1200 - (1200 / 10000 * 2700)
        Assert.assertEquals(876.0f, graph.SecondsLineSeparatorXPos.get(2), 0.1f);
        //756.0f = 1200 - (1200 / 10000 * 3700)
        Assert.assertEquals(756.0f, graph.SecondsLineSeparatorXPos.get(3), 0.1f);
        //636.0f = 1200 - (1200 / 10000 * 4700)
        Assert.assertEquals(636.0f, graph.SecondsLineSeparatorXPos.get(4), 0.1f);
        //516.0f = 1200 - (1200 / 10000 * 5700)
        Assert.assertEquals(516.0f, graph.SecondsLineSeparatorXPos.get(5), 0.1f);
        //396.0f = 1200 - (1200 / 10000 * 6700)
        Assert.assertEquals(396.0f, graph.SecondsLineSeparatorXPos.get(6), 0.1f);
        //276.0f = 1200 - (1200 / 10000 * 7700)
        Assert.assertEquals(276.0f, graph.SecondsLineSeparatorXPos.get(7), 0.1f);
        //156.0f = 1200 - (1200 / 10000 * 8700)
        Assert.assertEquals(156.0f, graph.SecondsLineSeparatorXPos.get(8), 0.1f);
        //36.0f = 1200 - (1200 / 10000 * 9700)
        Assert.assertEquals(36.0f, graph.SecondsLineSeparatorXPos.get(9), 0.1f);*/
        //-84.0f = 1200 - (1200 / 10000 * 10700) - not drawn



        /**
         * This loop should be here to avoid window closing
         */
        while (true)
        {

        }
    }

    protected void processEvent(ProcessingSerialGraph window, String sData)
    {
        /**
         * Visualize graph
         */
        window.dataProcessor.processData(sData);
        window.redraw();
        window.delay(1250);
    }
}
