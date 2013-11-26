import processing.core.PApplet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

public class SerialMockReader implements Runnable
{
    PApplet parent;
    String filename;
    BufferedReader reader;
    Method serialEventMethod;

    private final Thread t;

    public SerialMockReader(PApplet parent, String filename)  {
        this.parent = parent;
        this.filename = filename;

        try
        {
            this.reader = new BufferedReader( new FileReader(filename));
        } catch ( IOException e)
        {
            System.out.println("File read error: " + e.getMessage());
        }

        try {
            serialEventMethod = parent.getClass().getMethod("fileEvent", new Class[] { String.class });
        } catch (Exception e) {
            // no such method, or an error.. which is fine, just ignore
        }

        t = new Thread(this, "File Data Thread");
    }

    public void start() {
        t.start();
    }

    public void run() {
        boolean deltaMillisCalc = false;
        int lastMillis = 0;

        if(reader == null)
        {
            return ;
        }

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (serialEventMethod != null) {
                    try {
                        //TODO: Refactor dataProcessor, move method.
                        //TODO: using void processData from dataProcessor
                        String[] packMillis = line.split("\\|");
                        if(packMillis.length == 2)
                        {
                            int arduinoMillis = Integer.parseInt(packMillis[packMillis.length - 1]);

                            if(!deltaMillisCalc)
                            {
                                lastMillis = arduinoMillis;
                            }
                            int diff = arduinoMillis - lastMillis;

                            //Delay to view graph partially
                            parent.delay(diff);

                            lastMillis = arduinoMillis;
                            deltaMillisCalc = true;
                        }


                        serialEventMethod.invoke(parent, new Object[]{line});
                    } catch (Exception e) {
                        String msg = "error, disabling fileEvent() for " + filename;
                        System.err.println(msg);
                        e.printStackTrace();
                        serialEventMethod = null;
                    }
                }
            }
        } catch (IOException e) {
            line = null;
            System.out.println("Error while reading file");
        }
    }
}
