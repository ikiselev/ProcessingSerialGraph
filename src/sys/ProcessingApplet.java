package sys;

import Preprocessors.PreprocessorAbstract;
import processing.core.PApplet;
import processing.serial.Serial;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

abstract public class ProcessingApplet extends PApplet
{

    public int windowWidth = 1200;
    public int windowHeight = 700;

    public AppProperties appSettings = new AppProperties(this.getClass().getSimpleName());

    int dataProcessorWidth = windowWidth;

    Serial arduino;

    public DataProcessor dataProcessor;



    /**
     * initDataProvider - Если файл, то super.setup должен вызываться после добавления препроцессоров
     */
    public void setup()
    {
        size(windowWidth, windowHeight, this.getRenderer());

        dataProcessor = new DataProcessor(getDataProcessorWidth());

        String[] Preprocessors = appSettings.getClassProperties(AppProperties.PREPROCESSORS);
        for (String preprocessor : Preprocessors)
        {
            try
            {
                Class cl = Class.forName("Preprocessors." + preprocessor.trim());
                dataProcessor.addPreprocessor((PreprocessorAbstract)cl.newInstance());
            }
            catch(ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                System.out.println("Properties error: " + e.getMessage());
            }
        }

        boolean accumulateMillisBetweenPack = appSettings.getClassPropertyBoolean(AppProperties.ACCUMULATE_MILLIS_BETWEEN_PACK);
        dataProcessor.setAccumulateMillisBetweenPack(accumulateMillisBetweenPack);

        userSetup();

        initDataProvider();

        smooth();
    }

    public void userSetup()
    {

    }

    public void initDataProvider()
    {
        String readerClass = appSettings.getProperty("MockReaderClass".trim());
        String filename = appSettings.getProperty(readerClass + ".filename");


        if(filename != null)
        {
            //TODO: strange try-catch behaivor in java 7...
            try
            {
                Class cl = Class.forName("sys." + readerClass);
                try
                {
                    Constructor c = cl.getConstructor(new Class[]{PApplet.class, String.class});
                    MockReader serialMockReader = (MockReader)c.newInstance(this, filename);

                    System.out.println("Using " + readerClass + " with target " + filename);
                    serialMockReader.start();

                } catch (NoSuchMethodException e)
                {
                    System.out.println(e.getMessage());
                }
                catch(InvocationTargetException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            catch(ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                System.out.println("Properties error: " + e.getMessage());
            }
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

    abstract public void processData(String serialData);

    public String getRenderer()
    {
        return JAVA2D;
    }

    public int getDataProcessorWidth() {
        return dataProcessorWidth;
    }
}
