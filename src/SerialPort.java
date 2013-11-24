import processing.core.PApplet;

import java.util.Arrays;

public class SerialPort
{
    static String useSerialPort[] = {"COM7", "COM5", "COM8", "/dev/tty.usbserial-AH01RRV0"};

    public static processing.serial.Serial getSerial(PApplet window)
    {
        processing.serial.Serial result = null;

        PApplet.println(processing.serial.Serial.list()); // Use this to print connected serial devices
        String list[] = processing.serial.Serial.list();
        for (String sPort: useSerialPort)
        {
            int portIndex = Arrays.asList(list).indexOf(sPort);
            if(portIndex != -1)
            {
                result = new processing.serial.Serial(window, processing.serial.Serial.list()[portIndex], 115200);
            }
        }

        if(result == null)
        {
            result = new processing.serial.Serial(window, processing.serial.Serial.list()[0], 115200);
        }

        result.clear();
        result.bufferUntil('\n');
        return result;
    }
}
