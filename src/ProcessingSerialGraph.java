import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class ProcessingSerialGraph extends PApplet {

    Serial arduino;
    String useSerialPort[] = {"COM7", "/dev/tty.usbserial-AH01RRV0"};
    Graph graph = new Graph(this);

    static public void main(String args[]) {
        PApplet.main(new String[]{"ProcessingSerialGraph"});
    }

    protected Serial getSerial()
    {
        Serial result = null;

        println(Serial.list()); // Use this to print connected serial devices
        String list[] = Serial.list();
        for (String sPort: useSerialPort)
        {
            int portIndex = Arrays.asList(list).indexOf(sPort);
            if(portIndex != -1)
            {
                result = new Serial(this, Serial.list()[portIndex], 115200);
            }
        }

        if(result == null)
        {
            result = new Serial(this, Serial.list()[0], 115200);
        }


        return result;
    }

    public void setup()
    {
        size(1200, 700);

        arduino = getSerial();
        arduino.clear();
        arduino.bufferUntil('\n'); // Buffer until line feed
        smooth();
    }

    public void draw()
    {
        background(0); // black
        graph.drawGraph();
    }

    public void serialEvent (Serial arduino)
    {
        String serialData = arduino.readStringUntil('\n');
        serialData = trim(serialData);
        if (serialData != null && !serialData.equals("")) {
            if(graph.columnNamesInited)
            {
                graph.processData(serialData);
            }
            else
            {
                graph.initColumnNames(serialData, width);
            }
        }

        arduino.clear();
    }

}
