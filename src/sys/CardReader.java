package sys;

import processing.core.PApplet;

import java.io.*;
import java.lang.reflect.Method;

public class CardReader extends MockReader
{
    String filename;
    Method serialEventMethod;

    String UNIQUE_DELIMITER = "\\$\\$";
    String END_DELIMITER = "\n";


    int SECTOR_SIZE = 512;
    int MAX_LINE_LENGTH = 1024;

    int uniqueNumber;
    boolean uniqueNumberInited = false;

    String fileContents = "";

    private final Thread t;



    public CardReader(ProcessingApplet parent, String filename)
    {
        super(parent, filename);
        int startBlock = Integer.parseInt(parent.appSettings.getProperty("CardReader.startBlock"));
        boolean headerLine = true;

        try
        {
            File diskRoot = new File(this.getFilename());
            RandomAccessFile diskAccess = new RandomAccessFile (diskRoot, "r");
            byte[] content = new byte[SECTOR_SIZE];


            String bufString = "";
            boolean validData = true;

            while(validData)
            {
                diskAccess.seek(SECTOR_SIZE * startBlock);
                diskAccess.readFully(content);

                String contentString = new String(content, 0, SECTOR_SIZE);

                int nullTerminator;
                if((nullTerminator = contentString.indexOf("\0")) > 0)
                {
                    /**
                     * Обрезаем нули
                     */
                    contentString = contentString.substring(0, nullTerminator);
                }
                bufString += contentString;
                if(bufString.length() > MAX_LINE_LENGTH)
                {
                    /**
                     * Reached max bound
                     */
                    break;
                }
                String[] messages = bufString.split(END_DELIMITER);
                boolean lastMessageInBufferIsFull = bufString.endsWith(END_DELIMITER);

                for(int i=0; i < messages.length; i++)
                {
                    String message = messages[i];


                    if(headerLine)
                    {
                        headerLine = false;
                        fileContents += bufString;
                        bufString = "";
                    }
                    else
                    {
                        String[] dataStruct = message.split(UNIQUE_DELIMITER);
                        if(dataStruct.length == 2)
                        {
                            if(!uniqueNumberInited)
                            {
                                uniqueNumberInited = true;
                                uniqueNumber = Integer.parseInt(dataStruct[0]);
                            }
                            else
                            {
                                uniqueNumber++;
                                if(uniqueNumber > 255)
                                {
                                    uniqueNumber = 0;
                                }
                                if(uniqueNumber != Integer.parseInt(dataStruct[0]))
                                {
                                    //TODO: Error!
                                    System.out.println("END?!");
                                    validData = false;
                                    fileContents = fileContents.trim();
                                    break;
                                }
                            }

                            fileContents += dataStruct[1] + END_DELIMITER;
                            bufString = "";
                        }
                    }
                }

                startBlock++;
            }



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

        String[] lines = fileContents.split(END_DELIMITER);

        for(String line : lines) {
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

    }
}
