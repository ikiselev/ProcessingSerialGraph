import processing.core.PApplet;
import processing.serial.Serial;

import java.math.RoundingMode;
import java.text.DecimalFormat;

abstract public class ProcessingApplet extends PApplet
{

    int windowWidth = 1200;
    int windowHeight = 700;

    int dataProcessorWidth = windowWidth;

    Serial arduino;
    //String filename = "C:\\Users\\Alabay\\Dropbox\\Arduino\\Шипиловская-Борисово. 25.11.13.txt";
    //String filename = "C:\\Users\\ikiselev\\Dropbox\\Arduino\\Шипиловская-Борисово. 25.11.13.txt";
    String filename = null;

    DataProcessor dataProcessor;


    public void setup()
    {
        size(windowWidth, windowHeight, this.getRenderer());

        dataProcessor = new DataProcessor(getDataProcessorWidth());

        initDataProvider();

        smooth();
    }

    public void initDataProvider()
    {
        if(filename != null)
        {
            SerialMockReader serialMockReader = new SerialMockReader(this, filename);
            System.out.println("Using " + filename);
            serialMockReader.start();
        }
        else
        {
            arduino = SerialPort.getSerial(this);
        }
    }

    public String getFormatedFloat(float value)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(2);
        return df.format(value);
    }

    public void serialEvent (Serial arduino)
    {
        String serialData = arduino.readStringUntil('\n');

        this.processData(serialData);

        arduino.clear();
    }

    public void fileEvent(String line)
    {
        this.processData(line);
    }

    abstract void processData(String serialData);

    public String getRenderer()
    {
        return JAVA2D;
    }

    public int getDataProcessorWidth() {
        return dataProcessorWidth;
    }
}
