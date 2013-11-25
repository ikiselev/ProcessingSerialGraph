import processing.core.PApplet;
import processing.serial.Serial;

abstract public class ProcessingApplet extends PApplet
{

    int windowWidth = 1200;
    int windowHeight = 700;

    int dataProcessorWidth = windowWidth;

    Serial arduino;
    String filename = "SerialMockFile.txt";

    DataProcessor dataProcessor;


    public void setup()
    {
        size(windowWidth, windowHeight);

        dataProcessor = new DataProcessor(getDataProcessorWidth());

        initDataProvider();

        smooth();
    }

    public void initDataProvider()
    {
        if(filename != null)
        {
            SerialMockReader serialMockReader = new SerialMockReader(this, filename);
            serialMockReader.start();
        }
        else
        {
            arduino = SerialPort.getSerial(this);
        }
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


    public int getDataProcessorWidth() {
        return dataProcessorWidth;
    }
}
